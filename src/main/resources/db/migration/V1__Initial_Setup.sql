create table users
(
    user_id                 serial primary key,
    user_first_name         varchar(32)         not null,
    user_last_name          varchar(32)         not null,
    user_email              varchar(255) unique not null,
    user_encrypted_password char(60)            not null
);

create table jwt_tokens
(
    id           varchar primary key,
    jwt          varchar                            not null,
    user_id      integer references users (user_id) not null,
    expiry       timestamp                          not null,
    last_touched timestamp
);
