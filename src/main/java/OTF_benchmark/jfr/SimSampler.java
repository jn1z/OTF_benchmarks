package OTF_benchmark.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Set;

import OTF.NFATrim;
import OTF.OTFDeterminization;
import OTF.Simulation.ParallelSimulation;
import OTF.Simulation.NaiveSimulation;
import OTF_benchmark.jmh.BisimState;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.IOUtil;

public class SimSampler {

    public static void main(String[] args) throws IOException, ParseException {

        final Configuration configuration =
            Configuration.create(IOUtil.asBufferedUTF8Reader(OTFDeterminization.class.getClassLoader()
                .getResourceAsStream(
                    "custom.jfc")));

        final BisimState state = new BisimState();
        state.setup();

        CompactNFA<Integer> src = state.getNfa();
        ParallelSimulation.unifyInitialStatesWithoutTrim(src);

        CompactNFA<Integer> nfa;
        Set<IntIntPair> rels;

        System.err.println("go");

        try (
            Recording recSim = new Recording(configuration)) {

            if (src.size() > 1) {

                recSim.setDestination(Path.of("rabit.jfr"));
                recSim.start();

                nfa = NFATrim.trim(src);
                rels = NaiveSimulation.computeDirectSimulation(nfa, true, true);

                recSim.stop();

                System.err.println(rels.size());
            } else {
                System.out.println("skipped");
            }
        }
    }
}