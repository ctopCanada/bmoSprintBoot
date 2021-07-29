package com.bmo.pbt.bmo.pay.the.bil.endpoint.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.bmo.pbt.bmo.pay.the.bil.endpoint.ServiceConstants;


@Component
public class SchedulerJobRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://simpleTimer?delay=2s&fixedRate=true&period=6000")
            .routeId(ServiceConstants.RID_SCHEDULER_TEST_JOB_ID)
            .to("bean:ExcelDataLoadService?method=readRestEndpointsFromExcelFile")
            .to("bean:EndpointInvokingService?method=executeEndpointTest")
            .end();

        
	/*
        fromF("timer://simpleTimer?delay=2s&fixedRate=true&period=6000")
        .setBody(simple("Hello from timer at ${header.firedTime}"))
        .log("Force Post Route Exit. Operation: ${header.firedTime} Body: ${body}")
        ;
        //.to("stream:out");
        */
        
    }
}
