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

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(1)
public class WalnutJMHBenchmark extends AbstractJMHBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder().include(WalnutJMHBenchmark.class.getSimpleName())
                                                .addProfiler(GCProfiler.class)
                                                .build();
        final Runner runner = new Runner(opt);
        runner.run();
    }

    @Benchmark
    public void subset(WalnutState state, Blackhole blackhole) {
        final CompactNFA<Integer> nfa = state.getNfa();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.subset(nfa, alphabet);
        blackhole.consume(dfa);
    }

    @Benchmark
    public void otf(WalnutState state, Blackhole blackhole) {
        final CompactNFA<Integer> nfa = state.getNfa();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.otf(nfa, alphabet);
        blackhole.consume(dfa);
    }
/*
    @Benchmark
    public void rabit(WalnutState state, Blackhole blackhole) {
        final FiniteAutomaton fa = state.getFa();
        final Alphabet<Integer> alphabet = state.getAlphabet();
        final DFA<?, Integer> dfa = super.rabit(fa, alphabet);
        blackhole.consume(dfa);
    }
*/
}

