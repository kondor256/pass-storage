ALTER TABLE password_folders
ADD owner_user_id varchar(255) CONSTRAINT password_foreign_key1 REFERENCES app_users (id);