package com.neeve.demo.custservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.EServiceException;
import com.neeve.service.entities.ErrorType;
import com.neeve.service.entities.ErrorCode;
import com.neeve.service.messages.MessageHeader;

import com.neeve.demo.custservice.messages.UpdateCustomerRequest;
import com.neeve.demo.custservice.messages.UpdateCustomerResponse;
import com.neeve.demo.custservice.messages.CustomerUpdatedEvent;
import com.neeve.demo.custservice.state.Repository;
import com.neeve.demo.custservice.state.Customer;

final public class UpdateCustomerRequestHandler implements MessageHandler<UpdateCustomerRequest, UpdateCustomerResponse, Repository> {
    @Inject
    private MessageSender<com.neeve.demo.custservice.messages.CustomerUpdatedEvent> _customerUpdatedEventSender;

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
                                    final UpdateCustomerRequest request,
                                    final UpdateCustomerResponse response, 
									final Repository repository) throws Exception {
        // get the Customer
        Customer customer = repository.getCustomers().get(request.getCustomerId());
        if (customer == null) {
            throw new EServiceException(ErrorType.Functional,
                                        ErrorCode.MalformedRequestDTO,
                                        "customer id '" + request.getCustomerId() + "' not found",
                                        null);
        }
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());

        /**
            Put code here to update the Customer from the request message
            ... customer.setFoo();
            ... customer.setBar();
            ... .
            ... .
            ... .
            ... customer.setEtc();
         **/

        // dispatch event
        final CustomerUpdatedEvent event = CustomerUpdatedEvent.create();
        event.setHeader(_customerUpdatedEventSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        event.setCustomerId(customer.getCustomerId());
        event.setFirstName(customer.getFirstName());
        event.setLastName(customer.getLastName());
        _customerUpdatedEventSender.send(request.getHeader(), event);

        // done
        return null;
    }
}