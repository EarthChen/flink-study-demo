package io.github.earthchen.ride.common.utils;

import io.github.earthchen.ride.common.domain.TaxiFare;
import io.github.earthchen.ride.common.domain.TaxiRide;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @author earthchen
 * @date 2021/5/17
 **/
public class ExerciseBase {
    public static SourceFunction<TaxiRide> rides = null;
    public static SourceFunction<TaxiFare> fares = null;
    public static SourceFunction<String> strings = null;
    public static SinkFunction out = null;
    public static int parallelism = 4;

    /**
     * Retrieves a test source during unit tests and the given one during normal execution.
     */
    public static SourceFunction<TaxiRide> rideSourceOrTest(SourceFunction<TaxiRide> source) {
        if (rides == null) {
            return source;
        }
        return rides;
    }

    /**
     * Retrieves a test source during unit tests and the given one during normal execution.
     */
    public static SourceFunction<TaxiFare> fareSourceOrTest(SourceFunction<TaxiFare> source) {
        if (fares == null) {
            return source;
        }
        return fares;
    }

    /**
     * Retrieves a test source during unit tests and the given one during normal execution.
     */
    public static SourceFunction<String> stringSourceOrTest(SourceFunction<String> source) {
        if (strings == null) {
            return source;
        }
        return strings;
    }

    /**
     * Prints the given data stream during normal execution and collects outputs during tests.
     */
    public static void printOrTest(org.apache.flink.streaming.api.datastream.DataStream<?> ds) {
        if (out == null) {
            ds.print();
        } else {
            ds.addSink(out);
        }
    }

    // /**
    //  * Prints the given data stream during normal execution and collects outputs during tests.
    //  */
    // public static void printOrTest(org.apache.flink.streaming.api.scala.DataStream<?> ds) {
    //     if (out == null) {
    //         ds.print();
    //     } else {
    //         ds.addSink(out);
    //     }
    // }
}