package OTF_benchmark;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputTVLinear implements IInput {

    private final static Logger LOGGER = LoggerFactory.getLogger(InputTVLinear.class);

    private final static int NUM = 1000;

    private final static int SIZE_MIN = 30; // Distance restrictions require larger starting n
    private final static int SIZE_MAX = 250;
    private final static int SIZE_INC = 10;

    private final static float TD_MIN = 1.2f;
    private final static float TD_MAX = 3f;
    private final static float TD_INC = 0.1f;

    public final static float AD = 0.5f;
    public final static float DF = 0.2f;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));

    private final static int WARMUP = 60_000;
    private final static double SATURATION = 0.8d;

    private final static Alphabet<Integer> alphabet = Alphabets.integers(0, 1);

    private final static List<IConf<Integer>> CONFIGS;

    static {
        final int capacity = (int) ((TD_MAX - TD_MIN) / TD_INC + 1) * ((SIZE_MAX - SIZE_MIN) / SIZE_INC + 1);
        CONFIGS = new ArrayList<>(capacity);

        LOGGER.info("Starting generation ...");

        for (int size = SIZE_MIN; size <= SIZE_MAX; size += SIZE_INC) {
            for (float td = TD_MIN; td <= TD_MAX; td += TD_INC) {
                int seed = -1;
                for (int num = 0; num < NUM; num++) {
                    CompactNFA<Integer> nfa;
                    BitSet states;
/*
                    do {
                        nfa = TVNFAWithLocality.generateNFA(new Random(++seed),
                                                                size,
                                                                td,
                                                                AD,
                                                                DF,
                                                                alphabet,
                                                                CompactNFA::new);

                        states = new BitSet();
                        states.or(NFATrim.leftTrim(nfa, CompactNFA::new));
                        states.and(NFATrim.rightTrim(nfa));
                    } while (states.cardinality() < SATURATION * size);
*/
                    /*if (seed == 122) {
                        System.out.println(
                                "size:" + size + ", td:" + td +
                                        " trim size:" + states.cardinality());
                    }*/
                    //CONFIGS.add(new Configuration(seed, td, size));
                }
            }
        }

        LOGGER.info("... done");
    }

    @Override
    public String id() {
        return "tvLinear";
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
/*
    private record Configuration(int seed, float td, int size) implements IConf<Integer> {


        @Override
        public CompactNFA<Integer> buildNFA() {
            return TVNFAWithLocality.generateNFA(new Random(seed),
                    size,
                    td,
                    AD,
                    DF,
                    alphabet,
                    CompactNFA::new);
        }

        @Override
        public String getConfig() {
            return seed + "," + DECIMAL_FORMAT.format(td) + "," + size;
        }
    }*/
}
