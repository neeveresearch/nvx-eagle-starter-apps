package com.neeve.demo.pricingservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;

import com.neeve.demo.pricingservice.messages.GetPriceLocalRequest;
import com.neeve.demo.pricingservice.messages.GetPriceResponse;
import com.neeve.demo.pricingservice.messages.PriceEvent;
import com.neeve.demo.pricingservice.state.Repository;
import com.neeve.service.messages.MessageHeader;

final public class GetPriceLocalRequestHandler implements MessageHandler<GetPriceLocalRequest, GetPriceResponse, Repository> {
    @Inject private MessageSender<com.neeve.demo.pricingservice.messages.PriceEvent> _priceEventSender;

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
                                    final GetPriceLocalRequest request, 
                                    final GetPriceResponse response, 
                                    final Repository repository) throws Exception {
        // compute price
        double price = 101.0;

        // send event
        PriceEvent priceEvent = PriceEvent.create();
        priceEvent.setHeader(_priceEventSender.prepareHeader(request.getHeader(), MessageHeader.create(), null));
        priceEvent.setSku(request.getSku());
        priceEvent.setCustomerId(request.getCustomerId());
        priceEvent.setPrice(price);
        _priceEventSender.send(request.getHeader(), priceEvent);

        // send response
        response.setPrice(price);
        return null;
    }
}
