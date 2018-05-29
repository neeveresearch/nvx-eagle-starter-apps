package com.neeve.demo.pricingservice.driver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.text.NumberFormat;

import cern.colt.list.DoubleArrayList;

import com.neeve.stats.IStats.Series.Collector;
import com.neeve.stats.Stats.LatencyManager;

final class Stats {
    String name;
    Action action;
    NumberFormat format;

    long startTime = System.currentTimeMillis();
    long deltaStartTime;

    volatile long numSuccessRequests;
    volatile long numFailedRequests;

    long numSuccessRequestsLast;
    long numFailedRequestsLast;

    LatencyManager responseTimes = new LatencyManager("rr");

    Map<Action, Stats> actionStats;

    {
        (format = NumberFormat.getInstance()).setMaximumFractionDigits(2);
    }

    Stats(String name, Action action) {
        this.name = name;
        this.action = action;
    }
    Stats(String name, Action[] actionDistribution) {
        this.name = name;
        actionStats = new ConcurrentHashMap<Action, Stats>();
        for (Action action: actionDistribution) {
            if (actionStats.get(action) == null) {
                actionStats.put(action, new Stats(name, action));
            }
        }
    }

    void onSuccess(Action action, long responseTime) {
        responseTimes.add(responseTime);
        numSuccessRequests++;
        if (actionStats != null) {
            actionStats.get(action).onSuccess(action, responseTime);
        }
    }

    void onFailure(Action action) {
        numFailedRequests++;
        if (actionStats != null) {
            actionStats.get(action).onFailure(action);
        }
    }

    void stamp() {
        deltaStartTime = System.currentTimeMillis();
        numSuccessRequestsLast = numSuccessRequests;
        numFailedRequestsLast = numFailedRequests;
    }

    void get(final Action action, final StringBuilder sb) {
        if (action != null) {
            sb.append(name);
            actionStats.get(action).get((Action)null, sb);
        }
        else {
            final DoubleArrayList list = new DoubleArrayList();
            responseTimes.get(new Collector() {
                @Override
                final public void add(final long sno, final double val) {
                }
            }, list, 0);
            for (int i = 0; i < list.size(); i++) {
                sb.append(",").append(list.get(i));
            }
            sb.append("\n");
        }
    }

    void getHeader(final StringBuilder sb) {
        sb.append("\n").append(String.format("%-40s %-7s %-7s %-7s %-7s %s", "THREAD", "SNUM", "SRATE", "SDRATE", "FNUM", "RESP(ms)")).append("\n");
    }

    void get(final StringBuilder tsb, final StringBuilder asb) {
        long currentTime = System.currentTimeMillis();
        final long deltaTime = currentTime - deltaStartTime;
        final long deltaTotalTime = currentTime - startTime;

        final long numSuccessRequestsCurrent = numSuccessRequests;
        final long numFailedRequestsCurrent = numFailedRequests;

        final String numSuccessRequestsStr = format.format(numSuccessRequestsCurrent);
        final String deltaNumSuccessRequestsStr = format.format(numSuccessRequestsCurrent - numSuccessRequestsLast);
        final String successRequestRateStr = format.format(((double)numSuccessRequestsCurrent * 1000) / deltaTotalTime);
        final String deltaSuccessRequestRateStr = format.format((((double)numSuccessRequestsCurrent - numSuccessRequestsLast) * 1000) / deltaTime);
        final String numFailedRequestsStr = format.format(numFailedRequestsCurrent);

        StringBuilder sb = new StringBuilder();
        responseTimes.compute();
        responseTimes.get(sb);

        final String str = String.format("%-40s %-7s %-7s %-7s %-7s %s",
                                         action == null ? name : ("..." + action.toString()),
                                         numSuccessRequestsStr,
                                         successRequestRateStr,
                                         deltaSuccessRequestRateStr,
                                         numFailedRequestsStr,
                                         sb.toString());
        tsb.append(str);
        if (asb != null) {
            asb.append(str);
            if (actionStats != null) {
                for (Stats stats: actionStats.values()) {
                    stats.get(asb, null);
                }
            }
        }
        stamp();
    }
}

