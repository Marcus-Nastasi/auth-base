DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    user_status VARCHAR(100) NOT NULL,
    user_role VARCHAR(100) NOT NULL,
    user_type VARCHAR(100),
    birth_date DATE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inactivated_at TIMESTAMP WITH TIME ZONE
);

INSERT INTO users (
    id,
    email,
    cpf,
    password,
    first_name,
    last_name,
    user_status,
    user_role,
    user_type,
    birth_date
) VALUES (
    'a2d23f2f-465d-44fb-9c6e-c4884690399d',
     'vinnie.nstu@gmail.com',
     '43743508889',
     '12345',
     'Joao',
     'Felipe',
     'ACTIVE',
     'USER',
     null,
     '1974-04-23'
);
