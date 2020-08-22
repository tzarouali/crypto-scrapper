create table coin(
    id serial primary key,
    symbol varchar(50) not null,
    name varchar(50) not null,
    unique(symbol)
);

create table coin_details(
    coin_id integer not null,
    rank integer not null,
    price_usd decimal not null,
    created timestamp with time zone not null,
    constraint coin_fk foreign key (coin_id) references coin(id),
    unique(coin_id, created)
);


