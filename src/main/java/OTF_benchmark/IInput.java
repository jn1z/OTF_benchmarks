package OTF_benchmark;

import java.util.Iterator;

public interface IInput {

    String id();

    String header();

    Iterator<IConf<Integer>> warmups();

    Iterator<IConf<Integer>> jobs();

}
