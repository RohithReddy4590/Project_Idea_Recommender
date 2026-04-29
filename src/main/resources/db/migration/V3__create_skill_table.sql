CREATE TABLE IF NOT EXISTS skills (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(255) NOT NULL UNIQUE,
    description      VARCHAR(500),
    category         VARCHAR(100),
    parent_skill     VARCHAR(255),
    difficulty_level INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    description     VARCHAR(500),
    type            VARCHAR(50),
    parent_category VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student_skills (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id        BIGINT NOT NULL,
    skill_id          BIGINT NOT NULL,
    proficiency_level ENUM('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT'),
    years_experience  DOUBLE DEFAULT 0.0,
    added_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ss_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_ss_skill   FOREIGN KEY (skill_id)   REFERENCES skills(id)   ON DELETE CASCADE,
    UNIQUE KEY uq_student_skill (student_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project_embeddings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL UNIQUE,
    embedding_text  TEXT,
    embedding_vector LONGTEXT NOT NULL,
    embedding_model VARCHAR(100),
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pe_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
