# javacodegym
Bước 1: Cài đặt phần mềm cần thiết (Prerequisites)

   1. Java Development Kit (JDK):
       * Cài đặt JDK 17 hoặc 21 (Project này đang dùng JavaFX 21).
       * Tải tại: Oracle JDK (https://www.oracle.com/java/technologies/downloads/) hoặc Azul Zulu (https://www.azul.com/downloads/zulu-community/).
       * Kiểm tra bằng lệnh: java -version.

   2. MySQL Server:
       * Tải và cài đặt MySQL Community Server (https://dev.mysql.com/downloads/installer/).
       * Trong quá trình cài đặt, hãy nhớ User (thường là root) và Password.

   3. Maven:
       * Tải tại: Maven Apache (https://maven.apache.org/download.cgi).
    .

   4. Microsoft Edge & Edge Driver:
       * Máy cần có trình duyệt Edge.
       * Tải Edge Driver đúng với phiên bản Edge đang dùng tại: Edge Driver https://developer.microsoft.com/vi-vn/microsoft-edge/tools/webdriver?form=MA13LH tải x64 stable
       * Giải nén và lưu đường dẫn file .exe này.

  ---

  Bước 2: Thiết lập Cơ sở dữ liệu (Database)

   1. Mở MySQL Workbench hoặc Command Line và chạy lệnh sau để tạo database:
   1     CREATE DATABASE codeforces_analyzer;
   2. Import cấu hình bảng (schema):
       * Tìm file tại: src/main/resources/db/schema.sql.
       * Copy nội dung file đó và chạy trong MySQL để tạo các bảng cần thiết.

  ---

  Bước 3: Cấu hình ứng dụng (Configuration)

  Mở file src/main/resources/config.properties và cập nhật các thông tin sau:

   1. Database: Thay đổi username/password của MySQL máy bạn.

   1     db.url=jdbc:mysql://localhost:3306/codeforces_analyzer?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   2     db.username=root
   3     db.password=Mật_Khẩu_Của_Bạn
   2. Google Gemini API: Lấy API Key tại Google AI Studio (https://aistudio.google.com/) và dán vào:

   1     gemini.api.key=AIzaSy... (Dán key của bạn ở đây)
   3. Edge Driver Path: Cập nhật đường dẫn đến file msedgedriver.exe bạn đã tải ở Bước 1.

   1     edge.driver.path=C:/duong/dan/den/msedgedriver.exe

  ---
Bước 4: mở win + r , sau đó bỏ : msedge.exe --remote-debugging-port=9222 --user-data-dir="C:\edge_debug_profile" vào chạy nó hiện ra thì vào codeforces đăng nhập nick đăng nhiều submittion trong code ở config.properties có acc khanh vô test , sửa các thứ như username, password mysql lại và edge.driver.path = link edgedriver.exe( ví dụ edge.driver.path=C:/Users/Dung/Downloads/edgedriver_win64/msedgedriver.exe) đăng nhập xong rồi làm bước 5
  Bước 5: Biên dịch và Chạy ứng dụng

  Mở Terminal (hoặc CMD/PowerShell) tại thư mục gốc của project:

   1. Dọn dẹp và Biên dịch (Clean & Compile):mvn clean compile

   2. Chạy ứng dụng:
      * Nếu project đã cấu hình plugin JavaFX trong pom.xml, bạn chạy: mvn javafx:run
      * Hoặc chạy file Main.java trực tiếp từ IDE (IntelliJ/Eclipse).

  ---
