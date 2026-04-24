-- MVP 1.0 Database Migration
-- Execute against: cb-crawl-dev

-- 1. Add online_status column to announcements for admin visibility control
ALTER TABLE announcements ADD COLUMN online_status TINYINT DEFAULT 1 COMMENT '0=offline, 1=online';

-- 2. Update expired_at for records where it's NULL (default: published_at + 90 days)
UPDATE announcements
SET expired_at = DATE_ADD(published_at, INTERVAL 90 DAY)
WHERE expired_at IS NULL AND published_at IS NOT NULL;

-- 3. Create index on online_status
CREATE INDEX idx_announcement_online_status ON announcements(online_status);

-- 4. Create page_views table
CREATE TABLE IF NOT EXISTS page_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visitor_id VARCHAR(64) NOT NULL COMMENT 'Visitor identifier (Cookie)',
    page_url VARCHAR(500) COMMENT 'Page URL',
    page_type VARCHAR(50) COMMENT 'Page type (list/detail)',
    referer VARCHAR(500) COMMENT 'Referrer URL',
    user_agent VARCHAR(500) COMMENT 'Browser info',
    ip_address VARCHAR(45) COMMENT 'IP address',
    visit_time DATETIME NOT NULL COMMENT 'Visit time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pv_visitor_time (visitor_id, visit_time),
    INDEX idx_pv_page_type_time (page_type, visit_time),
    INDEX idx_pv_visit_time (visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Page view records';

-- 5. Create click_logs table
CREATE TABLE IF NOT EXISTS click_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id INT NOT NULL COMMENT 'Announcement ID',
    company_id INT COMMENT 'Company ID',
    visitor_id VARCHAR(64) NOT NULL COMMENT 'Visitor identifier',
    click_type VARCHAR(20) NOT NULL COMMENT 'Click type (link/email)',
    ip_address VARCHAR(45) COMMENT 'IP address',
    click_time DATETIME NOT NULL COMMENT 'Click time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cl_announcement_time (announcement_id, click_time),
    INDEX idx_cl_company_time (company_id, click_time),
    INDEX idx_cl_click_time (click_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Click logs for apply button';

-- 6. Create admin_users table
CREATE TABLE IF NOT EXISTS admin_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(255) NOT NULL COMMENT 'Password (BCrypt encrypted)',
    email VARCHAR(100) COMMENT 'Email',
    status TINYINT DEFAULT 1 COMMENT 'Status (1=enabled, 0=disabled)',
    last_login_at TIMESTAMP NULL COMMENT 'Last login time',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Admin users';

-- 7. Insert default admin user (password: admin123, BCrypt encoded)
INSERT INTO admin_users (username, password, email, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@campus.com', 1);
