package OTF_benchmark.jmh;

import OTF.Model.Threshold;
import OTF.OTFDeterminization;
import OTF.Registry.AntichainForestRegistry;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(1)
public class UnionJMHBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(UnionJMHBenchmark.class.getSimpleName()).build();
        final Runner runner = new Runner(opt);
        runner.run();
    }

    @Benchmark
    public void pt(UnionState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getProduct();
        final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa);
        blackhole.consume(min);
    }

    @Benchmark
    public void otf(UnionState state, Blackhole blackhole) {
        final CompactNFA<Integer> union = state.getUnion();
        final DFA<?, Integer> min = OTFDeterminization.doOTF(
            union.powersetView(), union.getInputAlphabet(), Threshold.maxSteps(5000), new AntichainForestRegistry<>(union));
        blackhole.consume(min);
    }
}
