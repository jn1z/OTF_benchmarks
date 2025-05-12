package OTF_benchmark;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import OTF.NFATrim;
import com.google.common.collect.Iterators;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputTVLanguageProjection implements IInput {

    private final static Logger LOGGER = LoggerFactory.getLogger(InputTVLanguageProjection.class);

    private final static int NUM = 100;

    private final static int SIZE_MIN = 10;
    private final static int SIZE_MAX = 50;
    private final static int SIZE_INC = 10;

    // Alphabet size. Could be even bigger!
    private final static int ALPH_SIZE_MIN = 10;
    private final static int ALPH_SIZE_MAX = 50;
    private final static int ALPH_SIZE_INC = 10;

    // Compression factor of language, e.g., 2x - 5x.
    // Could be bigger or smaller (1.5x ?), but I think smaller has little effect, and larger becomes trivial
    private final static float CF_MIN = 2f;
    private final static float CF_MAX = 5f;
    private final static float CF_INC = 1f;

    private final static int WARMUP = NUM * 10;
    private final static double SATURATION = 0.8d;

    private final static float AF = 0.5f;

    private final static List<IConf<Integer>> CONFIGS;

    static {
        final int capacity = Math.multiplyExact(NUM,
                                                Math.multiplyExact((int) ((CF_MAX - CF_MIN) / CF_INC) + 1,
                                                                   Math.floorDiv(SIZE_MAX - SIZE_MIN, SIZE_INC) + 1));
        CONFIGS = new ArrayList<>(capacity);

        LOGGER.info("Starting generation ...");

        for (int size = SIZE_MIN; size <= SIZE_MAX; size += SIZE_INC) {
            for (int alphSize = ALPH_SIZE_MIN; alphSize <= ALPH_SIZE_MAX; alphSize += ALPH_SIZE_INC) {
                for (float cf = CF_MIN; cf <= CF_MAX; cf += CF_INC) {
                    int seed = -1;

                    for (int num = 0; num < NUM; num++) {
                        CompactNFA<Integer> nfa;
                        CompactNFA<Integer> nfaTrim;
                        do {
                            nfa = TVLanguageProjection.randomNFA(new Random(++seed), size, alphSize, cf, AF);
                            nfaTrim = NFATrim.trim(nfa);
                        } while (nfaTrim.size() < SATURATION * size);

                        CONFIGS.add(new Configuration(seed, cf, size, alphSize));
                    }
                }
            }
        }

        LOGGER.info("... done");
    }

    @Override
    public String id() {
        return "tv-proj";
    }

    @Override
    public String header() {
        return "seed,cf,size,alphSize";
    }

    @Override
    public Iterator<IConf<Integer>> warmups() {
        return Iterators.limit(jobs(), WARMUP);
    }

    @Override
    public Iterator<IConf<Integer>> jobs() {
        return CONFIGS.iterator();
    }

    private record Configuration(int seed, float cf, int size, int alphSize) implements IConf<Integer> {

        private static final DecimalFormat DF = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

        @Override
        public CompactNFA<Integer> buildNFA() {
            return TVLanguageProjection.randomNFA(new Random(seed), size, alphSize, cf, AF);
        }

        @Override
        public String getConfig() {
            return seed + "," + DF.format(cf) + "," + size + "," + alphSize;
        }
    }
}
