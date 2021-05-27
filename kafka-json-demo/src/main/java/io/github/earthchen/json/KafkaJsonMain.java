package io.github.earthchen.json;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;

import static org.apache.flink.table.api.Expressions.$;


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
                "    'properties.bootstrap.servers' = '127.0.0.1'," +
                "\n" +
                "    'properties.group.id' = 'testGroup',\n" +
                "    'scan.startup.mode' = 'group-offsets',\n" +
                "    'json.fail-on-missing-field' = 'false'," +
                "    'json.ignore-parse-errors' = 'true'," +
                "    'format' = 'json'\n" +
                ")");
        Table kafkaJsonSource = tEnv.from("kafka_source");
        tEnv.executeSql("CREATE TABLE print_table WITH ('connector' = 'print')\n" +
                "LIKE kafka_source (EXCLUDING ALL)");

        tEnv.executeSql("CREATE TABLE kafka_sink_table\n" +
                "(\n" +
                "    `user_id` BIGINT,\n" +
                "    `item_id` BIGINT,\n" +
                "    `behavior` STRING\n" +
                ")\n" +
                "WITH (\n" +
                "    'connector' = 'jdbc',\n" +
                "    'url' = 'jdbc:mysql://127.0.0.1:3306/test',\n" +
                "    'username'= 'root',\n" +
                "    'password'= '123456',\n" +
                "    'table-name' = 'kafka_sink_table'\n" +
                ")");
        // tEnv.executeSql("select * from kafka_source").print();

        kafkaJsonSource.select($("user_id"),
                $("item_id"),
                $("behavior")).executeInsert("kafka_sink_table");

    }
}
