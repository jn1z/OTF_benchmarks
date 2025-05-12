package OTF_benchmark;

import net.automatalib.automaton.fsa.impl.CompactNFA;

public interface IConf<I> {

    CompactNFA<I> buildNFA();

    String getConfig();

}
