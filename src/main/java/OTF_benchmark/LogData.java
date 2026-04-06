package OTF_benchmark;

import org.slf4j.Logger;

public class LogData {
      String threshold, thresholdParam;
      Integer thresholdCross;
      String index;
      Integer sizeTrim, sizeBiSim, sizeSim, simRels, sizeSC1, sizeSC1Min, sizeSC2, sizeSC2Min, maxInter;
      Long timeBiSim, timeSim, timeSC1, timeSC1Min, timeSC2, timeSC2Min, timeTotal;

  final static String ALL_LOGDATA_HEADERS =
      "threshold,threshold_param,threshold_cross,"
          +"index,"
          +"size_trim,size_bisim,"
          +"size_sim,sim_rels,size_sc1,size_sc1_min,"
          +"size_sc2,size_sc2_min,max_inter,"
          +"time_bisim,time_sim,time_sc1,time_sc1_min,time_sc2,time_sc2_min,time_total,cancel";
  // no time trim or reverse, considered trivial
  // size_sc2_min == size_sc2 for Brz variants

    public LogData(IConf<?> config, Logger logger) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("{},{}", config.getConfig(), this)));
    }

    private static String strVal(Object o) {
    return o == null ? "": o.toString();
  }

  private static Long longVal(Long... o) {
      long result = 0;
      for (Long l : o) {
        if (l != null) { // timed out at one point
          result += l;
        }
      }
      return result;
  }

  @Override
  public String toString() {
    timeTotal = longVal(timeBiSim, timeSim, timeSC1, timeSC1Min, timeSC2, timeSC2Min);
    return strVal(threshold) + "," + strVal(thresholdParam) + "," + strVal(thresholdCross) + "," +
        strVal(index) + "," +
        strVal(sizeTrim) + "," + strVal(sizeBiSim) + "," +
        strVal(sizeSim) + "," + strVal(simRels) + "," + strVal(sizeSC1) + "," + strVal(sizeSC1Min) + "," +
        strVal(sizeSC2) + "," + strVal(sizeSC2Min) + "," + strVal(maxInter) + "," +
        strVal(timeBiSim) + "," + strVal(timeSim) + "," + strVal(timeSC1) + "," + strVal(timeSC1Min) + "," + strVal(timeSC2) + "," + strVal(timeSC2Min) + "," + strVal(timeTotal) + ",";
  }
}
