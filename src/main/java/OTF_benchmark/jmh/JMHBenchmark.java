package OTF_benchmark.jmh;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JMHBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(TVJMHBenchmark.class.getSimpleName())
                                                .include(WalnutJMHBenchmark.class.getSimpleName())
                                                .addProfiler(GCProfiler.class)
                                                .resultFormat(ResultFormatType.CSV)
                                                .result("results.csv")
                                                .build();
        final Runner runner = new Runner(opt);

        runner.run();
    }
}
