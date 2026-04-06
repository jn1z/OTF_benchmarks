package OTF_benchmark;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import OTF.Model.Threshold;
import OTF.NFATrim;
import OTF.OTFDeterminization;
import OTF.Registry.Registry;
import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import OTF.Simulation.ParallelSimulation;

public class BenchmarkOTF implements IBench {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkOTF.class);

    private final IInput input;
    private final Supplier<Threshold> thresholdSupplier;
    private final BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator;
    private final Logger logger;
    private final boolean bisim;
    private final boolean simulation;

    public BenchmarkOTF(IInput input,
                        Supplier<Threshold> thresholdSupplier,
                        boolean bisim,
                        boolean simulation,
                        BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator) {
        this.input = input;
        this.thresholdSupplier = thresholdSupplier;
        this.indexCreator = indexCreator;
        final Threshold threshold = thresholdSupplier.get();
        this.bisim = bisim;
        this.simulation = simulation;
        this.logger =
                Utils.getLogger(this, input, threshold.getName() + threshold.getParam() + indexCreator.toString());
    }

    @Override
    public String id() {
        return simulation ? "sim_otf" : "otf";
    }

    @Override
    public Iterator<Runnable> warmups() {
        logger.info("{},{}", this.input.header(), LogData.ALL_LOGDATA_HEADERS);
        return Iterators.transform(this.input.warmups(),
                                   configuration -> new OTFJob(configuration,
                                       thresholdSupplier.get(),
                                       bisim,
                                       simulation,
                                       indexCreator,
                                       LOGGER));
    }

    @Override
    public Iterator<Runnable> jobs() {
        return Iterators.transform(this.input.jobs(),
                                   configuration -> new OTFJob(configuration,
                                       thresholdSupplier.get(),
                                       bisim,
                                       simulation,
                                       indexCreator,
                                       logger));
    }

    static class OTFJob implements Runnable {

        private final IConf<Integer> config;
        private final Threshold threshold;
        private final BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator;
        private final Logger logger;
        private final boolean bisim;
        private final boolean simulation;

        public OTFJob(IConf<Integer> config,
                      Threshold threshold,
                      boolean bisim,
                      boolean simulation,
                      BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator,
                      Logger logger) {
            this.config = config;
            this.threshold = threshold;
            this.bisim = bisim;
            this.simulation = simulation;
            this.indexCreator = indexCreator;
            this.logger = logger;
        }

        @Override
        public void run() {
            CompactNFA<Integer> trimMaybeBiSimMaybeSim = NFATrim.trim(config.buildNFA(), config.buildAlphabet(), CompactNFA::new);
            final Alphabet<Integer> alphabet = trimMaybeBiSimMaybeSim.getInputAlphabet();

            long before, after;
            LogData logData = new LogData(this.config, this.logger);
            logData.sizeTrim = trimMaybeBiSimMaybeSim.size();

            if (bisim) {
                before = System.nanoTime();
                trimMaybeBiSimMaybeSim = NFATrim.bisim(trimMaybeBiSimMaybeSim);
                after = System.nanoTime();
                logData.sizeBiSim = trimMaybeBiSimMaybeSim.size();
                logData.timeBiSim = (after - before);
            }

            ArrayList<BitSet> simRels = new ArrayList<>();
            if (this.simulation) {
                before = System.nanoTime();
                trimMaybeBiSimMaybeSim = ParallelSimulation.fullyComputeRels(trimMaybeBiSimMaybeSim, simRels, true);
                //reducedTV = OLRTSimulation.fullyComputeRels(reducedTV, simRels);
                after = System.nanoTime();
                logData.sizeSim = trimMaybeBiSimMaybeSim.size();
                logData.timeSim = (after - before);
                int simRelCount = 0;
                for(BitSet b: simRels) {
                    if (b != null) {
                        simRelCount += b.cardinality();
                    }
                }
                logData.simRels = simRelCount;
            }

            final Registry registry = indexCreator.apply(trimMaybeBiSimMaybeSim, simRels.toArray(new BitSet[0]));;
            logData.index = registry.toString();

            simRels.clear(); // Help GC

            before = System.nanoTime();

            final DFA<?, Integer> dfa =
                OTFDeterminization.doOTF(trimMaybeBiSimMaybeSim.powersetView(), alphabet, threshold, registry);
            trimMaybeBiSimMaybeSim.clear(); // Help GC

            logData.threshold = threshold.getName();
            logData.thresholdParam = threshold.getParam();
            logData.thresholdCross = threshold.getCrossings();

            after = System.nanoTime();
            logData.sizeSC1 = dfa.size();
            logData.maxInter = registry.getMaxIntermediateCount();
            logData.timeSC1 = (after-before);
            before = System.nanoTime();
            final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa, alphabet);
            after = System.nanoTime();
            logData.sizeSC1Min = min.size();
            logData.timeSC1Min = (after-before);
        }
    }
}
