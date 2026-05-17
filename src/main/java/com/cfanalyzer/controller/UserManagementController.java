package com.cfanalyzer.controller;

import com.cfanalyzer.dao.UserDao;
import com.cfanalyzer.model.CodeforcesUser;
import com.cfanalyzer.util.AlertUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class UserManagementController {

    @FXML private TextField handleTextField;
    @FXML private TableView<CodeforcesUser> userTable;
    @FXML private TableColumn<CodeforcesUser, Long> idColumn;
    @FXML private TableColumn<CodeforcesUser, String> handleColumn;
    @FXML private TableColumn<CodeforcesUser, Integer> ratingColumn;
    @FXML private TableColumn<CodeforcesUser, String> rankColumn;
    @FXML private TableColumn<CodeforcesUser, LocalDateTime> lastCrawledColumn;
    @FXML private TableColumn<CodeforcesUser, Integer> totalSolvedColumn;
    @FXML private TableColumn<CodeforcesUser, Boolean> statusColumn;

    private final UserDao userDao = new UserDao();
    private final ObservableList<CodeforcesUser> userList = FXCollections.observableArrayList();
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        handleColumn.setCellValueFactory(new PropertyValueFactory<>("handle"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rankTitle"));
        lastCrawledColumn.setCellValueFactory(new PropertyValueFactory<>("lastCrawledAt"));
        totalSolvedColumn.setCellValueFactory(new PropertyValueFactory<>("totalSolved"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        loadUsers();
    }

    private void loadUsers() {
        try {
            List<CodeforcesUser> users = userDao.findAll();
            userList.setAll(users);
            userTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể tải danh sách người dùng.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleAddUser() {
        String handle = handleTextField.getText().trim();
        if (handle.isEmpty()) {
            AlertUtil.showError("Lỗi", "Vui lòng nhập handle.");
            return;
        }

        try {
            // Fetch info from CF API
            CodeforcesUser user = fetchUserInfo(handle);
            if (user != null) {
                userDao.save(user);
                handleTextField.clear();
                loadUsers();
                AlertUtil.showInfo("Thành công", "Đã thêm người dùng: " + handle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Lỗi", "Không thể fetch thông tin người dùng từ Codeforces: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser() {
        CodeforcesUser selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showError("Lỗi", "Vui lòng chọn người dùng cần xóa.");
            return;
        }

        if (AlertUtil.showConfirm("Xác nhận", "Bạn có chắc chắn muốn xóa người dùng " + selectedUser.getHandle() + "?")) {
            try {
                userDao.delete(selectedUser.getId());
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Lỗi", "Không thể xóa người dùng.");
            }
        }
    }

    private CodeforcesUser fetchUserInfo(String handle) throws Exception {
        String url = "https://codeforces.com/api/user.info?handles=" + handle;
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("API error: " + response.code());
            
            JsonNode root = objectMapper.readTree(response.body().string());
            if (!"OK".equals(root.path("status").asText())) {
                throw new Exception(root.path("comment").asText());
            }

            JsonNode info = root.path("result").get(0);
            return CodeforcesUser.builder()
                    .handle(info.path("handle").asText())
                    .rating(info.path("rating").asInt())
                    .maxRating(info.path("maxRating").asInt())
                    .rankTitle(info.path("rank").asText())
                    .displayName(info.path("firstName").asText("") + " " + info.path("lastName").asText(""))
                    .country(info.path("country").asText())
                    .organization(info.path("organization").asText())
                    .avatarUrl(info.path("titlePhoto").asText())
                    .isActive(true)
                    .totalSolved(0)
                    .build();
        }
    }
}
