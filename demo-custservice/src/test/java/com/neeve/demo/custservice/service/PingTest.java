package com.neeve.demo.custservice.service;

import org.junit.*;
import static org.junit.Assert.*;

import com.neeve.service.messages.*;

/**
 * Tests service ping
 */
public class PingTest extends AbstractTest {
    public PingTest() {
    }

    @Test
    public void testPing() {
        PingRequest request = PingRequest.create();
        PingResponse response = _client.ping(request);
        assertNotNull(response);
    }
}