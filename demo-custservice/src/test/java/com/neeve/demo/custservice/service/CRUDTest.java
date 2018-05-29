package com.neeve.demo.custservice.service;

import org.junit.*;
import static org.junit.Assert.*;

import com.neeve.service.EServiceException;
import com.neeve.service.messages.*;

import com.neeve.demo.custservice.messages.*;

/**
 * Tests service ping
 */
public class CRUDTest extends AbstractTest {
    public CRUDTest() {
    }

    @Test
    public void testCRUD() {
        // create a customer
        CreateCustomerRequest crequest = CreateCustomerRequest.create();
        /**
            Put code here to populate the request message
            ... grequest.setFoo();
            ... grequest.setBar();
            ... .
            ... .
            ... .
            ... grequest.setEtc();
         **/
        CreateCustomerResponse cresponse = _client.createCustomer(crequest);
        assertNotNull(cresponse);
        long id1 = cresponse.getCustomerId();

        // create another customer
        crequest = CreateCustomerRequest.create();
        /**
            Put code here to populate the request message
            ... grequest.setFoo();
            ... grequest.setBar();
            ... .
            ... .
            ... .
            ... grequest.setEtc();
         **/
        cresponse = _client.createCustomer(crequest);
        assertNotNull(cresponse);
        long id2 = cresponse.getCustomerId();
        assertNotSame(id1, id2);

        // get both customers
        GetCustomerRequest grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id1);
        GetCustomerResponse gresponse = _client.getCustomer(grequest);
        assertNotNull(gresponse);
        /**
            Put code here to validate the contents of the first Customer
            ... assertSomething(gresponse.getFoo());
            ... assertSomething(gresponse.getBar());
            ... .
            ... .
            ... .
        ... assertSomething(gresponse.getEtc());
         **/
        grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id2);
        gresponse = _client.getCustomer(grequest);
        assertNotNull(gresponse);
        /**
            Put code here to validate the contents of the second Customer
            ... assertSomething(gresponse.getFoo());
            ... assertSomething(gresponse.getBar());
            ... .
            ... .
            ... .
        ... assertSomething(gresponse.getEtc());
         **/

        // update first customers
        UpdateCustomerRequest urequest = UpdateCustomerRequest.create();
        urequest.setCustomerId(id1);
        /**
            Put code here to populate the first update request message
            ... urequest.setFoo();
            ... urequest.setBar();
            ... .
            ... .
            ... .
            ... urequest.setEtc();
         **/
        UpdateCustomerResponse uresponse = _client.updateCustomer(urequest);
        assertNotNull(uresponse);
        grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id1);
        gresponse = _client.getCustomer(grequest);
        assertNotNull(gresponse);
        /**
            Put code here to validate the contents of the first Customer
            ... assertSomething(gresponse.getFoo());
            ... assertSomething(gresponse.getBar());
            ... .
            ... .
            ... .
        ... assertSomething(gresponse.getEtc());
         **/

        // update second customer
        urequest = UpdateCustomerRequest.create();
        urequest.setCustomerId(id2);
        /**
            Put code here to populate the second update request message
            ... urequest.setFoo();
            ... urequest.setBar();
            ... .
            ... .
            ... .
            ... urequest.setEtc();
         **/
        uresponse = _client.updateCustomer(urequest);
        assertNotNull(uresponse);
        grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id2);
        gresponse = _client.getCustomer(grequest);
        assertNotNull(gresponse);
        /**
            Put code here to validate the contents of the second Customer
            ... assertSomething(gresponse.getFoo());
            ... assertSomething(gresponse.getBar());
            ... .
            ... .
            ... .
        ... assertSomething(gresponse.getEtc());
         **/

        // delete first customer
        DeleteCustomerRequest drequest = DeleteCustomerRequest.create();
        drequest.setCustomerId(id1);
        DeleteCustomerResponse dresponse = _client.deleteCustomer(drequest);
        assertNotNull(dresponse);
        grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id1);
        try {
            _client.getCustomer(grequest); 
            fail("expected get to fail but succeeded");
        }
        catch (EServiceException e) {
        }

        // delete second customer
        drequest = DeleteCustomerRequest.create();
        drequest.setCustomerId(id2);
        dresponse = _client.deleteCustomer(drequest);
        assertNotNull(dresponse);
        grequest = GetCustomerRequest.create();
        grequest.setCustomerId(id2);
        try {
            _client.getCustomer(grequest); 
            fail("expected get to fail but succeeded");
        }
        catch (EServiceException e) {
        }
    }
}