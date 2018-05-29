package com.neeve.demo.custservice.driver;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.neeve.cli.annotations.Command;
import com.neeve.root.RootConfig;
import com.neeve.server.app.annotations.AppFinalizer;
import com.neeve.service.messages.Credentials;
import com.neeve.trace.Tracer;

import com.neeve.demo.custservice.service.Client;

public class App {
    final private Client _client;
    final private List<Long> _customers;
    final private List<Requestor> _requestors;
    final private AtomicInteger _numSeedSuccess;
    final private AtomicInteger _numSeedFail;
    final private Tracer _tracer;
    final private Tracer _requestorStatsTracer;
    final private Tracer _threadStatsTracer;
    final private Tracer _actionStatsTracer;
    private ExecutorService _seedExecutor;
    private Runner _runner;

    public App() {
        Credentials credentials = Credentials.create();
        credentials.setUsername("perf");
        credentials.setPassword("doesntmatter");
        _client = new Client("perf", credentials);
        _customers = new ArrayList<Long>();
        _requestors = new ArrayList<Requestor>();
        _numSeedSuccess = new AtomicInteger(0);
        _numSeedFail = new AtomicInteger(0);
        _tracer = RootConfig.ObjectConfig.createTracer(RootConfig.ObjectConfig.get("demo-custservice.driver"));
        _requestorStatsTracer = RootConfig.ObjectConfig.createTracer(RootConfig.ObjectConfig.get("demo-custservice.driver.requestorstats"));
        _threadStatsTracer = RootConfig.ObjectConfig.createTracer(RootConfig.ObjectConfig.get("demo-custservice.driver.threadstats"));
        _actionStatsTracer = RootConfig.ObjectConfig.createTracer(RootConfig.ObjectConfig.get("demo-custservice.driver.actionstats"));
    }

    @Command(name = "seed")
    public void seed(final int customerCount, final int concurrency) throws Exception {
        synchronized (this){
            // validate
            if (_seedExecutor != null && !_seedExecutor.isTerminated()){
                throw new Exception("seeder is already running");
            }
            if (_runner != null && _runner.isAlive()){
                throw new Exception("run in progress");
            }

            // new customers
            _tracer.log("Seeding new customers (count=" + customerCount + ", concurrency=" + concurrency + ")...", Tracer.Level.INFO);
            _seedExecutor = Executors.newFixedThreadPool(concurrency);
            try {
                for (int i = 0; i < customerCount; i++){
                    _seedExecutor.submit(new Seeder(_client, _customers, _numSeedSuccess, _numSeedFail, _tracer));
                }
            }
            finally {
                _seedExecutor.shutdown();
            }
        }
    }

    @Command(name = "seeddone")
    public String seedDone() throws Exception {
        synchronized (this) {
            if (_seedExecutor != null && !_seedExecutor.isTerminated()) {
                return "NOK [Still running (NumSucceeded=" + _numSeedSuccess.get() + ", NumFailed=" + _numSeedFail.get() + ")]";
            }
            else {
                return "OK ";
            }
        }
    }

    @Command(name = "configurerequestors")
    final public void configureRequestors(final int numRequestors, final int requestCount, final int requestRate, final int requestThreads, final boolean useREST) throws Exception {
        synchronized (this) {
            // validate
            if (_runner != null && _runner.isAlive()) {
                throw new Exception("run in progress");
            }
            _requestors.clear();
            for (int i = 0; i < numRequestors; i++) {
                final Requestor requestor = new Requestor("requestor-" + (i + 1), _tracer);
                requestor.setUser("perf");
                requestor.setCount(requestCount);
                requestor.setRate(requestRate);
                requestor.setNumThreads(requestThreads);
                requestor.setUseREST(useREST);
                _requestors.add(requestor);
            }
        }
    }

    @Command(name = "resetactions")
    final public void resetActions() throws Exception {
        synchronized (this) {
            if (_runner != null && _runner.isAlive()) {
                throw new Exception("run in progress");
            }
            for (Requestor requestor: _requestors) {
                requestor.resetActions();
            }
        }
    }

    @Command(name = "addaction")
    final public void addAction(final String actionName, final double pct) throws Exception {
        synchronized (this) {
            if (_runner != null && _runner.isAlive()) {
                throw new Exception("run in progress");
            }
            if (actionName == null) {
                throw new IllegalArgumentException("action is required");
            }
            final Action action = Action.valueOf(actionName);
            for (Requestor requestor: _requestors) {
                requestor.addAction(action, pct);
            }
        }
    }

    @Command(name = "run")
    final public void run() throws Exception {
        synchronized (this) {
            if (_runner != null && _runner.isAlive()) {
                throw new Exception("run in progress");
            }
            (_runner = new Runner(_requestors,
                                  _customers,
                                  _tracer,
                                  _requestorStatsTracer,
                                  _threadStatsTracer,
                                  _actionStatsTracer)).start();
        }
    }

    @Command(name = "done")
    final public String done() throws Exception {
        synchronized (this) {
            return _runner != null && _runner.isAlive() ? "NOK [Still running]" : "OK ";
        }
    }

    @Command(name = "stats")
    final public String stats() throws Exception {
        synchronized (this) {
            if (_runner != null) {
                final StringBuilder sb1 = new StringBuilder();
                final StringBuilder sb2 = new StringBuilder();
                final StringBuilder sb3 = new StringBuilder();
                Map<Action, StringBuilder> responseTimes = new LinkedHashMap<Action, StringBuilder>();
                _runner.getStats(sb1, sb2, sb3, responseTimes);
                final StringBuilder sb = new StringBuilder();
                sb.append("requestorstats.txt").append("^").append(sb1);
                sb.append("|");
                sb.append("threadstats.txt").append("^").append(sb2);
                sb.append("|");
                sb.append("actionstats.txt").append("^").append(sb3);
                for (Map.Entry<Action, StringBuilder> entry: responseTimes.entrySet()) {
                    Action action = entry.getKey();
                    StringBuilder sb4 = entry.getValue();
                    sb.append("|");
                    sb.append("responsestats.").append(action.toString()).append(".csv").append("^").append(sb4);
                }
                return sb.toString();
            }
            else {
                throw new IllegalStateException("run never started");
            }
        }
    }

    @AppFinalizer
    public void finalize() throws Exception {
        synchronized (this) {
            if (_runner != null && _runner.isAlive()) {
                _runner.shutdown();
            }
        }
    }
}
