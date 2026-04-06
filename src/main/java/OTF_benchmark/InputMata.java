package OTF_benchmark;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import OTF.Model.CompactImpl;
import OTF.Model.FastImpl;
import OTF.Model.SupportsCompactPowerset;
import OTF_benchmark.parser.SimpleMataParser;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.common.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputMata implements IInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputMata.class);

    private static final Set<String> NFAS;

    private static final String PREFIX = "/nfa-bench/benchmarks/";

    static {
        try {
            final List<String> resources = Utils.findResources(PREFIX,
                                                               Predicates.and(Predicates.or(s -> s.contains("z3"),
                                                                                            s -> s.contains("w1s1"),
                                                                                            s -> s.contains("regexps"),
                                                                                            s -> s.contains("presburger-explicit/complement/UltimateAutomizer")),
                                                                              s -> !s.contains("instance08074.mata"),// multiple sections
                                                                              s -> !s.contains("instance14064.mata"),// multiple sections
                                                                              s -> !s.contains("instance10092.mata"),// multiple sections
                                                                              s -> !s.contains("instance14085.mata"),// multiple sections
                                                                              s -> !s.contains("instance14668.mata"),// multiple sections
                                                                              s -> !s.contains("instance06264.mata"),// multiple sections
                                                                              s -> !s.contains("instance12143.mata"),// multiple sections
                                                                              s -> !s.contains("instance08029.mata"),// multiple sections
                                                                              s -> !s.contains("http-2612_http-2612.mata"),// OOM
                                                                              s -> !s.contains("http-2604_http-2604.mata"),// OOM
                                                                              s -> !s.contains("http-1503_http-1503.mata"),// OOM
                                                                              s -> (s.endsWith(".mata"))));
            NFAS = Sets.newLinkedHashSetWithExpectedSize(resources.size());

            for (String resource : resources) {
                LOGGER.info("Loading '{}'", resource);
                NFAS.add(resource);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String id() {
        return "mata";
    }

    @Override
    public String header() {
        return "name,inputs,size";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Iterators.limit(jobs(), 1);
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return Iterators.transform(NFAS.iterator(), Configuration::new);
        //return Iterators.singletonIterator(new Configuration(NFAS.iterator().next()));
    }

    static class Configuration implements IConf<Integer> {

        private final String resource;
        private NFA<?, Integer> nfa;
        private Alphabet<Integer> alphabet;

        private Configuration(String resource) {
            this.resource = resource;
        }

        Configuration(Path path) {
            this.resource = path.getFileName().toString();
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                final SimpleMataParser parser = new SimpleMataParser(r);
                parser.parse();

                this.nfa = parser.getNFA();
                this.alphabet = parser.getAlphabet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public NFA<?, Integer> buildNFA() {
            if (this.nfa == null) {
                try (Reader r = IOUtil.asBufferedUTF8Reader(InputMata.class.getResourceAsStream(resource))) {
                    final SimpleMataParser parser = new SimpleMataParser(r);
                    parser.parse();

                    this.nfa = parser.getNFA();
                    this.alphabet = parser.getAlphabet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return nfa;
        }

        @Override
        public Alphabet<Integer> buildAlphabet() {
            return this.alphabet;
        }

        @Override
        public AutomatonCreator<SupportsCompactPowerset<?, Integer>, Integer> builder() {
            return this.nfa.size() < 2_500 ? CompactImpl::new : FastImpl::new;
        }

        @Override
        public String getConfig() {
            return getName() + ',' + alphabet.size() + ',' + nfa.size();
        }

        private String getName() {
            if (resource.contains("/")) {
                return resource.substring(resource.lastIndexOf('/'), resource.lastIndexOf('.'));
            } else {
                return resource;
            }
        }
    }
}
