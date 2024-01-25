CREATE TABLE IF NOT EXISTS app_users
(
    id varchar(255) PRIMARY KEY UNIQUE,
    name varchar(255)
);

CREATE TABLE IF NOT EXISTS password_folders
(
    id varchar(255) PRIMARY KEY UNIQUE,
    name varchar(255),
    folder_id varchar(255) CONSTRAINT folders_foreign_key1 REFERENCES password_folders (id)
);

CREATE TABLE IF NOT EXISTS stored_passwords
(
    id varchar(255) PRIMARY KEY UNIQUE,
    description text,
    url text,
    login varchar(255),
    password varchar(255),

    owner_user_id varchar(255) CONSTRAINT password_foreign_key1 REFERENCES app_users (id),
    folder_id varchar(255) CONSTRAINT password_foreign_key2 REFERENCES password_folders (id)
);

CREATE TABLE IF NOT EXISTS shared_passwords
(
    user_id varchar(255) CONSTRAINT shared_passwords_foreign_key1 REFERENCES app_users (id),
    password_id varchar(255) CONSTRAINT shared_passwords_foreign_key2 REFERENCES stored_passwords (id)
);
