package OTF_benchmark;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import OTF.Registry.AntichainForestRegistry;
import OTF.Registry.Registry;
import OTF.Model.Threshold;
import net.automatalib.automaton.fsa.NFA;
import org.openjdk.jmh.runner.RunnerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {

    private final static Logger LOGGER = LoggerFactory.getLogger(Example.class);

    final static int PARAM_MIN = 5_000;
    final static int PARAM_MAX = 5_000;
    final static int PARAM_INC = 5_000;

    public static void main(String[] args) throws RunnerException, InterruptedException {

        final int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        final List<IBench> benchmarks = new ArrayList<>();

        final List<BiFunction<NFA<?, Integer>, BitSet[], Registry>> indices = List.of(AntichainForestRegistry::new);
        final List<IntFunction<Threshold>> thresholds = List.of(
                //Threshold::maxSteps,
                //Threshold::maxInc,
                Threshold::adaptiveSteps);
        final List<IInput> inputs = List.of(new InputDebug());

        for (int i = PARAM_MAX; i >= PARAM_MIN; i -= PARAM_INC) {
            for (IInput input : inputs) {
                for (BiFunction<NFA<?, Integer>, BitSet[], Registry> index : indices) {
                    for (IntFunction<Threshold> threshold : thresholds) {
                        final int param = i;
//                        benchmarks.add(new BenchmarkOTF(input, () -> threshold.apply(param), true, false, index));
//                        benchmarks.add(new BenchmarkOTFBrz(input, () -> threshold.apply(param), true, false, index));
//                        benchmarks.add(new BenchmarkOTF(input, () -> threshold.apply(param), true, true, index));
//                        benchmarks.add(new BenchmarkOTFBrz(input, () -> threshold.apply(param), true, true, index));
                        benchmarks.add(new BenchmarkOTF2(input, () -> threshold.apply(param), true, true, index));
                    }
                }
//                benchmarks.add(new BenchmarkSC(input));
//                benchmarks.add(new BenchmarkBrz(input));
//                benchmarks.add(new BenchmarkSimSC(input));
//                benchmarks.add(new BenchmarkSimBrz(input));
            }
        }

        ExecutorService warmupService = Executors.newFixedThreadPool(threads);

        for (IBench benchmark : benchmarks) {
            benchmark.warmups().forEachRemaining(warmupService::execute);
        }

        warmupService.shutdown();
        boolean warmupTerminated = warmupService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        if (!warmupTerminated) {
            warmupService.shutdownNow();
            throw new IllegalStateException("There were non-terminated threads");
        }

        ExecutorService jobService = Executors.newFixedThreadPool(threads);

        for (IBench benchmark : benchmarks) {
            benchmark.jobs().forEachRemaining(jobService::execute);
        }

        jobService.shutdown();
        boolean jobsTerminated = jobService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        if (!jobsTerminated) {
            final List<Runnable> runnables = jobService.shutdownNow();
            LOGGER.warn("There were non-terminated threads: {}", runnables);
        }
    }
}
