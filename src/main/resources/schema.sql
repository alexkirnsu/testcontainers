CREATE TABLE customers
(id      INT         generated as identity NOT NULL ,
name     VARCHAR2(45) NOT NULL,
email    VARCHAR2(45) NOT NULL,
constraint users_pk PRIMARY KEY(id)
);