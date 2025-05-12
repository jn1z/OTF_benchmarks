package OTF_benchmark;

import OTF.PowersetDeterminizer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Random generator for Walnut-type automata.
 */
public class TVLanguageProjection {
  public static CompactNFA<Integer> randomNFA(Random r, int size, int alphSize, float compressFactor, float af) {
    Alphabet<Integer> alph = Alphabets.integers(0, alphSize - 1);
    CompactDFA<Integer> dfa;
    CompactDFA<Integer> compactDFA = null;
    while (compactDFA == null || compactDFA.size() < size) {
      // A random DFA will usually be minimal or near-minimal, so this isn't terrible
      dfa = randomDFA(r, size, alphSize, af);
      compactDFA = HopcroftMinimizer.minimizeDFA(dfa, alph);
    }
    return compressInputs(compactDFA, compressFactor);
  }

  public static CompactNFA<Integer> randomNFACombined(Random r, int size, int alphSize, float compressFactor, float af) {
    CompactNFA<Integer> nfa1 = randomNFA(r, size, alphSize, compressFactor, af);
    CompactNFA<Integer> nfa2 = randomNFA(r, size, alphSize, compressFactor, af);

    CompactDFA<Integer> dfa1 = new CompactDFA<>(nfa1.getInputAlphabet());
    PowersetDeterminizer.determinize(nfa1, nfa1.getInputAlphabet(), dfa1, true);

    CompactDFA<Integer> dfa2 = new CompactDFA<>(nfa2.getInputAlphabet());
    PowersetDeterminizer.determinize(nfa2, nfa2.getInputAlphabet(), dfa2, true);

    CompactDFA<Integer> dfa3 = productDFA(dfa1, dfa2, (a, b) -> a ^ b);
    CompactDFA<Integer> dfa4 = HopcroftMinimizer.minimizeDFA(dfa3, dfa3.getInputAlphabet());

    return compressInputs(dfa4, compressFactor);
  }

  public static CompactDFA<Integer> randomDFA(Random r, int size, int alphabetSize, float af) {
    CompactDFA<Integer> dfa = new CompactDFA<>(Alphabets.integers(0, alphabetSize - 1));
    // Determine how many states should be accepting
    // We always choose the initial state to be accepting
    // This is possibly not necessary, but we shuffle our remaining choices of accepting states.
    int numAccepting = Math.round(af * size);
    IntList allNonZeroStates = new IntArrayList();
    for (int i = 1; i < size; i++) {
      allNonZeroStates.add(i);
    }
    Collections.shuffle(allNonZeroStates, r);
    Set<Integer> acceptingStates = new HashSet<>(allNonZeroStates.subList(0, numAccepting));
    for (int i = 0; i < size; i++) {
      dfa.addState(i == 0 || acceptingStates.contains(i));
    }
    dfa.setInitial(0, true);

    for (int i = 0; i < size; i++) {
      for (int a = 0; a < alphabetSize; a++) {
        dfa.addTransition(i, a, r.nextInt(size));
      }
    }
    return dfa;
  }


  /**
   * Similar to eliminating quantifiers in Walnut; alphabet is compressed.
   *
   * @param dfa            - minimized DFA
   * @param compressFactor - factor to compress alphabet by
   * @return NFA with compressed alphabet
   */
  public static CompactNFA<Integer> compressInputs(CompactDFA<Integer> dfa, float compressFactor) {
    int oldAlphSize = dfa.getInputAlphabet().size();
    int newAlphSize = (int) (Math.ceil(oldAlphSize / compressFactor));

    // All states remain the same
    Set<Integer> initialStates = dfa.getInitialStates();
    CompactNFA<Integer> result = new CompactNFA<>(Alphabets.integers(0, newAlphSize - 1), dfa.size());
    for (int i = 0; i < dfa.size(); i++) {
      result.addState(dfa.isAccepting(i));
      if (initialStates.contains(i)) {
        result.setInitial(i, true);
      }
    }

    int[] oldToNew = createFairMapping(oldAlphSize, newAlphSize);

    // transitions reflect the compressed alphabet
    for (int a = 0; a < oldAlphSize; a++) {
      for (int i = 0; i < dfa.size(); i++) {
        result.addTransitions(i, oldToNew[a], dfa.getTransitions(i, a));
      }
    }
    return result;
  }

  /**
   * Create onto function to map the old alphabet to the new one.
   * This is fair, in the sense that all groups of letters that are mapped are roughly the same size.
   */
  private static int[] createFairMapping(int oldAlphSize, int newAlphSize) {
    int[] oldToNew = new int[oldAlphSize];
    double step = (double) newAlphSize / oldAlphSize; // Step size for fair distribution
    double current = 0; // Tracks the current position in the new range

    for (int i = 0; i < oldAlphSize; i++) {
      oldToNew[i] = (int) current;
      current += step;
      if (current >= newAlphSize) {
        current -= newAlphSize; // Wrap around to ensure fairness
      }
    }
    return oldToNew;
  }

  /**
   * Create DFA product of two DFAs. Operator is passed in.
   */
  public static CompactDFA<Integer> productDFA(CompactDFA<Integer> dfa1, CompactDFA<Integer> dfa2, BiPredicate<Boolean, Boolean> operator) {
    makeTotal(dfa1);
    makeTotal(dfa2);

    // Assumes both DFAs share the same input alphabet.
    Alphabet<Integer> alphabet = dfa1.getInputAlphabet();
    CompactDFA<Integer> product = new CompactDFA<>(alphabet);

    // Map to keep track of (dfa1 state, dfa2 state) -> product state index.
    Map<IntIntPair, Integer> stateMapping = new HashMap<>();
    Queue<IntIntPair> queue = new LinkedList<>();

    // Get initial states from both DFAs.
    int init1 = dfa1.getInitialState();
    int init2 = dfa2.getInitialState();
    IntIntPair initPair = new IntIntImmutablePair(init1, init2);

    boolean isAccepting = operator.test(dfa1.isAccepting(init1), dfa2.isAccepting(init2));

    product.addState(isAccepting);
    product.setInitial(0, true);
    stateMapping.put(initPair, 0);
    queue.add(initPair);

    // Process each product state (represented as a pair) in a breadth-first manner.
    while (!queue.isEmpty()) {
      IntIntPair current = queue.poll();
      int currentIndex = stateMapping.get(current);

      // For each symbol in the alphabet, compute the transition.
      for (int a = 0; a < alphabet.size(); a++) {
        int next1 = dfa1.getTransition(current.firstInt(), a);
        int next2 = dfa2.getTransition(current.secondInt(), a);
        IntIntPair nextPair = new IntIntImmutablePair(next1, next2);
        int nextIndex;

        // If the next pair hasn't been seen before, add it to the product DFA.
        Integer stateMapVal = stateMapping.get(nextPair);
        if (stateMapVal == null) {
          nextIndex = product.size();
          stateMapping.put(nextPair, nextIndex);
          boolean nextAccepting = dfa1.isAccepting(next1) && dfa2.isAccepting(next2);
          product.addState(nextAccepting);
          queue.add(nextPair);
        } else {
          nextIndex = stateMapVal;
        }

        // Add the transition for symbol.
        product.addTransition(currentIndex, a, nextIndex);
      }
    }
    return product;
  }

  /**
   * Totalize DFA.
   */
  public static CompactDFA<Integer> makeTotal(CompactDFA<Integer> dfa) {
    Alphabet<Integer> alphabet = dfa.getInputAlphabet();
    int alphSize = alphabet.size();
    int deadState = -1;  // This will hold the index of the dead state, once created.

    // Iterate over every state and symbol.
    for (int state = 0; state < dfa.size(); state++) {
      for (int symbol = 0; symbol < alphSize; symbol++) {
        // Assume that a missing transition returns -1.
        Integer target = dfa.getTransition(state, symbol);
        if (target != null) {
          continue;
        }
        // If we haven't created the dead state yet, do it now.
        if (deadState == -1) {
          deadState = dfa.size();
          // Add the dead state; it's non-accepting.
          dfa.addState(false);
          // Make the dead state total by looping to itself on every symbol.
          for (int a = 0; a < alphSize; a++) {
            dfa.addTransition(deadState, a, deadState);
          }
        }
        // Add the missing transition to the dead state.
        dfa.addTransition(state, symbol, deadState);
      }
    }
    return dfa;
  }
}
