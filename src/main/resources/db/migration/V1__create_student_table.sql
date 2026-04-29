CREATE TABLE IF NOT EXISTS students (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    email              VARCHAR(255) NOT NULL UNIQUE,
    name               VARCHAR(255) NOT NULL,
    career_goal        VARCHAR(500),
    domain_interests   VARCHAR(1000),
    experience_level   ENUM('BEGINNER','INTERMEDIATE','ADVANCED'),
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
