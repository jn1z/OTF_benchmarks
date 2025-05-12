package OTF_benchmark.PT;

import java.util.Arrays;
import java.util.Objects;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;

public class ValmariExtractors {

    public static CompactDFA<Integer> toDFA(ValmariDet valmari, Alphabet<Integer> alphabet) {

        /* Count the numbers of transitions and final states in the result */
        int mo = 0, fo = 0;
        for (int t = 0; t < valmari.mm; ++t) {
            if (valmari.B.L[valmari.T[t]] == valmari.B.F[valmari.B.S[valmari.T[t]]]) {
                ++mo;
            }
        }
        for (int b = 0; b < valmari.B.z; ++b) {
            if (valmari.B.F[b] < valmari.ff) {
                ++fo;
            }
        }

        final CompactDFA<Integer> result = new CompactDFA<>(alphabet, valmari.B.z);

        /* Print the result */
        for (int b = 0; b < valmari.B.z; ++b) {
            result.addState(valmari.B.F[b] < valmari.ff);
        }

        result.setInitialState(valmari.B.S[valmari.q0]);

        for (int t = 0; t < valmari.mm; ++t) {
            if (valmari.B.L[valmari.T[t]] == valmari.B.F[valmari.B.S[valmari.T[t]]]) {
                result.setTransition(valmari.B.S[valmari.T[t]], valmari.L[t], valmari.B.S[valmari.H[t]]);
            }
        }

//        MutableDFAs.complete(result, alphabet);
        return result;
    }

    public static <I> CompactDFA<I> toDFA(ValmariLehtinen valmari, DFA<Integer, I> original, Alphabet<I> alphabet) {

        int numBlocks = valmari.brp.sets + 1;
        int numInputs = alphabet.size();
        int[] repMap = new int[numBlocks];
        int[] stateMap = new int[numBlocks];
        Arrays.fill(stateMap, -1);

        CompactDFA<I> result = new CompactDFA<>(alphabet);

        int origInit = Objects.requireNonNull(original.getInitialState());
        int resInit = result.addIntInitialState(original.isAccepting(origInit));

        int initRep = valmari.brp.sidx[origInit];
        stateMap[initRep] = resInit;
        repMap[resInit] = origInit;

        int statesPtr = 0;
        int numStates = 1;
        while (statesPtr < numStates) {
            int resState = statesPtr++;
            int rep = repMap[resState];
            for (int i = 0; i < numInputs; i++) {
                Integer succ = original.getTransition(rep, alphabet.getSymbol(i));
                if (succ != null) {
                    int succBlockId = valmari.brp.sidx[succ];
                    int resSucc = stateMap[succBlockId];
                    if (resSucc < 0) {
                        boolean sp = original.isAccepting(succ);
                        resSucc = result.addState(sp);
                        stateMap[succBlockId] = resSucc;
                        repMap[resSucc] = succ;
                        numStates++;
                    }
                    result.setTransition(resState, i, resSucc);
                }
            }
        }

        return result;
    }

}
