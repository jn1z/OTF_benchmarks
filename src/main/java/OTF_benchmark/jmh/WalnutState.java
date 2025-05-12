package OTF_benchmark.jmh;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class WalnutState {

    @Param({"thm5-1790", "toeplitz"})
    private String filename;

    private CompactNFA<Integer> nfa;
    private Alphabet<Integer> alphabet;

    @Setup
    public void setup() throws IOException {
        /*try (InputStream is = WalnutState.class.getResourceAsStream('/' + this.filename + ".dot")) {
            final DOTInputModelData<Integer, Integer, CompactNFA<Integer>> modelData =
                    DOTParsers.nfa(DOTParsers.DEFAULT_FSA_NODE_PARSER, DOTImporter.EDGE_PARSER).readModel(is);

            this.nfa = NFAUtils.compactToCompactNFA(modelData.model);
            this.fa = Converter.CompactNFAtoRABITFA(this.nfa);
            this.alphabet = modelData.alphabet;
        }*/
    }

    public CompactNFA<Integer> getNfa() {
        return nfa;
    }

    public Alphabet<Integer> getAlphabet() {
        return alphabet;
    }

}
