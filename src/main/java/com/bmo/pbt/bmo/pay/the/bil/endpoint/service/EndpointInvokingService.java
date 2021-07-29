package com.bmo.pbt.bmo.pay.the.bil.endpoint.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bmo.pbt.bmo.pay.the.bil.endpoint.models.RestEndpoint;
import com.bmo.pbt.bmo.pay.the.bil.endpoint.utils.RestUtils;

@Component("EndpointInvokingService")
public class EndpointInvokingService {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointInvokingService.class);
    
    
    public void executeEndpointTest(List<RestEndpoint> listEndpoints) throws Exception {

	    int failureCount = 0 ; 
	    
	    for (RestEndpoint restEndpoint : listEndpoints) {
	      String responseCode = "";

	      String url = restEndpoint.getUrl();
	      String content = restEndpoint.getJsonPayload();
	      Map<String, String> httpHeaders = restEndpoint.getHttpHeaders();
	      String httpMethod = restEndpoint.getMethod();

	      switch (httpMethod) {
	        case "POST":
	          responseCode =
	              RestUtils.post(url, content, ContentType.APPLICATION_JSON, false, httpHeaders);
	          break;
	        case "PUT":
	          responseCode =
	              RestUtils.put(url, content, ContentType.APPLICATION_JSON, false, httpHeaders);
	          break;
	        case "DELETE":
	          responseCode =
	              RestUtils.post(url, content, ContentType.APPLICATION_JSON, false, httpHeaders);
	          break;
	        default:
	          LOGGER.debug("http method is not recognized as " + httpMethod);
	      }
	      
	      boolean isSuccessful = Pattern.matches("^2\\d{2}", responseCode);
	      
	      if (!isSuccessful) {
		  failureCount ++ ;
	      }
	      
	      LOGGER.info("To {} the endpoint {} with response code as {}" , httpMethod , url , responseCode);
	      
	    }
	    
	    LOGGER.info("In Summary there are totally {} failure endpoint call out of {} endpoints testing" , failureCount , listEndpoints.size());
	    
	    //System.out.println(listEndpoints);
	  }
    
}
