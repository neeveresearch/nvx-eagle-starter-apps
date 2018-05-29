package com.neeve.demo.custservice.webservice.resources;

import javax.ws.rs.Path;
import io.swagger.annotations.*;

import com.sun.jersey.spi.resource.Singleton;

import com.neeve.ci.XRuntime;
import com.neeve.util.UtlProps;

import com.neeve.demo.custservice.webservice.resources.AbstractWebApp;

@Singleton
@Path("/demo-custservice")
@Api(value="demo-custservice")
final public class WebApp extends AbstractWebApp {

    public WebApp() {
        super((int)UtlProps.getValue(XRuntime.getProps(), "demo.custservice.webservice.port", com.neeve.webservice.AbstractApp.DEFAULT_PORT));
    }

}
