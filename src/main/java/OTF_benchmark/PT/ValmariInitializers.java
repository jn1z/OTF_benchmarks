package OTF_benchmark.PT;

import java.util.BitSet;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;

public class ValmariInitializers {

    public static <I> ValmariDet initDet(CompactDFA<I> dfa, Alphabet<I> alphabet) {

        int n = dfa.size();
        int m = 0;

        final BitSet blocks = new BitSet(n);

        for (Integer s : dfa) {
            blocks.set(s, dfa.isAccepting(s));
            for (int i = 0; i < alphabet.size(); i++) {
                m += dfa.getTransition(s, i) == null ? 0 : 1;
            }
        }

        final int[] tail = new int[m];
        final int[] label = new int[m];
        final int[] head = new int[m];

        int cnt = 0;

        for (Integer s : dfa) {
            if (dfa.isAccepting(s)) {
                blocks.set(s);
            }
        }

        for (int i = 0; i < alphabet.size(); i++) {
            for (Integer s : dfa) {
                Integer succ = dfa.getTransition(s, i);
                if (succ != null) {
                    tail[cnt] = s;
                    label[cnt] = i;
                    head[cnt] = succ;
                    cnt++;
                }
            }
        }

        return new ValmariDet(dfa.getIntInitialState(), n, blocks, tail, label, head);
    }

    public static <I> ValmariLehtinen initVL(CompactDFA<I> dfa, Alphabet<I> alphabet) {

        int n = dfa.size();
        int m = 0;

        final int[] blocks = new int[n];
        int trueId, falseId;

        if (n > 0 && dfa.isAccepting(0)) {
            trueId = 0;
            falseId = 1;
        } else {
            falseId = 0;
            trueId = 1;
        }

        for (Integer s : dfa) {
            blocks[s] = dfa.isAccepting(s) ? trueId : falseId;
            for (int i = 0; i < alphabet.size(); i++) {
                m += dfa.getTransition(s, i) == null ? 0 : 1;
            }
        }

        final int[] tail = new int[m];
        final int[] label = new int[m];
        final int[] head = new int[m];

        int cnt = 0;

        for (int i = 0; i < alphabet.size(); i++) {
            for (Integer s : dfa) {
                Integer succ = dfa.getTransition(s, i);
                if (succ != null) {
                    tail[cnt] = s;
                    label[cnt] = i;
                    head[cnt] = succ;
                    cnt++;
                }
            }
        }

        return new ValmariLehtinen(blocks, 1, tail, label, head);
    }

}
