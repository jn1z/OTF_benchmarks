package OTF_benchmark;

import java.io.Reader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;

import OTF.Model.CompactImpl;
import OTF.Model.FastImpl;
import OTF.Model.SupportsCompactPowerset;
import OTF_benchmark.parser.SimpleMataParser;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.common.util.IOUtil;

class InputDebug implements IInput {

    @Override
    public String id() {
        return "debug";
    }

    @Override
    public String header() {
        return "name,inputs,size";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return Collections.<IConf<Integer>>singleton(new Configuration()).iterator();
    }

    private static class Configuration implements IConf<Integer> {

        private final String resource;
        private NFA<?, Integer> nfa;
        private Alphabet<Integer> alphabet;

        private Configuration() {
//            this.resource = "/home/frohme/tmp/nfa-bench/benchmarks/regexps/regexps_union/http-2612_http-2612.mata";
//            this.resource = "/home/frohme/tmp/nfa-bench/benchmarks/regexps/regexps_union/http-2604_http-2604.mata";
            this.resource = "/home/frohme/tmp/nfa-bench/benchmarks/regexps/regexps_union/http-1503_http-1503.mata";
        }

        @Override
        public NFA<?, Integer> buildNFA() {
            try (Reader r = IOUtil.asBufferedUTF8Reader(Path.of(resource).toFile())) {
                final SimpleMataParser parser = new SimpleMataParser(r);
                parser.parse();

                this.nfa = parser.getNFA();
                this.alphabet = parser.getAlphabet();
            } catch (Exception e) {
                throw new RuntimeException(e);
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
            return resource.substring(resource.lastIndexOf('/'), resource.lastIndexOf('.'));
        }
    }
}
