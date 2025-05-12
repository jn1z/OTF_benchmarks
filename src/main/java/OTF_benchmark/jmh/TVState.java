package OTF_benchmark.jmh;

import java.util.Random;

import OTF.NFATrim;
import OTF_benchmark.TabakovVardiRandomNFA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class TVState {

    @Param({"100", "150"})
    private int size;

    @Param({"1.4", "2", "2.5"})
    private float td;

    private Alphabet<Integer> alphabet;
    private CompactNFA<Integer> tv;

    @Setup
    public void setup() {
        final Random random = new Random(42);
        this.alphabet = Alphabets.integers(0, 1);
        final CompactNFA<Integer> nfa = TabakovVardiRandomNFA.generateNFA(random, size, td, 0.5f, alphabet, CompactNFA::new);

        this.tv = NFATrim.trim(nfa, CompactNFA::new);
    }

    public CompactNFA<Integer> getTv() {
        return tv;
    }

    public Alphabet<Integer> getAlphabet() {
        return alphabet;
    }
}
