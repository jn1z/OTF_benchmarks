package OTF_benchmark;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import OTF.NFATrim;
import OTF.Simulation.ParallelSimulation;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.serialization.ba.BAWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTVWithMod {

  private final static Logger LOGGER = LoggerFactory.getLogger(WriteTVWithMod.class);

  private final static int NUM = 10;

  private final static int SIZE_MIN = 20;
  private final static int SIZE_MAX = 300;
  private final static int SIZE_INC = 10;

  private final static float TD_MIN = 2.0f;
  private final static float TD_MAX = 2.0f;
  private final static float TD_INC = 0.1f;

  private final static double SATURATION = 0.8d;


  public static void writeBAModels() {
    LOGGER.info("Starting generation ...");

    for (int size = SIZE_MIN; size <= SIZE_MAX; size += SIZE_INC) {
      for (float td = TD_MIN; td <= TD_MAX; td += TD_INC) {
        int seed = -1;

        for (int num = 0; num < NUM; num++) {
          CompactNFA<Integer> nfa;
          CompactNFA<Integer> nfaTrim;
          do {
            nfa = TabakovVardiRandomNFAWithMod.generateNFA(++seed, size, td);
            nfaTrim = NFATrim.trim(nfa);
          } while (nfaTrim.size() < SATURATION * size);
          // export NFA to BA format
          String fileName = "systems/randomModular/";
          fileName += "nfa_" + size + "_" + td + "_" + seed + ".ba";
          LOGGER.info("writing file: " + fileName);

          // We have to unify initial states before writing to BA format!
          ParallelSimulation.unifyInitialStatesWithoutTrim(nfa);

          BAWriter<Integer> baWriter = new BAWriter<>();
          try {
            OutputStream os = new FileOutputStream(fileName);
            baWriter.writeModel(os, nfa, nfa.getInputAlphabet());
            os.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    LOGGER.info("... done");
  }

}
