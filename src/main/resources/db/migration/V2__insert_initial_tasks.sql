INSERT INTO tasks (title, description, completed, created_at, updated_at) VALUES
('Learn SOLID principles', 'Study SRP, OCP, LSP, ISP, DIP and apply them in this project', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Refactor TaskController', 'Extract business logic from controller to dedicated services', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Add unit tests', 'Cover service layer with unit tests using mocks', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Setup PostgreSQL', 'Docker Compose and Flyway migrations', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
