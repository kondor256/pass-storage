CREATE TABLE IF NOT EXISTS users_keys
(
    user_id varchar(255) PRIMARY KEY UNIQUE,
    priv_key bytea,
    pub_key bytea
);