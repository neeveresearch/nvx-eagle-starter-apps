package com.neeve.demo.analyticsservice.service;

import com.neeve.service.messages.*;

final public class Client extends AbstractClient {
    // ---- Constructors
    public Client() {
        this(null);
    }
    public Client(final String name) {
        this(name, null, null);
    }
    public Client(final String name, final Credentials credentials) {
        this(name, null, credentials);
    }
    public Client(final String name, final Object handlers) {
        this(name, handlers, null);
    }
    public Client(final String name, final Object handlers, final Credentials credentials) {
        super(name, App.APP_MAJOR_VERSION, App.APP_MINOR_VERSION, handlers, credentials);
    }
    // ---- Constructors

    // ---- Overriden implementation of {@link AbstractClient#createApp}
    @Override
    final protected com.neeve.demo.analyticsservice.service.AbstractApp createApp() {
        return new App();
    }
}
