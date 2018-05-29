package com.neeve.demo.custservice.driver;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import java.util.Arrays;

import com.neeve.ci.XRuntime;
import com.neeve.service.messages.PingRequest;
import com.neeve.service.messages.Credentials;
import com.neeve.trace.Tracer;
import com.neeve.util.UtlThrowable;

import com.neeve.demo.custservice.service.Client;
import com.neeve.demo.custservice.messages.*;

final class Requestor {
    final class RequestorThread extends Thread {
        final Action[] _actionDistribution;
        final int _rate;
        final long _rateDuration;
        final int _count;
        final Stats _stats;
        final Random _random;
        private boolean _done;

        RequestorThread(final String name, final Action[] actionDistribution, final int rate, final long rateDuration, final int count) {
            setName(name);
            setDaemon(true);
            _actionDistribution = actionDistribution;
            _rate = rate < 0 ? Integer.MAX_VALUE : rate;
            _rateDuration = rateDuration;
            _count = count < 0 ? Integer.MAX_VALUE : count;
            _stats = new Stats(name, actionDistribution);
            _random = new Random(System.currentTimeMillis() | getId() | hashCode());
        }

        final Stats getStats() {
            return _stats;
        }

        @Override
        final public void run() {
            try {
                Thread.currentThread().sleep(_random.nextInt(60000));
            }
            catch (InterruptedException e) {}
            int deltaCount = 0;
            long ts = System.currentTimeMillis();
            for (int i = 0; !_done && i < _count; i++) {
                Action action = _actionDistribution[_random.nextInt(_actionDistribution.length)];
                try {
                    _stats.onSuccess(action, Requestor.this.execute(action));
                }
                catch (Exception e) {
                    _tracer.log(UtlThrowable.prepareStackTrace(e), Tracer.Level.WARNING);
                    _stats.onFailure(action);
                }
                if (++deltaCount == _rate) {
                    long elapsed = System.currentTimeMillis() - ts;
                    if (elapsed < _rateDuration) {
                        try {
                            Thread.currentThread().sleep(_rateDuration - elapsed);
                        }
                        catch (InterruptedException e) {}
                    }
                    else {
                        // this avoids all the requests bunching together
                        // in cases where the response time is much higher than the interval
                        try {
                            Thread.currentThread().sleep(_random.nextInt((int)elapsed));
                        }
                        catch (InterruptedException e) {}
                    }
                    deltaCount = 0;
                    ts = System.currentTimeMillis();
                }
            }
        }

        final public void shutdown() {
            _done = true;
        }
    }

    final private String _name;
    final private Tracer _tracer;
    final private List<RequestorThread> _requestorThreads;
    final private Action[] _actionDistribution;
    private Stats _stats;
    private Client _client;
    private String _user;
    private int _rate;
    private int _count;
    private int _numThreads;
    private boolean _useREST;
    private Runner _runner;
    private int _actionIdx;

    Requestor(final String name, final Tracer tracer) {
        _name = name;
        _requestorThreads = new ArrayList<RequestorThread>();
        _tracer = tracer;
        _actionDistribution = new Action[10000]; // 10000 points to allow a lowest of 0.01% for an action
        _numThreads = 1;
    }

    final private void pingUsingREST() throws Exception {
    }

    final private void pingUsingJava() throws Exception {
        _client.ping(PingRequest.create());
    }

    final private void createCustomerUsingREST() throws Exception {
    }

    final private void createCustomerUsingJava() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.create();
        /**
         * Put code here to populate request
         */
        _runner.addCustomer(_client.createCustomer(request).getCustomerId());
    }

    final private void getCustomerUsingREST() throws Exception {
    }

    final private void getCustomerUsingJava() throws Exception {
        GetCustomerRequest request = GetCustomerRequest.create();
        request.setCustomerId(_runner.getCustomer());
        _client.getCustomer(request);
    }

    final private void updateCustomerUsingREST() throws Exception {
    }

    final private void updateCustomerUsingJava() throws Exception {
        UpdateCustomerRequest request = UpdateCustomerRequest.create();
        request.setCustomerId(_runner.getCustomer());
        /**
         * Put code here to populate request
         */
        _client.updateCustomer(request);
    }

    final private void deleteCustomerUsingREST() throws Exception {
    }

    final private void deleteCustomerUsingJava() throws Exception {
        DeleteCustomerRequest request = DeleteCustomerRequest.create();
        request.setCustomerId(_runner.removeCustomer());
        _client.deleteCustomer(request);
    }

    final String getName() {
        return _name;
    }

    final Requestor setUser(final String val) {
        _user = val;
        return this;
    }

    final String getUser() {
        return _user;
    }

    final Requestor setCount(final int val) {
        _count = val;
        return this;
    }

    final int getCount() {
        return _count;
    }

    final Requestor setRate(final int val) {
        _rate = val;
        return this;
    }

    final int getRate() {
        return _rate;
    }

    final Requestor setNumThreads(final int val) {
        _numThreads = val;
        return this;
    }

    final int getNumThreads() {
        return _numThreads;
    }

    final Requestor setUseREST(final boolean val) {
        _useREST = val;
        return this;
    }

    final boolean isUseREST() {
        return _useREST;
    }

    final Requestor addAction(final Action action, final double pct) {
        if (pct > 0.0) {
            if (pct < (100.0 / _actionDistribution.length)) {
                throw new RuntimeException("Action percent cannot be < " + (100.0 / _actionDistribution.length));
            }
            int points = (int)((Math.round(pct * _actionDistribution.length)) / 100);
            _tracer.log(action + "=" + points + " points", Tracer.Level.INFO);
            for (int i = 0; i < points; i++) {
                _actionDistribution[_actionIdx++] = action;
            }
        }
        return this;
    }

    final Requestor resetActions() {
        _actionIdx = 0;
        Arrays.fill(_actionDistribution, null);
        return this;
    }

    final Set<Action> getActions() {
        Set<Action> actions = new HashSet<Action>();
        for (int i = 0; i < _actionIdx; i++) {
            actions.add(_actionDistribution[i]);
        }
        return actions;
    }

    final List<RequestorThread> getRequestorThreads() {
        return _requestorThreads;
    }

    final Stats getStats() {
        return _stats;
    }

    final void open() throws Exception {
        // validate action distribution is 100%
        int holes = 0;
        for (Action action: _actionDistribution) {
            if (action == null) {
                holes++;
            }
        }
        if (holes > 0) {
            throw new RuntimeException("action percentages add up to less than 100% (" + holes + " holes in distribution)");
        }

        // create stats
        _stats = new Stats(_name, _actionDistribution);

        // create threads
        _requestorThreads.clear();
        int rateDurationPerThread = 1;
        int ratePerThread = _rate >= 0 ? _rate / _numThreads : -1;
        if (ratePerThread == 0) {
            ratePerThread = 1;
            rateDurationPerThread = _numThreads / _rate;
        }
        int countPerThread = _count >= 0 ? _count / _numThreads : -1;
        if (countPerThread == 0) {
            throw new RuntimeException("count per thread must be > 0");
        }
        for (int i = 0; i < _numThreads; i++) {
            _requestorThreads.add(new RequestorThread(_name + "-t" + String.format("%04d", i), _actionDistribution, ratePerThread, rateDurationPerThread * 1000l, countPerThread));
        }

        // create client
        if (_client != null) {
            _client.close();
        }
        if (!_useREST) {
            Credentials credentials = Credentials.create(); 
            credentials.setUsername(_user != null ? _user : "perf");
            credentials.setPassword("doesntmatter");
            _client = new Client(_name, credentials);
        }
    }

    final void start(final Runner runner) {
        _runner = runner;
        for (RequestorThread requestorThread: _requestorThreads) {
            requestorThread.start();
        }
    }

    final long execute(final Action action) throws Exception {
        try {
            long ts1 = System.currentTimeMillis(); 
            switch (action) {
                case Noop:
                    break;

                case Ping:
                    if (_useREST) {
                        pingUsingREST();
                    }
                    else {
                        pingUsingJava();
                    }
                    break; 

                case CreateCustomer:
                    if (_useREST) {
                        createCustomerUsingREST();
                    }
                    else {
                        createCustomerUsingJava();
                    }
                    break;

                case GetCustomer:
                    if (_useREST) {
                        getCustomerUsingREST();
                    }
                    else {
                        getCustomerUsingJava();
                    }
                    break;

                case UpdateCustomer:
                    if (_useREST) {
                        updateCustomerUsingREST();
                    }
                    else {
                        updateCustomerUsingJava();
                    }
                    break;

                case DeleteCustomer:
                    if (_useREST) {
                        deleteCustomerUsingREST();
                    }
                    else {
                        deleteCustomerUsingJava();
                    }
                    break;

                default:
                    break;
            }
            long delta = System.currentTimeMillis() - ts1;
            _stats.onSuccess(action, delta);
            return delta;
        }
        catch (Exception e) {
            _stats.onFailure(action);
            throw e;
        }
    }

    final void stop() {
        for (RequestorThread requestorThread: _requestorThreads) {
            requestorThread.shutdown();
        }
    }

    final void waitForFinish() {
        for (RequestorThread requestorThread: _requestorThreads) {
            while (true) {
                try {
                    requestorThread.join();
                    break;
                }
                catch (InterruptedException e) {}
            }
        }
    }
}
