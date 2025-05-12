package OTF_benchmark;

import java.util.Iterator;

import OTF.Model.Cancellation;
import OTF.NFATrim;
import OTF.PowersetDeterminizer;
import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkBrz implements IBench {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkBrz.class);

    private final IInput input;
    private final Logger logger;
    private final boolean bisim;

    public BenchmarkBrz(IInput input) {
        this(input, true);
    }

    public BenchmarkBrz(IInput input, boolean bisim) {
        this.input = input;
        this.bisim = bisim;
        this.logger = Utils.getLogger(this, input);
    }

    @Override
    public String id() {
        return "brz";
    }

    @Override
    public Iterator<Runnable> warmups() {
        logger.info("{},{}", this.input.header(), LogData.ALL_LOGDATA_HEADERS);
        return Iterators.transform(this.input.warmups(), configuration -> new BrzJob(configuration, bisim, LOGGER));
    }

    @Override
    public Iterator<Runnable> jobs() {
        return Iterators.transform(this.input.jobs(), configuration -> new BrzJob(configuration, bisim, logger));
    }

    private static class BrzJob extends AbstractJob {

        private final IConf<Integer> config;
        private final Logger logger;
        private final boolean bisim;

        public BrzJob(IConf<Integer> config, boolean bisim, Logger logger) {
            this.config = config;
            this.logger = logger;
            this.bisim = bisim;
        }

        @Override
        public void run() {
            final CompactNFA<Integer> trim = NFATrim.trim(config.buildNFA(), CompactNFA::new);
            final Alphabet<Integer> alphabet = trim.getInputAlphabet();

            final Cancellation cancellation = super.initCancellation(Thresholds.paigeTarjan(alphabet.size()));
            final PowersetDeterminizer determinizer = new PowersetDeterminizer(cancellation);

            long before, after;
            LogData logData = new LogData();
            logData.sizeTrim = trim.size();

            CompactNFA<Integer> rev1MaybeBisim = NFATrim.reverse(trim, CompactNFA::new);

            if (this.bisim) {
                before = System.nanoTime();
                rev1MaybeBisim = NFATrim.bisim(rev1MaybeBisim);
                after = System.nanoTime();
                logData.sizeBiSim = rev1MaybeBisim.size();
                logData.timeBiSim = (after-before);
            }

            before = System.nanoTime();
            final CompactDFA<Integer> dfa1 = determinizer.benchmark(rev1MaybeBisim, alphabet);

            if (!cancellation.isCancelled()) {
                after = System.nanoTime();
                logData.sizeSC1 = dfa1.size();
                logData.timeSC1 = (after-before);

                // no timing here
                final CompactDFA<Integer> min = HopcroftMinimizer.minimizeDFA(dfa1);
                logData.sizeSC1Min = min.size();

                final CompactNFA<Integer> rev2 = NFATrim.reverse(dfa1, alphabet);

                before = System.nanoTime();
                final CompactDFA<Integer> dfa2 = determinizer.benchmark(rev2, alphabet);

                if (!cancellation.isCancelled()) {
                    after = System.nanoTime();
                    logData.sizeSC2 = dfa2.size();
                    logData.timeSC2 = (after-before);

                    // no timing here
                    final CompactDFA<Integer> min2 = HopcroftMinimizer.minimizeDFA(dfa2);
                    logData.sizeSC2Min = min2.size(); // should equal sizeSC2
                } else {
                    logData.cancel = cancellation.cancelLabel();
                }
            } else {
                //final CompactDFA<I> min = cancellation.isOom() ? null : PaigeTarjanMinimization.minimizeDFA(dfa1);
                logData.cancel = cancellation.cancelLabel();
            }

            logger.info("{},{}", config.getConfig(), logData);

            cancellation.cancel();
        }
    }
}
