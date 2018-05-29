package com.neeve.demo.custservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.IdentityInformationProvider;
import com.neeve.service.messages.MessageHeader;

import com.neeve.demo.custservice.messages.CreateCustomerRequest;
import com.neeve.demo.custservice.messages.CreateCustomerResponse;
import com.neeve.demo.custservice.messages.CustomerCreatedEvent;
import com.neeve.demo.custservice.state.Repository;
import com.neeve.demo.custservice.state.Customer;
import com.neeve.demo.custservice.service.domain.CustomerId;

final public class CreateCustomerRequestHandler implements MessageHandler<CreateCustomerRequest, CreateCustomerResponse, Repository> {
    @Inject 
    private IdentityInformationProvider _identityInformationProvider;
    @Inject
    private MessageSender<com.neeve.demo.custservice.messages.CustomerCreatedEvent> _customerCreatedEventSender;

    /**
     * Implementation of {@link MessageHandler#getType}
     */
    final public Type getType() {
        return Type.Local;
    }

    /**
     * Implementation of {@link MessageHandler#handle}
     */
    final public MessageView handle(final String origin, 
                                    final CreateCustomerRequest request,
                                    final CreateCustomerResponse response, 
									final Repository repository) throws Exception {
        // create
        final Customer customer = Customer.create();
        customer.setCustomerId(CustomerId.create(repository, _identityInformationProvider));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());

        /**
            Put code here to populate the newly created Customer from the request message
            ... customer.setFoo();
            ... customer.setBar();
            ... .
            ... .
            ... .
            ... customer.setEtc();
         **/

        // add to collection
        repository.getCustomers().put(customer.getCustomerId(), customer);

        // populate response
        response.setCustomerId(customer.getCustomerId());

        // dispatch event
        final CustomerCreatedEvent event = CustomerCreatedEvent.create();
        event.setHeader(_customerCreatedEventSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        event.setCustomerId(customer.getCustomerId());
        event.setFirstName(customer.getFirstName());
        event.setLastName(customer.getLastName());
        _customerCreatedEventSender.send(request.getHeader(), event);

        // done
        return null;
    }
}