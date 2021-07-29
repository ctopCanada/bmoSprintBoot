package com.bmo.pbt.bmo.pay.the.bil.endpoint.models;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestEndpoint {
  private String url;
  private Map<String, String> httpHeaders;
  private String method;
  private String jsonPayload;
}
