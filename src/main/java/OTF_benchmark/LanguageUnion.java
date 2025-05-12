package OTF_benchmark;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableFSA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.fsa.DFAs;
import net.automatalib.util.ts.acceptor.AcceptanceCombiner;

public final class LanguageUnion {

    private LanguageUnion() {
        // prevent instantiation
    }

    public static <I> CompactNFA<I> or(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return or(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    public static <I, S, A extends MutableNFA<S, I>> A or(NFA<?, I> nfa1,
                                                          NFA<?, I> nfa2,
                                                          Collection<? extends I> inputs,
                                                          A out) {
        or(out, nfa1, inputs);
        or(out, nfa2, inputs);

        return out;
    }

    public static <S1, S2, I> void or(MutableFSA<S1, I> out, NFA<S2, I> in, Collection<? extends I> inputs) {

        final Mapping<S2, S1> mapping = AutomatonLowLevelCopy.copy(AutomatonCopyMethod.BFS, in, inputs, out);

        for (S2 s : in.getInitialStates()) {
            out.setInitial(mapping.get(s), true);
        }
    }

    public static <I> CompactDFA<I> or(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return DFAs.combine(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet), AcceptanceCombiner.OR);
    }
}
