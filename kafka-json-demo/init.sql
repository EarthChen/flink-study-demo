CREATE TABLE KafkaTable (
                            `user_id` BIGINT,
                            `item_id` BIGINT,
                            `behavior` STRING,
                            `ts` TIMESTAMP(3) METADATA FROM 'timestamp'
) WITH (
    'connector' = 'kafka',
    'topic' = 'user_behavior',
    'properties.bootstrap.servers' = '123.57.82.220:31090,123.57.82.220:31091,123.57.82.220:31092',
    'properties.group.id' = 'testGroup',
    'scan.startup.mode' = 'latest-offset',
    'format' = 'json'
)