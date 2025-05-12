package OTF_benchmark.jmh;

import java.io.IOException;
import java.util.BitSet;
import java.util.Random;

import OTF.BAFormat;
import OTF.PowersetDeterminizer;
import OTF_benchmark.InputWalnut;
import OTF_benchmark.TabakovVardiRandomNFA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.random.RandomUtil;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BisimState {

    private CompactNFA<Integer> tv;
    private CompactNFA<Integer> nfa;
    private CompactDFA<Integer> dfa;
    private BitSet finishedStates;

    @Setup
    public void setup() throws IOException {
        final Random random = new Random(1337);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        this.tv = TabakovVardiRandomNFA.generateNFA(random, 150, 2.0f, 0.5f, alphabet, CompactNFA::new);

        try {
            this.nfa = BAFormat.convertBAFromCompactNFA(InputWalnut.class.getResourceAsStream("/walnut/ssz23.ba"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.dfa = new CompactDFA<>(alphabet);
        PowersetDeterminizer.determinize(this.tv, alphabet, this.dfa, false);

        this.finishedStates = new BitSet(this.dfa.size());
        this.finishedStates.set(0, this.dfa.size());
    }

    public CompactNFA<Integer> getTv() {
        return tv;
    }

    public CompactNFA<Integer> getNfa() {
        return nfa;
    }

    public CompactDFA<Integer> getDfa() {
        return dfa;
    }

    public BitSet getFinishedStates() {
        return this.finishedStates;
    }

    private CompactDFA<Integer> generatePartialDFA() {

        Alphabet<Integer> alphabet = Alphabets.integers(0, 100);
        int n = 100;
        Random r = new Random(42);
        CompactDFA<Integer> result = new CompactDFA<>(alphabet);

        for (int i = 0; i < n; i++) {
            result.addState(r.nextBoolean());
        }

        result.setInitialState(r.nextInt(n));

        for (int i : result) {
            for (int j : RandomUtil.distinctIntegers(r, 5, alphabet.size())) {
                result.setTransition(i, j, r.nextInt(n));
            }
        }

        return result;
    }
}