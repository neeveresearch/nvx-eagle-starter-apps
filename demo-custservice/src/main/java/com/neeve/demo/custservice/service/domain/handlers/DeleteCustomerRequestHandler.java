package com.neeve.demo.custservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.EServiceException;
import com.neeve.service.entities.ErrorType;
import com.neeve.service.entities.ErrorCode;
import com.neeve.service.messages.MessageHeader;

import com.neeve.demo.custservice.messages.DeleteCustomerRequest;
import com.neeve.demo.custservice.messages.DeleteCustomerResponse;
import com.neeve.demo.custservice.messages.CustomerDeletedEvent;
import com.neeve.demo.custservice.state.Repository;
import com.neeve.demo.custservice.state.Customer;

final public class DeleteCustomerRequestHandler implements MessageHandler<DeleteCustomerRequest, DeleteCustomerResponse, Repository> {
    @Inject
    private MessageSender<com.neeve.demo.custservice.messages.CustomerDeletedEvent> _customerDeletedEventSender;

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
                                    final DeleteCustomerRequest request,
                                    final DeleteCustomerResponse response, 
									final Repository repository) throws Exception {
        // get the Customer
        Customer customer = repository.getCustomers().get(request.getCustomerId());
        if (customer == null) {
            throw new EServiceException(ErrorType.Functional,
                                        ErrorCode.MalformedRequestDTO,
                                        "customer id '" + request.getCustomerId() + "' not found",
                                        null);
        }

        // remove from collection
        repository.getCustomers().remove(request.getCustomerId());

        // dispatch event
        final CustomerDeletedEvent event = CustomerDeletedEvent.create();
        event.setHeader(_customerDeletedEventSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        event.setCustomerId(customer.getCustomerId());
        _customerDeletedEventSender.send(request.getHeader(), event);

        // done
        return null;
    }
}