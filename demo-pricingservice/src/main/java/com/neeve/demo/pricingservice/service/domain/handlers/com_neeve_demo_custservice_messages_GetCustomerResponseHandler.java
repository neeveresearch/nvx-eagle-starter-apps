package com.neeve.demo.pricingservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;
import com.neeve.service.messages.MessageHeader;

import com.neeve.demo.pricingservice.messages.PriceEvent;

import com.neeve.demo.custservice.messages.GetCustomerResponse;
import com.neeve.demo.pricingservice.messages.GetPriceResponse;
import com.neeve.demo.pricingservice.state.Repository;
import com.neeve.demo.pricingservice.state.PriceRequestContext;

final public class com_neeve_demo_custservice_messages_GetCustomerResponseHandler implements MessageHandler<GetCustomerResponse, GetPriceResponse, Repository> {
    @Inject private MessageSender<PriceEvent> _priceEventSender;

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
                                    final GetCustomerResponse request, 
                                    final GetPriceResponse response, 
                                    final Repository repository) throws Exception {
        // get request context
        PriceRequestContext requestContext = repository.getPriceRequestContexts().get(request.getHeader().getRequestId());

        double price = 100.0;

        // send event
        PriceEvent priceEvent = PriceEvent.create();
        priceEvent.setHeader(_priceEventSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        priceEvent.setSku(requestContext.getSku());
        priceEvent.setCustomerId(requestContext.getCustomerId());
        priceEvent.setPrice(price);
        _priceEventSender.send(request.getHeader(), priceEvent);

        // set response header fields for correct routing
        response.getHeader().setRequestId(requestContext.getRequestId());
        response.getHeader().setSourceId(requestContext.getSourceId());

        // remove the request context
        repository.getPriceRequestContexts().remove(request.getHeader().getRequestId());

        // done
        return null;
    }
}
