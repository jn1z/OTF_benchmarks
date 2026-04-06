package OTF;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableNFA;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class ExponentialNFA {
  /**
   * Builds the NFA M_k with k+1 states:
   *   – state 0 is initial & accepting
   *   – for each i=0…k–1, on symbol i you have transitions
   *       0 –i→ 0   (stay in q0)
   *       0 –i→ i+1 (branch off to qi)
   *   – for each branch‐state j=1…k, on every symbol loop back to itself
   *   – PLUS an “escape” state r
   *
   * q0 is initial+accepting.  On symbol i∈{0…k−1}:
   *    q0 –i→ q0   (stay)
   *    q0 –i→ qi   (branch)
   *
   * Each qi (i=1…k):
   *    – loops on all symbols to itself
   *    – on its own symbol i, also goes to ri
   *
   * r:
   *    – on *every* symbol goes straight back to q0
   *
   * Nothing except q0 and r are marked accepting;  but every qi can
   * reach q0 (by “i then anything”), so trim will keep them all.
   * Bisimulation can no longer collapse any two qi,
   * and the DFA still sees all 2^k subsets of {q1…qk}.
   */


  public static <A extends MutableNFA<Integer, Integer>>
  A generateBranchingNFA(int k,
                                AutomatonCreator<A,Integer> creator) {
    Alphabet<Integer> alphabet = Alphabets.integers(0, k-1);

    // total states = (k+1) core + 1 shared escape
    int coreSize   = k + 1;
    int totalSize  = coreSize + 1;
    int escapeIdx  = coreSize;    // index of the single escape state

    // create automaton and all states (initially non-accepting)
    A nfa = creator.createAutomaton(alphabet);
    for (int i = 0; i < totalSize; i++) {
      nfa.addState(false);
    }

    // q0 = state 0 is initial and accepting
    nfa.setInitial(0, true);
    nfa.setAccepting(0, true);

    // 1) Branching from q0 on symbol i: stay or branch
    for (Integer sym : alphabet) {
      int i = sym;  // assume symbols 0..k-1
      if (i < k) {
        nfa.addTransition(0, sym, 0);        // stay in q0
        nfa.addTransition(0, sym, i + 1);    // branch to qi
      }
    }

    // 2) Each branch-state qi = 1..k:
    //    – loops on every symbol back to itself
    //    – on its own symbol i, also goes to the shared escape
    for (int i = 1; i <= k; i++) {
      for (Integer sym : alphabet) {
        nfa.addTransition(i, sym, i);
      }
      nfa.addTransition(i, i - 1, escapeIdx);
    }

    // 3) Shared escape state:
    //    – on every symbol, returns to q0
    for (Integer sym : alphabet) {
      nfa.addTransition(escapeIdx, sym, 0);
    }

    // 4) Make the escape state “live” under trim
    //    (it reaches q0, so is co-reachable; but mark it accepting
    //     if your trim only keeps states that can *reach* an accepting)
    nfa.setAccepting(escapeIdx, true);

    return nfa;
  }


  // Experimental
  /*
  public static <A extends MutableNFA<Integer, Integer>>
  A generateBranchingNFA(int k,
                         AutomatonCreator<A, Integer> creator) {
    Alphabet<Integer> alphabet = Alphabets.integers(0, k-1);

    // total states = (k+1) core + 1 shared escape
    int coreSize   = k + 1;
    int totalSize  = coreSize + 2;
    int escapeIdx  = coreSize;    // index of the single escape state
    int sharedQIdx = escapeIdx + 1;    // index of the shared q state

    // create automaton and all states (initially non-accepting)
    A nfa = creator.createAutomaton(alphabet);
    for (int i = 0; i < totalSize; i++) {
      nfa.addState(false);
    }

    // q0 = state 0 is initial and accepting
    nfa.setInitial(0, true);
    nfa.setAccepting(0, true);

    // 1) Branching from q0 on symbol i: stay or branch
    for (Integer sym : alphabet) {
      int i = sym;  // assume symbols 0..k-1
      if (i < k) {
        nfa.addTransition(0, sym, 0);        // stay in q0
        nfa.addTransition(0, sym, i + 1);    // branch to qi
      }
    }
    nfa.addTransition(0, 0, sharedQIdx);

    // 2) Each branch-state qi = 1..k:
    //    – loops on every symbol back to itself
    //    – on its own symbol i, also goes to the shared escape
    for (int i = 1; i <= k; i++) {
      for (Integer sym : alphabet) {
        nfa.addTransition(i, sym, i);
      }
      nfa.addTransition(i, i - 1, escapeIdx);
    }

    // 3) Shared escape state:
    //    – on every symbol, returns to q0
    for (Integer sym : alphabet) {
      nfa.addTransition(escapeIdx, sym, 0);
    }

    // 4) Make the escape state “live” under trim
    //    (it reaches q0, so is co-reachable; but mark it accepting
    //     if your trim only keeps states that can *reach* an accepting)
    nfa.setAccepting(escapeIdx, true);

    // 5) Shared Q state
    for (Integer sym : alphabet) {
      int i = sym;  // assume symbols 0..k-1
      if (i < k) {
        nfa.addTransition(sharedQIdx, sym, 0);        // stay in q0
        nfa.addTransition(sharedQIdx, sym, i + 1);    // branch to qi
      }
    }
    nfa.addTransition(sharedQIdx, 0, sharedQIdx);
    for (int i = 1; i <= k; i++) {
      for (Integer sym : alphabet) {
        nfa.addTransition(sharedQIdx, sym, i);
      }
      nfa.addTransition(sharedQIdx, i - 1, escapeIdx);
    }

    return nfa;
  }

  // BitSets of length n with exactly k bits set.
  public static List<BitSet> genBitsets(int n, int k) {
    List<BitSet> res = new ArrayList<>();
    backtrack(new BitSet(n), 0, k, n, res);
    return res;
  }

  private static void backtrack(BitSet bs, int pos, int rem, int n, List<BitSet> out) {
    // prune: not enough slots left to place all remaining 1s
    if (rem > n - pos) return;

    if (pos == n) {
      if (rem == 0) {
        // add a *copy* of bs
        out.add((BitSet) bs.clone());
      }
      return;
    }

    // 1) Place a 1 at pos (if we still need them)
    if (rem > 0) {
      bs.set(pos);
      backtrack(bs, pos + 1, rem - 1, n, out);
      bs.clear(pos);
    }

    // 2) Place a 0 at pos
    backtrack(bs, pos + 1, rem, n, out);
  }*/
}
