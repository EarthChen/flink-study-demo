package io.github.earthchen.ride.common;

import io.github.earthchen.ride.common.domain.TaxiRide;
import io.github.earthchen.ride.common.source.TaxiRideGenerator;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author earthchen
 * @date 2021/5/17
 **/
public class RideCountExample {

    public static void main(String[] args) throws Exception {

        // set up streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // start the data generator
        DataStream<TaxiRide> rides = env.addSource(new TaxiRideGenerator());

        // map each ride to a tuple of (driverId, 1)
        DataStream<Tuple2<Long, Long>> tuples =
                rides.map((MapFunction<TaxiRide, Tuple2<Long, Long>>) ride -> Tuple2.of(ride.driverId, 1L));

        // partition the stream by the driverId
        KeyedStream<Tuple2<Long, Long>, Long> keyedByDriverId = tuples.keyBy(t -> t.f0);

        // count the rides for each driver
        DataStream<Tuple2<Long, Long>> rideCounts = keyedByDriverId.sum(1);

        // we could, in fact, print out any or all of these streams
        rideCounts.print();

        // run the cleansing pipeline
        env.execute("Ride Count");
    }
}
