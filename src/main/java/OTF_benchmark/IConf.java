package OTF_benchmark;

import OTF.Model.CompactImpl;
import OTF.Model.SupportsCompactPowerset;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.NFA;

public interface IConf<I> {

    NFA<?, I> buildNFA();

    Alphabet<I> buildAlphabet();

    default AutomatonCreator<SupportsCompactPowerset<?, I>, I> builder() {
        return new CompactImpl.Creator<>();
    }

    String getConfig();

}
