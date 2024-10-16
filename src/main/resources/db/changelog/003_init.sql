INSERT INTO privileges (is_default, created_at, update_at, id, label)
VALUES (true, current_timestamp, current_timestamp,
        '62ba6d54-a7ff-450b-b52b-8deb2aff25e3', 'read');

INSERT INTO privileges (is_default, created_at, update_at, id, label)
VALUES (false, current_timestamp, current_timestamp,
        '5dac90d4-6b48-48b6-bded-7a5ef03f87f3', 'write');

INSERT INTO roles (is_default, created_at, update_at, id, label)
VALUES (false, current_timestamp, current_timestamp,
        '75614f4b-53e1-4198-bd67-aeb7a35f34d8', 'admin');

INSERT INTO roles (is_default, created_at, update_at, id, label)
VALUES (true, current_timestamp, current_timestamp,
        'd47712f9-5c05-435b-bedb-2b449bb152af', 'user');

INSERT INTO users (created_at, update_at, email, id, password)
VALUES (current_timestamp, current_timestamp, 'admin@mail.com',
        '985d08a7-0960-4c57-863c-d19647576559', '{bcrypt}$2a$10$EWxPQRePbB.0TjYwgHSNQe1m2v0wBpGZlsyyDR6RfIft602PAOAi2');

INSERT INTO users (created_at, update_at, email, id, password)
VALUES (current_timestamp, current_timestamp, 'user@mail.com',
        '6acef69b-4549-4e2a-83e6-95dcb41f0acb', '{bcrypt}$2a$10$c5kV1TUmmW3ggGM9RiXGyO798S1WLpKSX33LLDEpMDAbT01gVkueu');

INSERT INTO roles_privileges (privilege_id, role_id)
VALUES ('62ba6d54-a7ff-450b-b52b-8deb2aff25e3', '75614f4b-53e1-4198-bd67-aeb7a35f34d8');

INSERT INTO roles_privileges (privilege_id, role_id)
VALUES ('5dac90d4-6b48-48b6-bded-7a5ef03f87f3', '75614f4b-53e1-4198-bd67-aeb7a35f34d8');

INSERT INTO roles_privileges (privilege_id, role_id)
VALUES ('62ba6d54-a7ff-450b-b52b-8deb2aff25e3', 'd47712f9-5c05-435b-bedb-2b449bb152af');

INSERT INTO users_roles (role_id, user_id)
VALUES ('d47712f9-5c05-435b-bedb-2b449bb152af', '985d08a7-0960-4c57-863c-d19647576559');

INSERT INTO users_roles (role_id, user_id)
VALUES ('75614f4b-53e1-4198-bd67-aeb7a35f34d8', '985d08a7-0960-4c57-863c-d19647576559');

INSERT INTO users_roles (role_id, user_id)
VALUES ('d47712f9-5c05-435b-bedb-2b449bb152af', '6acef69b-4549-4e2a-83e6-95dcb41f0acb');
