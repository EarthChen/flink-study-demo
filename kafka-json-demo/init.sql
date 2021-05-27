CREATE TABLE KafkaTable
(
    `user_id` BIGINT,
    `item_id` BIGINT,
    `behavior` STRING
)
WITH (
    'connector' = 'kafka',
    'topic' = 'user_behavior',
    'properties.bootstrap.servers' = '127.0.0.1:9000',
    'properties.group.id' = 'testGroup',
    'scan.startup.mode' = 'latest-offset',
    'format' = 'json'
)


CREATE TABLE kafka_sink_table
(
    `user_id` BIGINT,
    `item_id` BIGINT,
    `behavior` STRING
)
WITH (
    'connector' = 'jdbc',
    'url' = 'jdbc:mysql://127.0.0.1:3306/test',
    'username'= 'root'
    'password'= '123456'
    'table-name' = 'kafka_sink_table'
);

create table kafka_sink_table
(
    `id`       bigint primary key auto_increment,
    `user_id`  bigint not null,
    `item_id`  bigint not null,
    `behavior` varchar(256)
);