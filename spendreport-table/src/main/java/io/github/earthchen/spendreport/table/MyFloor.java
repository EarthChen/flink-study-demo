package io.github.earthchen.spendreport.table;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author earthchen
 * @date 2021/5/12
 **/
public class MyFloor extends ScalarFunction {

    public @DataTypeHint("TIMESTAMP(3)") LocalDateTime eval(
            @DataTypeHint("TIMESTAMP(3)") LocalDateTime timestamp) {

        return timestamp.truncatedTo(ChronoUnit.HOURS);
    }
}