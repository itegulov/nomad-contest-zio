create table users
(
    user_id                 serial primary key,
    user_first_name         varchar(32)         not null,
    user_last_name          varchar(32)         not null,
    user_email              varchar(255) unique not null,
    user_encrypted_password char(60)            not null
);
