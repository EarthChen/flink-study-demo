package io.github.earthchen.ride.alert;

import io.github.earthchen.ride.common.domain.TaxiRide;
import io.github.earthchen.ride.common.source.TaxiRideGenerator;
import io.github.earthchen.ride.common.utils.ExerciseBase;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @author earthchen
 * @date 2021/5/25
 **/
public class LongRidesExercise extends ExerciseBase {

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

        DataStream<TaxiRide> longRides = rides
                .keyBy((TaxiRide ride) -> ride.rideId)
                .process(new MatchFunction());

        printOrTest(longRides);

        env.execute("Long Taxi Rides");
    }

    private static class MatchFunction extends KeyedProcessFunction<Long, TaxiRide, TaxiRide> {

        private ValueState<TaxiRide> rideState;

        @Override
        public void open(Configuration config) {
            ValueStateDescriptor<TaxiRide> stateDescriptor =
                    new ValueStateDescriptor<>("ride event", TaxiRide.class);
            rideState = getRuntimeContext().getState(stateDescriptor);
        }

        @Override
        public void processElement(TaxiRide ride, Context context, Collector<TaxiRide> out) throws Exception {
            TaxiRide previousRideEvent = rideState.value();

            if (previousRideEvent == null) {
                rideState.update(ride);
                if (ride.isStart) {
                    context.timerService().registerEventTimeTimer(getTimerTime(ride));
                }
            } else {
                if (!ride.isStart) {
                    // it's an END event, so event saved was the START event and has a timer
                    // the timer hasn't fired yet, and we can safely kill the timer
                    context.timerService().deleteEventTimeTimer(getTimerTime(previousRideEvent));
                }
                // both events have now been seen, we can clear the state
                rideState.clear();
            }
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext context, Collector<TaxiRide> out) throws Exception {

            // if we get here, we know that the ride started two hours ago, and the END hasn't been processed
            out.collect(rideState.value());
            rideState.clear();
        }

        private long getTimerTime(TaxiRide ride) {
            return ride.startTime.plusSeconds(120 * 60).toEpochMilli();
        }
    }
}
