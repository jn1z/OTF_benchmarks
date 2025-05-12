package OTF_benchmark;

import OTF.Model.Cancellation;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractJob implements Runnable {

    protected Cancellation initCancellation(int maxStates) {
        return initCancellation(maxStates, Benchmark.TIMEOUT_DUR, Benchmark.TIMEOUT_UNIT);
    }

    protected Cancellation initCancellation(int maxStates, int timeoutDur, TimeUnit timeoutUnit) {
        final Cancellation cancellation = new Cancellation(false, maxStates);
        final ScheduledFuture<?> schedule =
                Benchmark.INTERRUPTOR.schedule(cancellation::setInterrupted, timeoutDur, timeoutUnit);
        cancellation.setBackref(schedule);
        return cancellation;
    }

}
