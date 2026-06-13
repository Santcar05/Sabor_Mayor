-- Test users, one per role. Passwords (dev only):
--   superadmin@sabormayor.com / Admin123!
--   admin@sabormayor.com      / Admin123!
--   mesero@sabormayor.com     / Mesero123!
--   cocinero@sabormayor.com   / Cocina123!
--   cliente@sabormayor.com    / Cliente123!
INSERT INTO users (id, email, password_hash, full_name, role, provider, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'superadmin@sabormayor.com',
     '$2a$10$RBKQXvSyR1hJy0zH/8hYWeqVSXGa//0eB6LRONkaHwT2Er2AAnRsy', 'Super Administrador', 'SUPER_ADMIN', 'LOCAL', TRUE),
    ('00000000-0000-0000-0000-000000000002', 'admin@sabormayor.com',
     '$2a$10$vlvo6.vvWlT5.2jfkZTto.exdW4sITE9ugMXYFW6LLNt2Ct2u1hdy', 'Administrador General', 'ADMIN', 'LOCAL', TRUE),
    ('00000000-0000-0000-0000-000000000003', 'mesero@sabormayor.com',
     '$2a$10$Vpjgil4Nr28VTpMbFnDmqejOi4akPZKPbssTRkKhtXLLXdpOjSeui', 'Mario Mesero', 'MESERO', 'LOCAL', TRUE),
    ('00000000-0000-0000-0000-000000000004', 'cocinero@sabormayor.com',
     '$2a$10$aUskAhIOxIM9oUWpX58eJe1s6B6qUtvwnwwtOtFVBdZLRxJwp7WQO', 'Carla Cocinera', 'COCINERO', 'LOCAL', TRUE),
    ('00000000-0000-0000-0000-000000000005', 'cliente@sabormayor.com',
     '$2a$10$zGdOvPWs1lYu8Jxf6oPGx.hSjFZV.fWPXsVSJiZWxjmLhol2uAGOa', 'Clara Cliente', 'CLIENTE', 'LOCAL', TRUE);
