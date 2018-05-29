package com.neeve.demo.analyticsservice.service.domain.handlers;

import com.google.inject.*;

import com.neeve.sma.MessageView;
import com.neeve.service.MessageHandler;
import com.neeve.service.MessageSender;

import com.neeve.demo.pricingservice.messages.PriceEvent;
import com.neeve.service.messages.NullMessage;
import com.neeve.demo.analyticsservice.state.Repository;

final public class com_neeve_demo_pricingservice_messages_PriceEventHandler implements MessageHandler<PriceEvent, NullMessage, Repository> {
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
                                    final PriceEvent request, 
                                    final NullMessage response, 
                                    final Repository repository) throws Exception {
        return null;
    }
}
