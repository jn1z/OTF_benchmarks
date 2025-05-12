package OTF_benchmark;

import OTF.NFATrim;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.random.RandomUtil;

import java.util.Random;

public class TVNFAWithLocality {
    public static <A extends MutableNFA<Integer, Integer>> A generateNFA(
        Random r, int size, float td, float ad, float df, Alphabet<Integer> alphabet, AutomatonCreator<A, Integer> creator) {
        return generateNFAWithLinearStructure(
            r, size, Math.round(td * size), Math.round(ad * size), Math.round(df * size),
            alphabet, creator);
    }

    public static CompactNFA<Integer> generateTrimNFAWithLinearStructure(
            int r, int size, float distanceFactor) {
        CompactNFA<Integer> nfa = generateNFAWithLinearStructure(r, size, distanceFactor);
        return NFATrim.trim(nfa);
    }
    public static CompactNFA<Integer> generateNFAWithLinearStructure(
            int r, int size, float distanceFactor) {
        final float td = 1.5f; // 1.25f
        final float ad = 0.5f;
        final Random random = new Random(r);
        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        return generateNFAWithLinearStructure(
                random, size, Math.round(td * size), Math.round(ad * size), Math.round(distanceFactor * size),
                alphabet, CompactNFA::new);
    }

    /**
     * Generate random NFA with locality using Tabakov's approach, described in
     * Tabakov's Masters thesis:
     * https://www.cs.rice.edu/CS/Verification/Theses/Archive/dtabakov_thesis2005.pdf
     *
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param td
     *      transition density, in [0,size]
     * @param ad
     *      acceptance density, in (0,1]. 0.5 is the usual value
     * @param alphabet
     *      alphabet
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <A extends MutableNFA<Integer, Integer>> A generateNFAWithLinearStructure(
            Random r, int size, float td, float ad, Alphabet<Integer> alphabet, float distanceFactor,
            AutomatonCreator<A, Integer> creator) {
        return generateNFAWithLinearStructure(
                r, size, Math.round(td * size), Math.round(ad * size), Math.round(distanceFactor * size),
                alphabet, creator);
    }

    /**
     * Generate random NFA, with fixed number of accept states and edges (per letter).
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param edgeNum
     *      number of edges (per letter)
     * @param acceptNum
     *      number of accepting states (at least one)
     * @param distance
     *      maximum distance between edges.
     * @param alphabet
     *      alphabet
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <A extends MutableNFA<Integer, Integer>> A generateNFAWithLinearStructure(
            Random r, int size, int edgeNum, int acceptNum, int distance,
            Alphabet<Integer> alphabet, AutomatonCreator<A, Integer> creator) {
        assert acceptNum > 0 && acceptNum <= size;
        assert edgeNum >= 0 && edgeNum <= size*size;

        A result = TabakovVardiRandomNFA.basicNFA(size, alphabet, creator);

        // Set final states other than the initial state.
        // We want exactly acceptNum-1 of them, from the elements [1,size).
        // This works even if acceptNum == 1.
        int[] finalStates = RandomUtil.distinctIntegers(r, acceptNum - 1, 1, size);
        for (int f : finalStates) {
            result.setAccepting(f, true);
        }

        // For each letter, add edgeNum transitions.
        // For the linear restriction, we want |state1 - state2| <= distance * size.
        IntSet edges = new IntOpenHashSet();
        int distanceRange = 2*distance + 1;
        for (int a: alphabet) {
            int edgeCount = 0;
            while (edgeCount < edgeNum) {
                // Generate state and random distance
                for (int edgeIndex: RandomUtil.distinctIntegers(r, edgeNum-edgeCount, size*(distanceRange))) {
                    if (!edges.add(edgeIndex)) {
                        continue;
                    }
                    int state1 = edgeIndex / distanceRange;
                    int offset = edgeIndex % distanceRange; // [0, 2*distance]
                    offset -= (distance); // [-distance,+distance]
                    int state2 = state1 - offset;
                    if (state2 < 0 || state2 >= size ) {
                        continue;
                    }
                    result.addTransition(state1, a, state2);
                    edgeCount++;
                }
            }
        }

        return result;
    }

}

