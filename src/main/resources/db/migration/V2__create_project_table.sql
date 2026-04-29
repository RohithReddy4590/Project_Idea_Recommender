CREATE TABLE IF NOT EXISTS projects (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    tech_stack              VARCHAR(1000),
    difficulty              ENUM('BEGINNER','INTERMEDIATE','ADVANCED'),
    skills_required         VARCHAR(1000),
    domain                  VARCHAR(255),
    learning_outcomes       TEXT,
    estimated_hours         INT,
    portfolio_impact_score  DOUBLE DEFAULT 5.0,
    embedding_vector        LONGTEXT,
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
