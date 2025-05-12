package OTF_benchmark.jmh;

import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.partitionrefinement.Valmari;
import net.automatalib.util.partitionrefinement.ValmariExtractors;
import net.automatalib.util.partitionrefinement.ValmariInitializers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 2, time = 5)
@Fork(1)
public class BisimBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(BisimBenchmark.class.getSimpleName())
                                                .addProfiler(GCProfiler.class)
                                                .build();
        final Runner runner = new Runner(opt);
        runner.run();
    }

    @Benchmark
    public void valmari(BisimState state, Blackhole blackhole) {
        final CompactNFA<Integer> nfa = state.getTv();
        Valmari valmari = ValmariInitializers.initializeNFA(nfa, nfa.getInputAlphabet());
        valmari.computeCoarsestStablePartition();
        CompactNFA<Integer> min = ValmariExtractors.toNFA(valmari, nfa, nfa.getInputAlphabet(), false, CompactNFA::new);

        blackhole.consume(min);
    }
}