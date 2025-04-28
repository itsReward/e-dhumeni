-- Add this to your migration script
INSERT INTO users (id, username, password, email, full_name, role, enabled, created_at, updated_at)
VALUES (
           gen_random_uuid(),
           'admin',
           -- This is BCrypt encoded 'password'
           '$2a$10$x5EQVSMwDAt9DFrUJUBTAe45JZn0.XBtK3CbZRdBpKGxhkA9Uw1Wu',
           'admin@edhumeni.com',
           'System Administrator',
           'ADMIN',
           true,
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );