INSERT INTO skills (name, description, category, parent_skill, difficulty_level) VALUES
-- Programming Languages
('Java', 'Object-oriented programming language', 'Programming', NULL, 1),
('Python', 'High-level scripting and data science language', 'Programming', NULL, 1),
('JavaScript', 'Web scripting language', 'Programming', NULL, 1),
('TypeScript', 'Typed superset of JavaScript', 'Programming', 'JavaScript', 2),
('SQL', 'Structured Query Language for databases', 'Database', NULL, 1),

-- Backend
('Spring Boot', 'Java-based backend framework', 'Backend', 'Java', 2),
('Spring Security', 'Security framework for Spring', 'Backend', 'Spring Boot', 3),
('REST API', 'RESTful API design and implementation', 'Backend', NULL, 2),
('Microservices', 'Distributed microservice architecture', 'Backend', NULL, 3),
('JWT', 'JSON Web Token authentication', 'Backend', 'Spring Security', 2),
('WebSockets', 'Real-time bidirectional communication', 'Backend', NULL, 2),
('AOP', 'Aspect-Oriented Programming', 'Backend', 'Spring Boot', 3),
('Webhooks', 'Event-driven HTTP callbacks', 'Backend', 'REST API', 2),

-- Frontend
('React', 'JavaScript UI library', 'Frontend', 'JavaScript', 2),
('CSS', 'Cascading stylesheets', 'Frontend', NULL, 1),
('Tailwind CSS', 'Utility-first CSS framework', 'Frontend', 'CSS', 2),
('D3.js', 'Data visualization library', 'Frontend', 'JavaScript', 3),
('Canvas API', 'HTML5 canvas rendering', 'Frontend', 'JavaScript', 3),
('Chart.js', 'Simple charting library', 'Frontend', 'JavaScript', 2),

-- AI / ML
('OpenAI API', 'Integration with OpenAI LLM services', 'AI', NULL, 2),
('Machine Learning', 'Supervised/unsupervised learning fundamentals', 'AI', NULL, 3),
('Vector Search', 'Semantic similarity search with embeddings', 'AI', NULL, 3),
('Hugging Face', 'Pre-trained transformer models', 'AI', 'Machine Learning', 3),
('Scikit-learn', 'Python ML library', 'AI', 'Python', 2),
('NLP', 'Natural Language Processing', 'AI', 'Machine Learning', 3),

-- Database
('MySQL', 'Relational database management system', 'Database', 'SQL', 1),
('Redis', 'In-memory data store and cache', 'Database', NULL, 2),
('JDBC', 'Java Database Connectivity', 'Database', 'Java', 2),
('Performance Tuning', 'Database query and index optimization', 'Database', 'SQL', 3),

-- DevOps
('Docker', 'Containerization platform', 'DevOps', NULL, 2),
('Kubernetes', 'Container orchestration system', 'DevOps', 'Docker', 3),
('GitHub API', 'GitHub REST and GraphQL API', 'DevOps', NULL, 2),
('AWS', 'Amazon Web Services cloud platform', 'DevOps', NULL, 3),
('CI/CD', 'Continuous integration and deployment', 'DevOps', NULL, 2),

-- Security
('Cryptography', 'Encryption and hashing fundamentals', 'Security', NULL, 3),
('OAuth2', 'Authorization framework', 'Security', 'Spring Security', 3),
('AES Encryption', 'Advanced Encryption Standard', 'Security', 'Cryptography', 3),

-- System Design
('Distributed Systems', 'Design of distributed software systems', 'System Design', NULL, 4),
('Caching', 'Data caching strategies and patterns', 'System Design', NULL, 2),
('Design Patterns', 'Software design pattern knowledge', 'System Design', NULL, 2),
('Concurrency', 'Multithreading and parallel programming', 'System Design', 'Java', 3),
('Networking', 'TCP/IP and networking fundamentals', 'System Design', NULL, 3),
('Blockchain', 'Distributed ledger technology', 'System Design', NULL, 4),

-- General
('File I/O', 'File reading and writing operations', 'General', NULL, 1),
('JSON', 'JSON data format and parsing', 'General', NULL, 1),
('CLI', 'Command-line interface development', 'General', NULL, 1);
