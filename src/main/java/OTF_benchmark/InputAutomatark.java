package OTF_benchmark;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import OTF_benchmark.parser.TimbukParser;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputAutomatark implements IInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputAutomatark.class);

    private static final Map<String, CompactNFA<Integer>> NFAS;

    private static final String PREFIX = "/automatark/NFA/";

    static {
        try {
            final List<String> resources = Utils.findResources(PREFIX, s -> (s.endsWith(".timbuk")));
            NFAS = Maps.newLinkedHashMapWithExpectedSize(resources.size());

            for (String resource : resources) {
                LOGGER.info("Loading '{}'", resource);

                try (Reader r = IOUtil.asBufferedUTF8Reader(InputAutomatark.class.getResourceAsStream(resource))) {
                    final TimbukParser parser = new TimbukParser(r);
                    parser.parse();

                    final CompactNFA<String> nfa = parser.getNFA();
                    final CompactNFA<Integer> translated =
                            nfa.translate(Alphabets.integers(0, nfa.getInputAlphabet().size() - 1));
                    final String file = resource.substring(PREFIX.length(), resource.lastIndexOf('.'));

                    NFAS.put(file, translated);
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
        return "automatark";
    }

    @Override
    public String header() {
        return "name,inputs,size";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Iterators.limit(jobs(), 100);
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return Iterators.transform(NFAS.entrySet().iterator(), InputAutomatark::toConfiguration);
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
        public Alphabet<I> buildAlphabet() {
            return nfa.getInputAlphabet();
        }

        @Override
        public String getConfig() {
            return name + ',' + nfa.getInputAlphabet().size() + ',' + nfa.size();
        }
    }
}
