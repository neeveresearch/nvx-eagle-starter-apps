package com.neeve.demo.pricingservice.service.domain.handlers;

import com.google.inject.*;
import com.neeve.demo.custservice.messages.GetCustomerRequest;
import com.neeve.demo.custservice.messages.GetCustomerResponse;
import com.neeve.demo.pricingservice.messages.GetPriceLocalRequest;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;

import com.neeve.demo.pricingservice.messages.GetPriceRemoteRequest;
import com.neeve.demo.pricingservice.messages.GetPriceResponse;
import com.neeve.demo.pricingservice.state.Repository;
import com.neeve.service.messages.MessageHeader;

final public class GetPriceRemoteRequestHandler implements MessageHandler<GetPriceRemoteRequest, GetPriceResponse, Repository> {
    @Inject
    private com.neeve.demo.custservice.service.Client _custClient;

    /**
     * Implementation of {@link MessageHandler#getType}
     */
    final public Type getType() {
        return Type.Remote;
    }

    /**
     * Implementation of {@link MessageHandler#handle}
     */
    final public MessageView handle(final String origin,
                                    final GetPriceRemoteRequest request, 
                                    final GetPriceResponse response, 
                                    final Repository repository) throws Exception {
        // make a backend system long running call

        // get customer info
        String backendVal = null;
        if (request.getCustomerId() > 0) {
            GetCustomerRequest grequest = GetCustomerRequest.create();
            grequest.setCustomerId(request.getCustomerId());
            GetCustomerResponse gresponse = _custClient.getCustomer(grequest);

            backendVal = gresponse.getLastName();
        }

        // proceed to next stage - chain to next handler
        final GetPriceLocalRequest nextStageRequest = GetPriceLocalRequest.create();
        nextStageRequest.setHeader((MessageHeader) request.getHeader().clone());
        nextStageRequest.setSku(request.getSku());
        nextStageRequest.setCustomerId(request.getCustomerId());
        nextStageRequest.setBackendVal(backendVal);
        
        return nextStageRequest;
    }
}
