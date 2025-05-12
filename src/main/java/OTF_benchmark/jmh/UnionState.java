package OTF_benchmark.jmh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import OTF_benchmark.LanguageUnion;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.random.RandomAutomata;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class UnionState {

    @Param({"20", "50"})
    private int size;

    private CompactDFA<Integer> product;
    private CompactNFA<Integer> union;

    @Setup
    public void setup() {
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final List<DFA<?, Integer>> dfas = generateDFAs(alphabet, 4);

        this.product = computeProduct(dfas, alphabet);
        this.union = computeUnion(dfas, alphabet);

        System.err.println("Product size: " + this.product.size());
        System.err.println("union size: " + this.union.size());
    }

    private <I> List<DFA<?, I>> generateDFAs(Alphabet<I> alphabet, int num) {
        final List<DFA<?, I>> result = new ArrayList<>(num);
        final Random random = new Random(42);

        for (int i = 0; i < num; i++) {
            result.add(RandomAutomata.randomDFA(random, size, alphabet));
        }

        return result;
    }

    private <I> CompactDFA<I> computeProduct(List<DFA<?, I>> dfas, Alphabet<I> alphabet) {
        assert dfas.size() > 1;

        CompactDFA<I> result = LanguageUnion.or(dfas.get(0), dfas.get(1), alphabet);

        for (int i = 2; i < dfas.size(); i++) {
            result = LanguageUnion.or((DFA<?, I>) result, dfas.get(i), alphabet);
        }

        return result;
    }

    private <I> CompactNFA<I> computeUnion(List<DFA<?, I>> dfas, Alphabet<I> alphabet) {
        assert dfas.size() > 1;

        CompactNFA<I> result = LanguageUnion.or((NFA<?, I>) dfas.get(0), dfas.get(1), alphabet);

        for (int i = 2; i < dfas.size(); i++) {
            result = LanguageUnion.or((NFA<?, I>) result, dfas.get(i), alphabet);
        }

        return result;
    }

    public CompactDFA<Integer> getProduct() {
        return product;
    }

    public CompactNFA<Integer> getUnion() {
        return union;
    }
}
