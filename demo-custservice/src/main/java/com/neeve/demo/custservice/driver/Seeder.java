package com.neeve.demo.custservice.driver;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.neeve.trace.*;

import com.neeve.demo.custservice.messages.CreateCustomerRequest;
import com.neeve.demo.custservice.messages.CreateCustomerResponse;
import com.neeve.demo.custservice.service.Client;

final class Seeder implements Runnable {
    final private Client _client;
    final private List<Long> _customers;
    final private AtomicInteger _numSeedSuccess;
    final private AtomicInteger _numSeedFail;
    final private Tracer _tracer;

    Seeder(final Client client,
           final List<Long> customers,
           final AtomicInteger numSeedSuccess,
           final AtomicInteger numSeedFail,
           final Tracer tracer) {
        _client = client;
        _customers = customers;
        _numSeedSuccess = numSeedSuccess;
        _numSeedFail = numSeedFail;
        _tracer = tracer;
    }

    final public void run() {
        StringBuilder sb = new StringBuilder();
        try {
            CreateCustomerRequest request = CreateCustomerRequest.create();
            long customerId = _client.createCustomer(request).getCustomerId();
            synchronized (_customers) {
                _customers.add(customerId);
            }
            _numSeedSuccess.incrementAndGet();
        }
        catch (Throwable e) {
            _numSeedFail.incrementAndGet();
        }
    }
}

