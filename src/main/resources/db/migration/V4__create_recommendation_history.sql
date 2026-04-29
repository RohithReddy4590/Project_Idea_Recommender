CREATE TABLE IF NOT EXISTS recommendation_history (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id            BIGINT NOT NULL,
    project_id            BIGINT NOT NULL,
    recommendation_score  DOUBLE,
    skill_match_score     DOUBLE,
    semantic_score        DOUBLE,
    llm_score             DOUBLE,
    explanation           TEXT,
    skill_gaps            VARCHAR(1000),
    feedback              ENUM('LIKED','DISLIKED','NEUTRAL','NOT_REVIEWED') DEFAULT 'NOT_REVIEWED',
    session_id            VARCHAR(255),
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rh_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_rh_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_rh_student   (student_id),
    INDEX idx_rh_session   (session_id),
    INDEX idx_rh_created   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
