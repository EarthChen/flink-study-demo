package io.github.earthchen.ride.cleaning;

import io.github.earthchen.ride.common.domain.TaxiRide;
import io.github.earthchen.ride.common.source.TaxiRideGenerator;
import io.github.earthchen.ride.common.utils.ExerciseBase;
import io.github.earthchen.ride.common.utils.GeoUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author earthchen
 * @date 2021/5/17
 **/
public class RideCleansingExercise extends ExerciseBase {
    /**
     * Main method.
     *
     * @throws Exception which occurs during job execution.
     */
    public static void main(String[] args) throws Exception {

        // set up streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(ExerciseBase.parallelism);
        // start the data generator
        DataStream<TaxiRide> rides = env.addSource(rideSourceOrTest(new TaxiRideGenerator()));

        DataStream<TaxiRide> filteredRides = rides
                // filter out rides that do not start or stop in NYC
                .filter(new NYCFilter());

        // print the filtered stream
        printOrTest(filteredRides);

        // run the cleansing pipeline
        env.execute("Taxi Ride Cleansing");
    }

    private static class NYCFilter implements FilterFunction<TaxiRide> {

        @Override
        public boolean filter(TaxiRide taxiRide) throws Exception {
            return GeoUtils.isInNYC(taxiRide.startLon, taxiRide.startLat) &&
                    GeoUtils.isInNYC(taxiRide.endLon, taxiRide.endLat);
        }
    }

}