package OTF_benchmark.jmh;

import OTF.Compress.AntichainForest5Idx;
import OTF.Model.Threshold;
import OTF.OTFDeterminization;
import OTF.PowersetDeterminizer;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;

public class AbstractJMHBenchmark {

    protected DFA<?, Integer> subset(CompactNFA<Integer> nfa, Alphabet<Integer> alphabet) {
        return PowersetDeterminizer.determinize(nfa, alphabet);
    }

    protected DFA<?, Integer> otf(CompactNFA<Integer> nfa, Alphabet<Integer> alphabet) {
        return OTFDeterminization.doOTF(
            nfa.powersetView(), alphabet, Threshold.maxSteps(500), new AntichainForest5Idx(nfa.size()));
    }

}

