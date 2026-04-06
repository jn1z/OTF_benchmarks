package OTF_benchmark;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import OTF.NFATrim;
import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputTV implements IInput {

    private final static Logger LOGGER = LoggerFactory.getLogger(InputTV.class);

    private final static int NUM = 100;

    private final static int SIZE_MIN = 20;
    private final static int SIZE_MAX = 200;
    private final static int SIZE_INC = 20;

    private final static int WARMUP = NUM * 4;
    private final static double SATURATION = 0.8d;

    private final static Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
    private final static List<IConf<Integer>> CONFIGS;

    static {
        final int capacity = Math.multiplyExact(NUM, Math.floorDiv(SIZE_MAX - SIZE_MIN, SIZE_INC) + 1);
        CONFIGS = new ArrayList<>(capacity);

        LOGGER.info("Starting generation ...");

        for (int size = SIZE_MIN; size <= SIZE_MAX; size += SIZE_INC) {
            int seed = -1;
            float td = 1.25f;

            for (int num = 0; num < NUM; num++) {
                CompactNFA<Integer> nfa;
                CompactNFA<Integer> nfaTrim;
                do {
                    nfa = TabakovVardiRandomNFA.generateNFA(new Random(++seed), size, td, 0.5f, alphabet, CompactNFA::new);
                    nfaTrim = NFATrim.trim(nfa);
                } while (nfaTrim.size() < SATURATION * size);

                CONFIGS.add(new Configuration(seed, td, size));
            }
        }

        LOGGER.info("... done");
    }

    @Override
    public String id() {
        return "tv";
    }

    @Override
    public String header() {
        return "seed,td,size";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Iterators.limit(jobs(), WARMUP);
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return CONFIGS.iterator();
    }

    private record Configuration(int seed, float td, int size) implements IConf<Integer> {

        private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

        @Override
        public CompactNFA<Integer> buildNFA() {
            return TabakovVardiRandomNFA.generateNFA(new Random(seed), size, td, 0.5f, alphabet, CompactNFA::new);
        }

        @Override
        public Alphabet<Integer> buildAlphabet() {
            return alphabet;
        }

        @Override
        public String getConfig() {
            return seed + "," + DF.format(td) + "," + size;
        }
    }
}
