ALTER TABLE stored_passwords
    ADD name varchar(255);

UPDATE stored_passwords
    SET name = description;