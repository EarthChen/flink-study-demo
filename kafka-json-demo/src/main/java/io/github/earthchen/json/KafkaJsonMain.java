package io.github.earthchen.json;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;


/**
 * @author earthchen
 * @date 2021/5/26
 **/
public class KafkaJsonMain {

    public static void main(String[] args) throws Exception {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().build();
        // ExecutionEnvironment executionEnvironment = ExecutionEnvironment.getExecutionEnvironment();
        TableEnvironment tEnv = TableEnvironment.create(settings);
        tEnv.executeSql("CREATE TABLE kafka_source (\n" +
                "                            `user_id` BIGINT,\n" +
                "                            `item_id` BIGINT,\n" +
                "                            `behavior` STRING\n" +
                // "                            `ts` TIMESTAMP(3) METADATA FROM 'timestamp'\n" +
                ") WITH (\n" +
                "    'connector' = 'kafka',\n" +
                "    'topic' = 'user_behavior1',\n" +
                "    'properties.bootstrap.servers' = '123.57.82.220:31090,123.57.82.220:31091,123.57.82.220:31092'," +
                "\n" +
                "    'properties.group.id' = 'testGroup',\n" +
                "    'scan.startup.mode' = 'latest-offset',\n" +
                "    'json.fail-on-missing-field' = 'false'," +
                "    'json.ignore-parse-errors' = 'true'," +
                "    'format' = 'json'\n" +
                ")");
        Table kafkaJsonSource = tEnv.from("kafka_source");
        tEnv.executeSql("CREATE TABLE print_table WITH ('connector' = 'print')\n" +
                "LIKE kafka_source (EXCLUDING ALL)");
        tEnv.executeSql("select user_id as UserId from kafka_source").print();
        // kafkaJsonSource.select($("user_id").as("userId"),
        //         $("item_id"),
        //         $("behavior")).executeInsert("print_table");

        // tEnv.executeSql("select * from kafka_source").print();
        //
        // tEnv.executeSql("CREATE TABLE kafka_source (" +
        //         "    funcName STRING," +
        //         "    data ROW<snapshots ARRAY<ROW<content_type STRING,url STRING>>,audio ARRAY<ROW<content_type " +
        //         "STRING,url STRING>>>," +
        //         "    resultMap ROW<`result` MAP<STRING,STRING>,isSuccess BOOLEAN>," +
        //         "    meta  MAP<STRING,STRING>," +
        //         "    `type` INT," +
        //         "    `timestamp` BIGINT," +
        //         "    arr ARRAY<ROW<address STRING,city STRING>>," +
        //         "    map MAP<STRING,INT>," +
        //         "    doublemap MAP<STRING,MAP<STRING,INT>>," +
        //         "    proctime as PROCTIME()" +
        //         ") WITH (" +
        //         "    'connector' = 'kafka'," +
        //         "    'topic' = 'test_kafka_json'," +
        //         "    'properties.bootstrap.servers' = '123.57.82.220:31090,123.57.82.220:31091,123.57.82
        //         .220:31092'," +
        //         "    'properties.group.id' = 'test_kafka_json'," +
        //         "    'scan.startup.mode' = 'latest-offset'," +
        //         "    'format' = 'json'," +
        //         "    'json.fail-on-missing-field' = 'true'," +
        //         "    'json.ignore-parse-errors' = 'false'" +
        //         ")");
        //
        // Table kafkaJsonSource = tEnv.from("kafka_source");
        // tEnv.executeSql("select * from kafka_source").print();
        // tEnv.executeSql("select" +
        //         "funcName," +
        //         "doublemap['inner_map']['key']," +
        //         // "count(data.snapshots[1].url)," +
        //         "`type`," +
        //         "TUMBLE_START(proctime, INTERVAL '30' second) as t_start" +
        //         "from kafka_source" +
        //         "group by TUMBLE(proctime, INTERVAL '30' second),funcName,`type`,doublemap['inner_map']['key']")
        //         .print();
    }
}
