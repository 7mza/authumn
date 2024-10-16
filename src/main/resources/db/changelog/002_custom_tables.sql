create table if not exists key_pairs
(
    created_at  timestamp(6) with time zone not null,
    update_at   timestamp(6) with time zone not null,
    id          varchar(255)                not null,
    private_key text                        not null,
    public_key  text                        not null,
    primary key (id)
);

create table if not exists privileges
(
    is_default boolean default false       not null,
    created_at timestamp(6) with time zone not null,
    update_at  timestamp(6) with time zone not null,
    id         varchar(255)                not null,
    label      varchar(255)                not null,
    primary key (id),
    constraint privilege_label_unique
        unique (label)
);

create table if not exists roles
(
    is_default boolean default false       not null,
    created_at timestamp(6) with time zone not null,
    update_at  timestamp(6) with time zone not null,
    id         varchar(255)                not null,
    label      varchar(255)                not null,
    primary key (id),
    constraint role_label_unique
        unique (label)
);

create table if not exists roles_privileges
(
    privilege_id varchar(255) not null,
    role_id      varchar(255) not null,
    constraint fk5duhoc7rwt8h06avv41o41cfy
        foreign key (privilege_id) references privileges,
    constraint fk629oqwrudgp5u7tewl07ayugj
        foreign key (role_id) references roles
);

create table if not exists users
(
    created_at timestamp(6) with time zone not null,
    update_at  timestamp(6) with time zone not null,
    email      varchar(255)                not null,
    id         varchar(255)                not null,
    password   text                        not null,
    primary key (id),
    constraint user_email_unique
        unique (email)
);

create table if not exists users_roles
(
    role_id varchar(255) not null,
    user_id varchar(255) not null,
    constraint fkj6m8fwv7oqv74fcehir1a9ffy
        foreign key (role_id) references roles,
    constraint fk2o0jvgh89lemvvo17cbqvdxaa
        foreign key (user_id) references users
);
