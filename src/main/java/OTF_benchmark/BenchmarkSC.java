package OTF_benchmark;

import java.util.Iterator;

import OTF.NFATrim;
import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkSC implements IBench {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkSC.class);

    private final IInput input;
    private final Logger logger;
    private final boolean bisim;

    public BenchmarkSC(IInput input) {
        this(input, true);
    }

    public BenchmarkSC(IInput input,
                       boolean bisim) {
        this.input = input;
        this.bisim = bisim;
        this.logger = Utils.getLogger(this, input);
    }

    @Override
    public String id() {
        return "sc";
    }

    @Override
    public Iterator<Runnable> warmups() {
        logger.info("{},{}", this.input.header(), LogData.ALL_LOGDATA_HEADERS);
        return Iterators.transform(this.input.warmups(), configuration ->
            new SCJob<>(configuration, bisim, LOGGER));
    }

    @Override
    public Iterator<Runnable> jobs() {
        return Iterators.transform(this.input.jobs(), configuration ->
            new SCJob<>(configuration, bisim, logger));
    }

    static class SCJob<I> implements Runnable {
        private final IConf<I> config;
        private final Logger logger;
        private final boolean bisim;

        public SCJob(IConf<I> config,
                     boolean bisim, Logger logger) {
            this.config = config;
            this.logger = logger;
            this.bisim = bisim;
        }

        @Override
        public void run() {
            CompactNFA<I> trimMaybeBiSim = NFATrim.trim(config.buildNFA(), config.buildAlphabet(), CompactNFA::new);
            final Alphabet<I> alphabet = trimMaybeBiSim.getInputAlphabet();

            long before, after;
            LogData logData = new LogData(this.config, this.logger);
            logData.sizeTrim = trimMaybeBiSim.size();

            if (bisim) {
                before = System.nanoTime();
                trimMaybeBiSim = NFATrim.bisim(trimMaybeBiSim);
                after = System.nanoTime();
                logData.sizeBiSim = trimMaybeBiSim.size();
                logData.timeBiSim = (after - before);
            }

            before = System.nanoTime();
            final CompactDFA<I> dfa = NFAs.determinize(trimMaybeBiSim, alphabet, false, false);
            trimMaybeBiSim.clear(); // Help GC

            after = System.nanoTime();
            logData.sizeSC1 = dfa.size();
            logData.timeSC1 = (after-before);
            before = System.nanoTime();

            final CompactDFA<I> min = HopcroftMinimizer.minimizeDFA(dfa, alphabet);
            after = System.nanoTime();
            logData.sizeSC1Min = min.size();
            logData.timeSC1Min = (after-before);
        }
    }
}
