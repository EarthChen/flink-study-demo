package io.github.earthchen.hourly.tips;

import io.github.earthchen.ride.common.domain.TaxiFare;
import io.github.earthchen.ride.common.source.TaxiFareGenerator;
import io.github.earthchen.ride.common.utils.ExerciseBase;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author earthchen
 * @date 2021/5/24
 **/
public class HourlyTipsExercise extends ExerciseBase {

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
        DataStream<TaxiFare> fares = env.addSource(fareSourceOrTest(new TaxiFareGenerator()));

        // compute tips per hour for each driver
        DataStream<Tuple3<Long, Long, Float>> hourlyTips = fares
                .keyBy((TaxiFare fare) -> fare.driverId)
                .window(TumblingEventTimeWindows.of(Time.hours(1)))
                .process(new AddTips());

        DataStream<Tuple3<Long, Long, Float>> hourlyMax = hourlyTips
                .windowAll(TumblingEventTimeWindows.of(Time.hours(1)))
                .maxBy(2);

//		You should explore how this alternative behaves. In what ways is the same as,
//		and different from, the solution above (using a windowAll)?

// 		DataStream<Tuple3<Long, Long, Float>> hourlyMax = hourlyTips
// 			.keyBy(t -> t.f0)
// 			.maxBy(2);

        printOrTest(hourlyMax);

        // execute the transformation pipeline
        env.execute("Hourly Tips (java)");
    }

    /*
     * Wraps the pre-aggregated result into a tuple along with the window's timestamp and key.
     */
    public static class AddTips extends ProcessWindowFunction<TaxiFare, Tuple3<Long, Long, Float>, Long, TimeWindow> {

        @Override
        public void process(Long key, Context context, Iterable<TaxiFare> fares,
                            Collector<Tuple3<Long, Long, Float>> out) {
            float sumOfTips = 0F;
            for (TaxiFare f : fares) {
                sumOfTips += f.tip;
            }
            out.collect(Tuple3.of(context.window().getEnd(), key, sumOfTips));
        }
    }
}
