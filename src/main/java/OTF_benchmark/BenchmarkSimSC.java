package OTF_benchmark;

import OTF.Model.Cancellation;
import OTF.Model.Threshold;
import OTF.NFATrim;
import OTF.OTFDeterminization;
import OTF.Registry.AntichainForestRegistry;
import OTF.Registry.Registry;
import OTF.Simulation.ParallelSimulation;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkSimSC implements IBench {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkSimSC.class);

    private final IInput input;
    private final Logger logger;
    private final boolean bisim;

    public BenchmarkSimSC(IInput input) {
        this(input, true);
    }

    public BenchmarkSimSC(IInput input,
                          boolean bisim) {
        this.input = input;
        this.bisim = bisim;
        this.logger = Utils.getLogger(this, input);
    }

    @Override
    public String id() {
        return "sim_sc";
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

    private static class SCJob<I> extends AbstractJob {
        private final IConf<Integer> config;
        private final Logger logger;
        private final boolean bisim;

        public SCJob(IConf<Integer> config,
                     boolean bisim, Logger logger) {
            this.config = config;
            this.logger = logger;
            this.bisim = bisim;
        }

        @Override
        public void run() {
            CompactNFA<Integer> trimMaybeBiSim = NFATrim.trim(config.buildNFA(), CompactNFA::new);
            final Alphabet<Integer> alphabet = trimMaybeBiSim.getInputAlphabet();

            final Cancellation cancellation = super.initCancellation(Thresholds.paigeTarjan(alphabet.size()));

            long before, after;
            LogData logData = new LogData();
            logData.sizeTrim = trimMaybeBiSim.size();

            if (bisim) {
                before = System.nanoTime();
                trimMaybeBiSim = NFATrim.bisim(trimMaybeBiSim);
                after = System.nanoTime();
                logData.sizeBiSim = trimMaybeBiSim.size();
                logData.timeBiSim = (after - before);
            }

            ArrayList<BitSet> simRels = new ArrayList<>();
            before = System.nanoTime();
            trimMaybeBiSim = ParallelSimulation.fullyComputeRels(trimMaybeBiSim, simRels, true);
            after = System.nanoTime();
            logData.sizeSim = trimMaybeBiSim.size();
            logData.timeSim = (after - before);

            Threshold threshold = Threshold.noop();
            Registry registry = new AntichainForestRegistry<>(trimMaybeBiSim, simRels.toArray(new BitSet[0]));

            before = System.nanoTime();

            final DFA<?, Integer> dfa =
                OTFDeterminization.doOTF(trimMaybeBiSim.powersetView(), alphabet, threshold, registry, cancellation);

            if (!cancellation.isCancelled()) {
                after = System.nanoTime();
                logData.sizeSC1 = dfa.size();
                logData.timeSC1 = (after-before);
                before = System.nanoTime();
                final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa, alphabet);
                after = System.nanoTime();
                logData.sizeSC1Min = min.size();
                logData.timeSC1Min = (after-before);
            } else {
                logData.cancel = cancellation.cancelLabel();
            }

            logger.info("{},{}", config.getConfig(), logData);

            cancellation.cancel();
        }
    }
}
