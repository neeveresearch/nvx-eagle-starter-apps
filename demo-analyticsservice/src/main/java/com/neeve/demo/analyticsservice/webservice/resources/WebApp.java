package com.neeve.demo.analyticsservice.webservice.resources;

import javax.ws.rs.Path;
import io.swagger.annotations.*;

import com.sun.jersey.spi.resource.Singleton;

import com.neeve.ci.XRuntime;
import com.neeve.util.UtlProps;

import com.neeve.demo.analyticsservice.webservice.resources.AbstractWebApp;

@Singleton
@Path("/demo-analyticsservice")
@Api(value="demo-analyticsservice")
final public class WebApp extends AbstractWebApp {

    public WebApp() {
        super((int)UtlProps.getValue(XRuntime.getProps(), "demo.analyticsservice.webservice.port", com.neeve.webservice.AbstractApp.DEFAULT_PORT));
    }

}
