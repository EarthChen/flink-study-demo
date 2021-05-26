package io.github.earthchen.hourly.tips;

import io.github.earthchen.ride.common.domain.TaxiFare;
import io.github.earthchen.ride.common.source.TaxiFareGenerator;
import io.github.earthchen.ride.common.utils.ExerciseBase;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @author earthchen
 * @date 2021/5/24
 **/
public class HourlyTipsExercise extends ExerciseBase {

    private static final OutputTag<TaxiFare> lateFares = new OutputTag<TaxiFare>("lateFares") {};



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
        // 计算每个司机每小时的小费总和
        SingleOutputStreamOperator<Tuple3<Long, Long, Float>> hourlyTips = fares
                .keyBy((TaxiFare fare) -> fare.driverId)
                // .window(TumblingEventTimeWindows.of(Time.hours(1)))
                // .process(new AddTips())
                .process(new PseudoWindow(Time.hours(1)));

        DataStream<Tuple3<Long, Long, Float>> hourlyMax = hourlyTips
                .windowAll(TumblingEventTimeWindows.of(Time.hours(1)))
                .maxBy(2);

        hourlyTips.getSideOutput(lateFares).print();

//		You should explore how this alternative behaves. In what ways is the same as,
//		and different from, the solution above (using a windowAll)?

// 		DataStream<Tuple3<Long, Long, Float>> hourlyMax = hourlyTips
// 			.keyBy(t -> t.f0)
// 			.maxBy(2);

        printOrTest(hourlyMax);
        hourlyTips.getSideOutput(lateFares).print();
        // printOrTest(hourlyTips);

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


    // 在时长跨度为一小时的窗口中计算每个司机的小费总和。
// 司机ID作为 key。
    public static class PseudoWindow extends
            KeyedProcessFunction<Long, TaxiFare, Tuple3<Long, Long, Float>> {

        private final long durationMsec;

        // 每个窗口都持有托管的 Keyed state 的入口，并且根据窗口的结束时间执行 keyed 策略。
        // 每个司机都有一个单独的MapState对象。
        private MapState<Long, Float> sumOfTips;

        public PseudoWindow(Time duration) {
            this.durationMsec = duration.toMilliseconds();
        }

        @Override
        // 在初始化期间调用一次。
        public void open(Configuration conf) {
            MapStateDescriptor<Long, Float> sumDesc =
                    new MapStateDescriptor<>("sumOfTips", Long.class, Float.class);
            sumOfTips = getRuntimeContext().getMapState(sumDesc);
        }

        @Override
        // 每个票价事件（TaxiFare-Event）输入（到达）时调用，以处理输入的票价事件。
        public void processElement(
                TaxiFare fare,
                Context ctx,
                Collector<Tuple3<Long, Long, Float>> out) throws Exception {
            long eventTime = fare.getEventTime();
            TimerService timerService = ctx.timerService();


            if (eventTime <= timerService.currentWatermark()) {
                // 事件延迟；其对应的窗口已经触发。
                ctx.output(lateFares, fare);
            } else {
                // 将 eventTime 向上取值并将结果赋值到包含当前事件的窗口的末尾时间点。
                long endOfWindow = (eventTime - (eventTime % durationMsec) + durationMsec - 1);

                // 在窗口完成时将启用回调
                timerService.registerEventTimeTimer(endOfWindow);

                // 将此票价的小费添加到该窗口的总计中。
                Float sum = sumOfTips.get(endOfWindow);
                if (sum == null) {
                    sum = 0.0F;
                }
                sum += fare.tip;
                sumOfTips.put(endOfWindow, sum);
            }
        }

        @Override
        // 当当前水印（watermark）表明窗口现在需要完成的时候调用。
        public void onTimer(long timestamp,
                            OnTimerContext context,
                            Collector<Tuple3<Long, Long, Float>> out) throws Exception {

            long driverId = context.getCurrentKey();
            // 查找刚结束的一小时结果。
            Float sumOfTips = this.sumOfTips.get(timestamp);

            Tuple3<Long, Long, Float> result = Tuple3.of(driverId, timestamp, sumOfTips);
            out.collect(result);
            this.sumOfTips.remove(timestamp);
        }
    }
}
