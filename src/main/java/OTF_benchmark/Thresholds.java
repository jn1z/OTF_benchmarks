package OTF_benchmark;

public final class Thresholds {

    public static int paigeTarjan(int alphabetSize) {
        /*
         * numStatesWithSink = numStates + 1;
         * posDataLow = numStatesWithSink;
         * predOfsDataLow = posDataLow + numStatesWithSink;
         * numTransitionsFull = numStatesWithSink * numInputs;
         * predDataLow = predOfsDataLow + numTransitionsFull + 1;
         * dataSize = predDataLow + numTransitionsFull;
         *
         * -> dataSize = ((numStatesWithSink + numStatesWithSink) + (numStatesWithSink * numInputs) + 1) + (numStatesWithSink * numInputs) = numStatesWithSink * ((numInputs * 2) + 2) + 1
         */

        return (Integer.MAX_VALUE / ((alphabetSize * 2) + 2) + 1) - 1;
    }
}
