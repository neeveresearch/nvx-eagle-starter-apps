package com.neeve.demo.pricingservice.driver;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.neeve.trace.Tracer;
import com.neeve.util.UtlThrowable;

final class Runner extends Thread {
    final private class StatsThread extends Thread {
        private boolean done;
        final void shutdown() {
            done = true;
            while (true) {
                try {
                    join();
                    break;
                }
                catch (InterruptedException e) {}
            }
        }
        final public void run() {
            while (!done) {
                try {
                    Thread.currentThread().sleep(1000);
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    StringBuilder sb3 = new StringBuilder();
                    getStats(sb1, sb2, sb3, null);
                    _requestorStatsTracer.log(sb1.toString(), Tracer.Level.INFO);
                    _threadStatsTracer.log(sb2.toString(), Tracer.Level.INFO);
                    _actionStatsTracer.log(sb3.toString(), Tracer.Level.INFO);
                }
                catch (InterruptedException e) {}
            }
        }
    }

    final private Tracer _tracer;
    final private Tracer _requestorStatsTracer;
    final private Tracer _threadStatsTracer;
    final private Tracer _actionStatsTracer;
    final private List<Requestor> _requestors;
    final private Random _random = new Random(System.currentTimeMillis());

    Runner(final List<Requestor> requestors,
           final List<Long> customers,
           final Tracer tracer,
           final Tracer requestorStatsTracer,
           final Tracer threadStatsTracer,
           final Tracer actionStatsTracer) throws Exception {
        setDaemon(true);
        _requestors = requestors;
        _tracer = tracer;
        _requestorStatsTracer = requestorStatsTracer;
        _threadStatsTracer = threadStatsTracer;
        _actionStatsTracer = actionStatsTracer;
    }

    final void getStats(final StringBuilder sb1, final StringBuilder sb2, final StringBuilder sb3, final Map<Action, StringBuilder> responseTimes) {
        boolean first = true;
        for (Requestor requestor: _requestors) {
            requestor.getStats().getHeader(sb1);
            requestor.getStats().get(sb1, null);
            for (Requestor.RequestorThread requestorThread: requestor.getRequestorThreads()) {
                if (first) {
                    requestorThread.getStats().getHeader(sb2);
                    requestorThread.getStats().getHeader(sb3);
                    first = false;
                }
                requestorThread.getStats().get(sb2, sb3);
                if (responseTimes != null) {
                    for (Action action: requestor.getActions()) {
                        StringBuilder sb = responseTimes.get(action);
                        if (sb == null) {
                            responseTimes.put(action, sb = new StringBuilder());
                        }
                        requestorThread.getStats().get(action, sb);
                    }
                }
            }
        }
    }

    final void shutdown() {
        for (Requestor requestor: _requestors) {
            requestor.stop();
        }
    }

    @Override
    public void run() {
        try {
            // open requestors
            for (Requestor requestor: _requestors) {
                try {
                    requestor.open();
                }
                catch (Exception e) {
                    _tracer.log("Failed to open a requestor [" + e.toString() + "]. Exiting...", Tracer.Level.SEVERE);
                    return;
                }
            }

            // start requestor threads
            for (Requestor requestor: _requestors) {
                requestor.start(this);
            }

            // start stats thread
            final StatsThread statsThread = new StatsThread();
            statsThread.start();

            // wait for threads to finish
            for (Requestor requestor: _requestors) {
                requestor.waitForFinish();
            }

            // stop stats thread
            statsThread.shutdown();
        }
        catch (Throwable e) {
            _tracer.log(UtlThrowable.prepareStackTrace(e), Tracer.Level.SEVERE);
        }
    }
}
