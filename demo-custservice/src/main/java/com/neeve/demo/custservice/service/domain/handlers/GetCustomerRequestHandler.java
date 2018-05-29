package com.neeve.demo.custservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.EServiceException;
import com.neeve.service.entities.ErrorType;
import com.neeve.service.entities.ErrorCode;

import com.neeve.demo.custservice.messages.GetCustomerRequest;
import com.neeve.demo.custservice.messages.GetCustomerResponse;
import com.neeve.demo.custservice.state.Repository;
import com.neeve.demo.custservice.state.Customer;

final public class GetCustomerRequestHandler implements MessageHandler<GetCustomerRequest, GetCustomerResponse, Repository> {
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
                                    final GetCustomerRequest request,
                                    final GetCustomerResponse response, 
                                    final Repository repository) throws Exception {
        // get the Customer
        Customer customer = repository.getCustomers().get(request.getCustomerId());
        if (customer == null) {
            throw new EServiceException(ErrorType.Functional,
                                        ErrorCode.MalformedRequestDTO,
                                        "customer id '" + request.getCustomerId() + "' not found",
                                        null);
        }
        response.setCustomerId(customer.getCustomerId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());

        /**
            Put code here to populate the response message from the Customer object
            ... response.setFoo();
            ... response.setBar();
            ... .
            ... .
            ... .
            ... response.setEtc();
         **/

        // done
        return null; 
    }
}