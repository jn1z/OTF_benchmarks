package OTF_benchmark.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

import OTF.OTFDeterminization;

import OTF_benchmark.PT.ValmariDet;
import OTF_benchmark.PT.ValmariExtractors;
import OTF_benchmark.PT.ValmariInitializers;
import OTF_benchmark.jmh.BisimState;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;

public class PTSampler {

    public static void main(String[] args) throws IOException, ParseException {

        final Configuration configuration =
                Configuration.create(IOUtil.asBufferedUTF8Reader(OTFDeterminization.class.getClassLoader()
                                                                                              .getResourceAsStream(
                                                                                                  "custom.jfc")));

        final BisimState state = new BisimState();
        state.setup();

        final CompactDFA<Integer> dfa = state.getDfa();

        System.err.println("go");

        try (Recording recPT = new Recording(configuration);
             Recording recVal = new Recording(configuration)) {

            recPT.setDestination(Path.of("pt.jfr"));
            recPT.start();

            final CompactDFA<Integer> minPT1 = HopcroftMinimizer.minimizeDFA(dfa, dfa.getInputAlphabet());

            recPT.stop();
            recVal.setDestination(Path.of("val.jfr"));
            recVal.start();

            ValmariDet valmari = ValmariInitializers.initDet(dfa, dfa.getInputAlphabet());
            valmari.computeCoarsestStablePartition();
            CompactDFA<Integer> minVal = ValmariExtractors.toDFA(valmari, dfa.getInputAlphabet());

            recVal.stop();

            System.err.println(minPT1.size());
            System.err.println(minVal.size());
        }
    }
}
