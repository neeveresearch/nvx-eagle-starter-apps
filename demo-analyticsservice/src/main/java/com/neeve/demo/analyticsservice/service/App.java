package com.neeve.demo.analyticsservice.service;

@com.neeve.server.app.annotations.AppVersion(1)
public class App extends AbstractApp {
    // --- App version
    final public static int APP_MAJOR_VERSION = 1;
    final public static int APP_MINOR_VERSION = 0;
    // --- App version

    /**
     * Constructor 
     */
    public App() {
        super(APP_MAJOR_VERSION, APP_MINOR_VERSION);
    }
}
