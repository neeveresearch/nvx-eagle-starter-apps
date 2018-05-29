package com.neeve.demo.analyticsservice.service;

import org.junit.*;
import static org.junit.Assert.*;

import com.neeve.service.messages.*;

/**
 * Base class for service tests
 */
abstract public class AbstractTest {
    protected static Client _client;

    protected AbstractTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Credentials credentials = Credentials.create();
        credentials.setUsername("user");
        credentials.setPassword("password");
        _client = new Client("ping-test");
        _client.open(credentials);
    }

    @AfterClass
    public static void tearDownClass() {
        if (_client != null) {
            _client.close();
        }
    }
}