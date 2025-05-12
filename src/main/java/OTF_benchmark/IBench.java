package OTF_benchmark;

import java.util.Iterator;

public interface IBench {

    String id();

    Iterator<Runnable> warmups();

    Iterator<Runnable> jobs();

}
