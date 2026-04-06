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
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import OTF.Simulation.ParallelSimulation;

public class BenchmarkOTFBrz implements IBench {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkOTFBrz.class);

    private final IInput input;
    private final Supplier<Threshold> thresholdSupplier;
    private final BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator;
    private final Logger logger;
    private final boolean bisim;
    private final boolean simulation;

    public BenchmarkOTFBrz(IInput input,
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
        return simulation ? "sim_brz-otf" : "brz-otf";
    }

    @Override
    public Iterator<Runnable> warmups() {
        logger.info("{},{}", this.input.header(), LogData.ALL_LOGDATA_HEADERS);
        return Iterators.transform(this.input.warmups(),
                                   configuration -> new OTFBrzJob(configuration,
                                       thresholdSupplier.get(),
                                       bisim,
                                       simulation,
                                       indexCreator,
                                       LOGGER));
    }

    @Override
    public Iterator<Runnable> jobs() {
        return Iterators.transform(this.input.jobs(),
                                   configuration -> new OTFBrzJob(configuration,
                                       thresholdSupplier.get(),
                                       bisim,
                                       simulation,
                                       indexCreator,
                                       logger));
    }

    static class OTFBrzJob implements Runnable {

        private final IConf<Integer> config;
        private final Threshold threshold;
        private final BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator;
        private final Logger logger;
        private final boolean bisim;
        private final boolean simulation;

        public OTFBrzJob(IConf<Integer> config,
                         Threshold threshold,
                         boolean bisim,
                         boolean simulation,
                         BiFunction<NFA<?, Integer>, BitSet[], Registry> indexCreator,
                         Logger logger) {
            this.config = config;
            this.threshold = threshold;
            this.indexCreator = indexCreator;
            this.logger = logger;
            this.bisim = bisim;
            this.simulation = simulation;
        }

        @Override
        public void run() {
            final CompactNFA<Integer> trim = NFATrim.trim(config.buildNFA(), config.buildAlphabet(), CompactNFA::new);
            final Alphabet<Integer> alphabet = trim.getInputAlphabet();

            long before, after;
            LogData logData = new LogData(this.config, this.logger);
            logData.sizeTrim = trim.size();

            CompactNFA<Integer> rev1MaybeBisimMaybeSim = NFATrim.reverse(trim, CompactNFA::new);

            if (this.bisim) {
                before = System.nanoTime();
                rev1MaybeBisimMaybeSim = NFATrim.bisim(rev1MaybeBisimMaybeSim);
                after = System.nanoTime();
                logData.sizeBiSim = rev1MaybeBisimMaybeSim.size();
                logData.timeBiSim = (after-before);
            }

            ArrayList<BitSet> simRels = new ArrayList<>();
            if (this.simulation) {
                before = System.nanoTime();
                rev1MaybeBisimMaybeSim = ParallelSimulation.fullyComputeRels(rev1MaybeBisimMaybeSim, simRels, true);
                after = System.nanoTime();
                logData.sizeSim = rev1MaybeBisimMaybeSim.size();
                logData.timeSim = (after - before);
            }

            final Registry registry = indexCreator.apply(rev1MaybeBisimMaybeSim, simRels.toArray(new BitSet[0]));
            logData.index = registry.toString();

            simRels.clear(); // Help GC

            //final CompactNFA<Integer> reduced = index.getReducedNFA(rev1);
            final CompactNFA<Integer> reduced = rev1MaybeBisimMaybeSim;

            final long before2 = System.nanoTime();
            DFA<Integer, Integer> dfa1 =
                OTFDeterminization.doOTF(reduced.powersetView(), alphabet, threshold, registry);

            logData.threshold = threshold.getName();
            logData.thresholdParam = threshold.getParam();
            logData.thresholdCross = threshold.getCrossings();

            final long after2 = System.nanoTime();
            logData.sizeSC1 = dfa1.size();
            logData.maxInter = registry.getMaxIntermediateCount();
            logData.timeSC1 = after2 - before2;

            // no timing here
            final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa1, alphabet);
            logData.sizeSC1Min = min.size();

            final CompactNFA<Integer> rev2 = NFATrim.reverse(dfa1, alphabet, new CompactNFA.Creator<>());

            dfa1 = null; // Help GC

            final long before3 = System.nanoTime();
            final CompactDFA<Integer> dfa2 = NFAs.determinize(rev2, alphabet, false, false);
            rev2.clear(); // Help GC

            final long after3 = System.nanoTime();
            logData.sizeSC2 = dfa2.size();
            logData.timeSC2 = after3 - before3;

            // no timing here
            final CompactDFA<Integer> min2 = HopcroftMinimizer.minimizeDFA(dfa2);
            logData.sizeSC2Min = min2.size(); // should equal sizeSC2
        }
    }
}
