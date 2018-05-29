package com.neeve.demo.pricingservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.messages.MessageHeader;
import com.neeve.service.messages.NullMessage;

import com.neeve.demo.custservice.messages.GetCustomerRequest;

import com.neeve.demo.pricingservice.messages.GetPriceRequest;
import com.neeve.demo.pricingservice.messages.GetPriceResponse;
import com.neeve.demo.pricingservice.state.Repository;
import com.neeve.demo.pricingservice.state.PriceRequestContext;

final public class GetPriceRequestHandler implements MessageHandler<GetPriceRequest, GetPriceResponse, Repository> {
    @Inject private MessageSender<GetCustomerRequest> _getCustomerRequestSender;

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
                                    final GetPriceRequest request, 
                                    final GetPriceResponse response, 
                                    final Repository repository) throws Exception {
        // store the request context for continuation when customer fetch response is received
        PriceRequestContext requestContext = PriceRequestContext.create();
        requestContext.setRequestId(request.getHeader().getRequestId());
        requestContext.setSourceId(request.getHeader().getSourceId());
        requestContext.setSku(request.getSku());
        requestContext.setCustomerId(request.getCustomerId());
        repository.getPriceRequestContexts().put(requestContext.getRequestId(), requestContext);

        // send customer fetch request
        GetCustomerRequest getCustomerRequest = GetCustomerRequest.create();
        getCustomerRequest.setHeader(_getCustomerRequestSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        getCustomerRequest.getHeader().setRequestId(request.getHeader().getRequestId());
        getCustomerRequest.setCustomerId(request.getCustomerId());
        _getCustomerRequestSender.send(request.getHeader(), getCustomerRequest);

        // done. will continue when get customer response arrive
        return NullMessage.create();
    }
}
