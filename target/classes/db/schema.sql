-- =============================================
-- DATABASE: codeforces_analyzer
-- =============================================
CREATE DATABASE IF NOT EXISTS codeforces_analyzer
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE codeforces_analyzer;

-- Bảng 1: Người dùng Codeforces được theo dõi
CREATE TABLE IF NOT EXISTS cf_users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    handle          VARCHAR(100) NOT NULL UNIQUE,   -- Nick Codeforces (vd: tourist)
    display_name    VARCHAR(200),                    -- Tên hiển thị
    rating          INT DEFAULT 0,                   -- Rating CF hiện tại
    max_rating      INT DEFAULT 0,                   -- Rating cao nhất
    rank_title      VARCHAR(50),                     -- grandmaster, master, etc.
    country         VARCHAR(100),
    organization    VARCHAR(200),
    avatar_url      VARCHAR(500),
    cf_registered_at DATETIME,                       -- Ngày đăng ký CF
    is_active       TINYINT(1) DEFAULT 1,            -- Có đang theo dõi không
    last_crawled_at DATETIME,                        -- Lần crawl gần nhất
    total_solved    INT DEFAULT 0,
    notes           TEXT,                            -- Ghi chú của admin
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_handle (handle),
    INDEX idx_rating (rating),
    INDEX idx_active (is_active)
) ENGINE=InnoDB;

-- Bảng 2: Các bài nộp (submissions) đã crawl
CREATE TABLE IF NOT EXISTS submissions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cf_submission_id    BIGINT NOT NULL,             -- ID bài nộp trên CF
    user_id             BIGINT NOT NULL,
    problem_id          VARCHAR(50),                 -- vd: 1234A
    problem_name        VARCHAR(500),
    contest_id          INT,
    problem_index       VARCHAR(10),                 -- A, B, C1, ...
    verdict             VARCHAR(50),                 -- OK, WRONG_ANSWER, TLE,...
    language            VARCHAR(100),               -- C++17, Python 3, Java 11,...
    time_consumed_ms    INT,
    memory_consumed_kb  INT,
    code_content        LONGTEXT,                    -- Source code đầy đủ
    code_length         INT,                         -- Số ký tự
    cf_submitted_at     DATETIME,                    -- Thời gian nộp trên CF
    crawled_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_analyzed         TINYINT(1) DEFAULT 0,        -- Đã phân tích AI chưa
    UNIQUE KEY uq_cf_submission (cf_submission_id),
    FOREIGN KEY (user_id) REFERENCES cf_users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_verdict (verdict),
    INDEX idx_analyzed (is_analyzed),
    INDEX idx_cf_submitted (cf_submitted_at),
    INDEX idx_problem (problem_id)
) ENGINE=InnoDB;

-- Bảng 3: Kết quả phân tích AI từng bài
CREATE TABLE IF NOT EXISTS analysis_results (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id           BIGINT NOT NULL UNIQUE,
    user_id                 BIGINT NOT NULL,
    -- Phân tích CTDL (Data Structures)
    data_structures_used    JSON,                   -- ["Array","HashMap","Stack",...]
    ds_primary              VARCHAR(100),            -- CTDL chính
    ds_complexity_score     TINYINT,                 -- 1-10: độ phức tạp CTDL
    -- Phân tích Thuật toán (Algorithms)
    algorithms_used         JSON,                   -- ["BFS","DP","Binary Search",...]
    algo_primary            VARCHAR(100),            -- Thuật toán chính
    algo_category           VARCHAR(100),            -- Graph, DP, Greedy, Math,...
    algo_complexity_score   TINYINT,                 -- 1-10: độ phức tạp giải thuật
    time_complexity         VARCHAR(50),             -- O(n log n), O(n^2),...
    space_complexity        VARCHAR(50),             -- O(n), O(1),...
    -- Phân tích AI-generated
    ai_probability          DECIMAL(5,2),            -- 0-100: % khả năng dùng AI
    ai_indicators           JSON,                   -- Các dấu hiệu phát hiện AI
    ai_explanation          TEXT,                    -- Giải thích tại sao nghi AI
    -- Đánh giá tổng thể
    code_quality_score      TINYINT,                 -- 1-10
    readability_score       TINYINT,                 -- 1-10
    originality_score       TINYINT,                 -- 1-10
    difficulty_estimate     VARCHAR(20),             -- easy/medium/hard/very_hard
    overall_summary         TEXT,                    -- Mô tả tổng quan
    raw_ai_response         LONGTEXT,                -- Raw JSON từ Claude API
    model_used              VARCHAR(100),            -- claude-sonnet-4-20250514
    tokens_used             INT,
    analysis_duration_ms    INT,
    analyzed_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES cf_users(id) ON DELETE CASCADE,
    INDEX idx_user_ai (user_id, ai_probability),
    INDEX idx_algo_category (algo_category)
) ENGINE=InnoDB;

-- Bảng 4: Đánh giá tổng hợp theo từng user (cập nhật sau mỗi lần phân tích)
CREATE TABLE IF NOT EXISTS user_evaluations (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                     BIGINT NOT NULL UNIQUE,
    -- Thống kê CTDL
    ds_skill_level              VARCHAR(50),         -- beginner/intermediate/advanced/expert
    ds_skill_score              DECIMAL(5,2),        -- 0-100
    most_used_ds                JSON,               -- Top 5 CTDL hay dùng
    ds_diversity_count          INT DEFAULT 0,       -- Số loại CTDL khác nhau
    -- Thống kê Thuật toán
    algo_skill_level            VARCHAR(50),
    algo_skill_score            DECIMAL(5,2),
    most_used_algorithms        JSON,
    algo_diversity_count        INT DEFAULT 0,
    strongest_category          VARCHAR(100),        -- Category giỏi nhất
    weakest_category            VARCHAR(100),
    -- Thống kê AI usage
    ai_usage_level              VARCHAR(50),         -- none/low/moderate/high/very_high
    avg_ai_probability          DECIMAL(5,2),        -- Trung bình % AI
    high_ai_submission_count    INT DEFAULT 0,       -- Số bài nghi AI cao (>70%)
    ai_usage_trend              VARCHAR(20),         -- increasing/decreasing/stable
    -- Thống kê tổng hợp
    total_analyzed              INT DEFAULT 0,
    avg_code_quality            DECIMAL(5,2),
    avg_difficulty              DECIMAL(5,2),
    problem_solving_style       TEXT,               -- Mô tả phong cách giải
    overall_assessment          TEXT,               -- Đánh giá tổng thể
    overall_score               DECIMAL(5,2),        -- 0-100: điểm tổng
    ranking_percentile          DECIMAL(5,2),        -- So sánh với tất cả user
    evaluated_at                DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES cf_users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Bảng 5: Log quá trình crawl
CREATE TABLE IF NOT EXISTS crawl_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    crawl_type      ENUM('MANUAL','SCHEDULED') DEFAULT 'MANUAL',
    status          ENUM('RUNNING','SUCCESS','FAILED','PARTIAL') DEFAULT 'RUNNING',
    submissions_found   INT DEFAULT 0,
    submissions_new     INT DEFAULT 0,
    error_message   TEXT,
    started_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    finished_at     DATETIME,
    duration_ms     INT,
    FOREIGN KEY (user_id) REFERENCES cf_users(id) ON DELETE SET NULL,
    INDEX idx_user_log (user_id),
    INDEX idx_started (started_at)
) ENGINE=InnoDB;

-- Bảng 6: Cấu hình hệ thống
CREATE TABLE IF NOT EXISTS app_settings (
    setting_key     VARCHAR(100) PRIMARY KEY,
    setting_value   TEXT NOT NULL,
    description     VARCHAR(500),
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Insert cấu hình mặc định
INSERT INTO app_settings VALUES
('crawl_schedule_cron', '0 0 2 * * ?', 'Cron biểu thức cho lịch crawl (mặc định 2 giờ sáng mỗi ngày)', NOW()),
('max_submissions_per_crawl', '200', 'Số submission tối đa crawl mỗi lần', NOW()),
('ai_analysis_batch_size', '10', 'Số bài phân tích AI cùng lúc', NOW()),
('cf_login_email', '', 'Email đăng nhập Codeforces', NOW()),
('cf_login_password', '', 'Mật khẩu Codeforces (mã hóa AES)', NOW()),
('claude_api_key', '', 'Anthropic API Key', NOW()),
('anthropic_model', 'claude-sonnet-4-20250514', 'Model Claude dùng để phân tích', NOW()),
('edge_driver_path', '', 'Đường dẫn tới msedgedriver.exe', NOW()),
('min_verdict_for_crawl', 'OK', 'Chỉ crawl bài verdict này (OK = Accepted)', NOW()),
('ai_detection_threshold', '70', 'Ngưỡng % coi là dùng AI (0-100)', NOW());

-- Bảng 7: Tag/nhóm người dùng
CREATE TABLE IF NOT EXISTS user_groups (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_group_members (
    user_id     BIGINT NOT NULL,
    group_id    INT NOT NULL,
    added_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES cf_users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_groups(id) ON DELETE CASCADE
) ENGINE=InnoDB;
