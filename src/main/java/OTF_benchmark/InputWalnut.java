package OTF_benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import OTF.BAFormat;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputWalnut implements IInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputWalnut.class);

    private static final Map<String, CompactNFA<Integer>> NFAS;

    static {
        try {
            // Test .ba files, except unsolved problem.
            final List<String> resources = Utils.findResources("/walnut/", s -> s.endsWith(".ba"));
            NFAS = Maps.newLinkedHashMapWithExpectedSize(resources.size());

            for (String resource : resources) {
                LOGGER.info("Loading '{}'", resource);

                try {
                    final CompactNFA<Integer> tv = BAFormat.convertBAFromCompactNFA(InputWalnut.class.getResourceAsStream(resource));
                    NFAS.put(Utils.extractFilename(resource), tv);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String id() {
        return "walnut";
    }

    @Override
    public String header() {
        return "name,inputs,size";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Iterators.forArray(toConfiguration("thm5"),
                                  toConfiguration("toeplitz"),
                                  toConfiguration("triboddpal"),
                                  toConfiguration("paper_pseudo2"));
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return Iterators.transform(NFAS.entrySet().iterator(), InputWalnut::toConfiguration);
    }

    private static IConf<Integer> toConfiguration(String name) {
        return toConfiguration(new SimpleEntry<>(name, NFAS.get(name)));
    }

    private static IConf<Integer> toConfiguration(Map.Entry<String, CompactNFA<Integer>> entry) {
        return new Configuration<>(entry.getKey(), entry.getValue());
    }

    private record Configuration<I>(String name, CompactNFA<I> nfa) implements IConf<I> {

        @Override
        public CompactNFA<I> buildNFA() {
            return nfa;
        }

        @Override
        public String getConfig() {
            return name + ',' + nfa.getInputAlphabet().size() + ',' + nfa.size();
        }
    }
}
