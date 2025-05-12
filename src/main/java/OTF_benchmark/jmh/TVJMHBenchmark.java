package OTF_benchmark.jmh;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
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
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Fork(1)
public class TVJMHBenchmark extends AbstractJMHBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt =
                new OptionsBuilder().include(TVJMHBenchmark.class.getSimpleName()).addProfiler(GCProfiler.class).build();
        final Runner runner = new Runner(opt);
        runner.run();
    }

    @Benchmark
    public void subset(TVState state, Blackhole blackhole) {
        final CompactNFA<Integer> nfa = state.getTv();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.subset(nfa, alphabet);
        blackhole.consume(dfa);
    }

    @Benchmark
    public void otf(TVState state, Blackhole blackhole) {
        final CompactNFA<Integer> nfa = state.getTv();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.otf(nfa, alphabet);
        blackhole.consume(dfa);
    }
/*
    @Benchmark
    public void rabit(TVState state, Blackhole blackhole) {
        final FiniteAutomaton fa = state.getFa();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.rabit(fa, alphabet);
        blackhole.consume(dfa);
    }*/

}

