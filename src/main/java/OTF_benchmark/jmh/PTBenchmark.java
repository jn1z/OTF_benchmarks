package OTF_benchmark.jmh;

import OTF_benchmark.PT.ValmariDet;
import OTF_benchmark.PT.ValmariExtractors;
import OTF_benchmark.PT.ValmariInitializers;
import OTF_benchmark.PT.ValmariLehtinen;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.partitionrefinement.Valmari;
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
public class PTBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options opt =
                new OptionsBuilder().include(PTBenchmark.class.getSimpleName()).addProfiler(GCProfiler.class).build();
        final Runner runner = new Runner(opt);
        runner.run();
    }

    @Benchmark
    public void pt(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa);
        blackhole.consume(min);
    }

   /* @Benchmark
    public void ptOtf(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        final Alphabet<Integer> alphabet = dfa.getInputAlphabet();

        final PaigeTarjan2 pt = new PaigeTarjan2();
        final FullIntAbstraction<Integer, Boolean, Void> abs = dfa.fullIntAbstraction(alphabet);

        BitSet finishedStates = new BitSet(dfa.size());
        finishedStates.set(0, dfa.size());
        PTInitializers.initDeterministic(pt, dfa, finishedStates);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        CompactDFA<Integer> min = PaigeTarjanMinimization2.toDeterministicPruned(pt, new CompactDFA.Creator<>(), alphabet, abs);
        blackhole.consume(min);
    }*/

    @Benchmark
    public void valmari(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        Valmari valmari = net.automatalib.util.partitionrefinement.ValmariInitializers.initializeNFA(dfa, dfa.getInputAlphabet());
        valmari.computeCoarsestStablePartition();
        CompactDFA<Integer> minDfa = net.automatalib.util.partitionrefinement.ValmariExtractors.toUniversal(valmari, dfa, dfa.getInputAlphabet(), CompactDFA::new);

        blackhole.consume(minDfa);
    }

    @Benchmark
    public void valmariDet(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        ValmariDet valmari = ValmariInitializers.initDet(dfa, dfa.getInputAlphabet());
        valmari.computeCoarsestStablePartition();
        CompactDFA<Integer> minDfa = ValmariExtractors.toDFA(valmari, dfa.getInputAlphabet());

        blackhole.consume(minDfa);
    }

    @Benchmark
    public void valmariLehtinen(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        ValmariLehtinen valmari = ValmariInitializers.initVL(dfa, dfa.getInputAlphabet());
        valmari.computeCoarsestStablePartition();
        CompactDFA<Integer> minDfa = ValmariExtractors.toDFA(valmari, dfa, dfa.getInputAlphabet());

        blackhole.consume(minDfa);
    }

    @Benchmark
    public void bealCrochemore(BisimState state, Blackhole blackhole) {
        final CompactDFA<Integer> dfa = state.getDfa();
        final CompactDFA<Integer> min = Automata.minimize(dfa, dfa.getInputAlphabet(), new CompactDFA<>(dfa.getInputAlphabet()));
        blackhole.consume(min);
    }

}