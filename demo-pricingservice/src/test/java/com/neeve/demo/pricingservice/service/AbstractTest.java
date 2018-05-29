package com.neeve.demo.pricingservice.service;

import org.junit.*;
import static org.junit.Assert.*;

import com.neeve.service.messages.*;

/**
 * Base class for service tests
 */
abstract public class AbstractTest {
    protected static Client _client;
    protected static com.neeve.demo.custservice.service.Client _custServiceClient;

    protected AbstractTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        com.neeve.ci.XRuntime.getProps().setProperty("demo.custservice.client.servertracking.enable", "false");
        Credentials credentials = Credentials.create();
        credentials.setUsername("user");
        credentials.setPassword("password");
        _custServiceClient = new com.neeve.demo.custservice.service.Client("pricing-test");
        _custServiceClient.open(credentials);
        _client = new Client("pricing-test");
        _client.open(credentials);
    }

    @AfterClass
    public static void tearDownClass() {
        if (_custServiceClient != null) {
            _custServiceClient.close();
        }
        if (_client != null) {
            _client.close();
        }
    }
}