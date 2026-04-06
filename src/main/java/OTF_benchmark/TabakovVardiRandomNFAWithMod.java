package OTF_benchmark;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;

import java.util.*;

/**
 * Structured Tabakov-Vardi-style NFA generator.
 * <p>
 * - Alphabet size A = max(1, floor(sqrt(N))).
 * - Partition states into A classes by (state % A).
 * - For each symbol a in [0..A-1], add transitions that stay within each class.
 * - Degree balancing: for each class and symbol, assigns out-degrees that
 * average to 'density' and matches stubs so the in-degree multiset equals
 * the out-degree multiset (roughly uniform targets).
 * <p>
 * Density is interpreted as "average out-degree per state per symbol".
 * Example: density = 2.0 -> each state has ~2 out-edges on each symbol.
 */
public final class TabakovVardiRandomNFAWithMod {
  public static CompactNFA<Integer> generateNFA(long seed, int nStates, double density) {
    if (nStates <= 0 || density < 0.0) {
      throw new IllegalArgumentException("nStates must be > 0, density must be >= 0");
    }
    final Random rnd = new Random(seed);

    final int A = Math.max(1, (int) Math.floor(Math.sqrt(nStates)));
    final Alphabet<Integer> alphabet = Alphabets.integers(0, A - 1);

    final CompactNFA<Integer> nfa = new CompactNFA<>(alphabet, nStates);

    // Create states
    for (int i = 0; i < nStates; i++) {
      nfa.addState(false);
    }

    // Partition states into representative classes by (state % A).
    // Pick one state per class as initial & accepting.
    final List<List<Integer>> classes = new ArrayList<>(A);
    for (int c = 0; c < A; c++) classes.add(new ArrayList<>());
    for (int s = 0; s < nStates; s++) {
      int idx = Math.floorMod(s, A);
      List<Integer> c = classes.get(idx);
      if (c.isEmpty()) {
        nfa.setInitial(s, true);
        nfa.setAccepting(s, true);
      }
      c.add(s);
    }

    // For every symbol, generate balanced transitions within each class — but enforce per-(src,sym) uniqueness.
    for (int sym = 0; sym < A; sym++) {
      for (List<Integer> cls : classes) {
        final int m = cls.size();
        if (m == 0) continue;

        // Out-degrees with randomized rounding; cap at m to allow unique targets.
        final double avg = Math.min(density, m);
        final int base = (int) Math.floor(avg);
        final int extras = (int) Math.round((avg - base) * m);

        final List<Integer> outDegs = new ArrayList<>(m);
        for (int i = 0; i < m; i++) outDegs.add(Math.min(base, m));
        final List<Integer> idxs = new ArrayList<>(m);
        for (int i = 0; i < m; i++) idxs.add(i);
        Collections.shuffle(idxs, rnd);
        for (int i = 0; i < extras && i < m; i++) {
          final int pos = idxs.get(i);
          outDegs.set(pos, Math.min(outDegs.get(pos) + 1, m));
        }

        // Recompute total edges after capping.
        int totalEdges = 0;
        for (int d : outDegs) totalEdges += d;
        if (totalEdges == 0) continue;

        // Build "stubs": outStubs & inStubs (balanced multiset per class & symbol).
        final List<Integer> outStubs = new ArrayList<>(totalEdges);
        final List<Integer> inStubs  = new ArrayList<>(totalEdges);

        // Permute indices to define target multiplicities (same multiset as outDegs, different order).
        final List<Integer> inDegsOrder = new ArrayList<>(m);
        for (int i = 0; i < m; i++) inDegsOrder.add(i);
        Collections.shuffle(inDegsOrder, rnd);

        // Fill stubs
        for (int i = 0; i < m; i++) {
          final int src = cls.get(i);
          for (int k = 0; k < outDegs.get(i); k++) outStubs.add(src);
        }
        for (int j = 0; j < m; j++) {
          final int tIdx = inDegsOrder.get(j);
          final int tgt = cls.get(tIdx);
          final int deg = outDegs.get(tIdx);
          for (int k = 0; k < deg; k++) inStubs.add(tgt);
        }

        // Shuffle pairing (balanced matching skeleton).
        Collections.shuffle(inStubs, rnd);

        // Map stateId -> local index [0..m-1] for O(1) "used[target]" lookup.
        final Map<Integer, Integer> localIndex = new HashMap<>(m * 2);
        for (int i = 0; i < m; i++) localIndex.put(cls.get(i), i);

        // Enforce uniqueness per (src, sym) while pairing.
        // For each src, keep a boolean[] "used targets in this class".
        final Map<Integer, boolean[]> usedBySrc = new HashMap<>(m * 2);

        for (int e = 0; e < totalEdges; e++) {
          final int src = outStubs.get(e);
          final int origTgt = inStubs.get(e);

          boolean[] used = usedBySrc.computeIfAbsent(src, sId -> new boolean[m]);
          int tgtLocal = localIndex.get(origTgt);
          int chosenTgt = origTgt;

          if (used[tgtLocal]) {
            // Try to swap with a later stub giving a fresh target for this src
            int swapAt = -1;
            for (int f = e + 1; f < totalEdges; f++) {
              int candidate = inStubs.get(f);
              int candLocal = localIndex.get(candidate);
              if (!used[candLocal]) { swapAt = f; break; }
            }
            if (swapAt != -1) {
              // swap and take the fresh candidate
              Collections.swap(inStubs, e, swapAt);
              chosenTgt = inStubs.get(e);
              tgtLocal = localIndex.get(chosenTgt);
            } else {
              // Fallback: pick uniformly a fresh target not yet used by this src
              // (keeps uniqueness; may deviate slightly from the per-class balanced in-degree)
              int tries = 0;
              do {
                chosenTgt = cls.get(rnd.nextInt(m));
                tgtLocal = localIndex.get(chosenTgt);
                tries++;
              } while (used[tgtLocal] && tries < 3 * m);

              if (used[tgtLocal]) {
                // As a final fallback, scan for any unused target
                for (int scan = 0; scan < m; scan++) {
                  int cand = cls.get(scan);
                  if (!used[scan]) { chosenTgt = cand; tgtLocal = scan; break; }
                }
              }
            }
          }

          // Emit edge and mark used
          if (!used[tgtLocal]) {
            used[tgtLocal] = true;
            nfa.addTransition(src, sym, chosenTgt);
          }
          // else (pathologically) all m were already used because outDeg > m,
          // but we capped outDeg<=m, so this should not happen.
        }
      }
    }

    return nfa;
  }
}
