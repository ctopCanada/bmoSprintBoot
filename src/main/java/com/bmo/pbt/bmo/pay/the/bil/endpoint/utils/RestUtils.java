package com.bmo.pbt.bmo.pay.the.bil.endpoint.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bmo.pbt.bmo.pay.the.bil.endpoint.exceptions.RestException;

/**
 * Utility methods to send plain HTTP requests and invoked Rest API using either
 *
 * <p>XML or JSON.
 *
 * <p>The APIs must use return a response that follows EGrantResponse base object
 *
 * @author legault1
 */
public class RestUtils {

  public static final String MIME_TYPE_JSON = "application/json";

  public static HttpHost PROXY = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class.getName());

  private RestUtils() {

    throw new IllegalStateException("Utility class");
  }

  public static class BinaryResponse {

    public byte[] data;

    public ContentType contentType;
  }

  public static class FileResponse {

    public ContentType contentType;

    public String filename;
  }

  public static FileResponse getFile(
      String url, File file, boolean ignoreSSL, Map<String, String> httpHeaders) throws Exception {

    HttpGet httpGetRequest = new HttpGet(url);

    return executeFile(file, httpGetRequest, ignoreSSL, httpHeaders);
  }

  public static String put(
      String url,
      String content,
      ContentType contentType,
      boolean ignoreSSL,
      Map<String, String> httpHeaders)
      throws Exception {

    httpHeaders.put("content-type", contentType.toString());

    HttpPut httpPutRequest = new HttpPut(url);

    StringEntity stringEntity = new StringEntity(content, contentType);

    httpPutRequest.setEntity(stringEntity);

    return execute(httpPutRequest, ignoreSSL, httpHeaders, false);
  }

  public static String post(
      String url,
      String content,
      ContentType contentType,
      boolean ignoreSSL,
      Map<String, String> httpHeaders)
      throws Exception {

    return post(url, content, contentType, ignoreSSL, httpHeaders, null, null, null, null);
  }

  public static String post(
      String url,
      String content,
      ContentType contentType,
      boolean ignoreSSL,
      Map<String, String> httpHeaders,
      String[] filenames,
      String[] contentTypes,
      String[] partNames,
      byte[][] fileDatas)
      throws Exception {

    HttpPost httpPostRequest = new HttpPost(url);

    HttpEntity httpEntity;

    if (filenames == null) {

      httpEntity = new StringEntity(content, contentType);

      httpHeaders.put("content-type", contentType.toString());

    } else {

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();

      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

      ByteArrayInputStream baisr = new ByteArrayInputStream(content.getBytes("UTF-8"));

      InputStreamBody inputStreamBod2y = new InputStreamBody(baisr, contentType, "test.json");

      builder.addPart("jsonRequest", inputStreamBod2y);

      for (int f = 0; f < filenames.length; f++) {

        ByteArrayInputStream bais = new ByteArrayInputStream(fileDatas[f]);

        InputStreamBody inputStreamBody =
            new InputStreamBody(bais, ContentType.create(contentTypes[f]), filenames[f]);

        builder.addPart(partNames[f], inputStreamBody);
      }

      httpEntity = builder.build();
    }

    httpPostRequest.setEntity(httpEntity);

    return execute(httpPostRequest, ignoreSSL, httpHeaders, true);
  }

  private static String execute(
      HttpRequestBase httpRequest,
      boolean ignoreSSL,
      Map<String, String> httpHeaders,
      boolean expectedResult)
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
          ClientProtocolException, IOException, RestException {

    HttpClientBuilder httpClientBuilder = HttpClients.custom();

    CloseableHttpClient httpClient = null;

    CloseableHttpResponse httpResponse = null;

    try {

      if (ignoreSSL) {

        httpClientBuilder
            .setSSLContext(
                new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
      }

      RequestConfig.Builder requestBuilder = RequestConfig.custom();

      requestBuilder.setConnectTimeout(10 * 60 * 1000);

      requestBuilder.setConnectionRequestTimeout(10 * 60 * 1000);

      requestBuilder.setContentCompressionEnabled(true);

      requestBuilder.setSocketTimeout(10 * 60 * 1000);

      // Require to use system settings such as a proxy if one is set

      httpClientBuilder.useSystemProperties();

      httpClientBuilder.setDefaultRequestConfig(requestBuilder.build());

      if (PROXY != null) {

        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(PROXY);

        httpClientBuilder.setRoutePlanner(routePlanner);
      }

      httpClient = httpClientBuilder.build();

      setHeaders(httpRequest, httpHeaders);

      httpResponse = httpClient.execute(httpRequest);

      int statusCode = httpResponse.getStatusLine().getStatusCode();

      HttpEntity responseEntity = httpResponse.getEntity();

      StringBuilder sResponseHttpHeaders = new StringBuilder();

      List<Header> responseHttpHeaders = Arrays.asList(httpResponse.getAllHeaders());

      for (Header responseHttpHeader : responseHttpHeaders) {

        sResponseHttpHeaders.append(responseHttpHeader.getName());

        sResponseHttpHeaders.append(": ");

        sResponseHttpHeaders.append(responseHttpHeader.getValue());

        sResponseHttpHeaders.append("\n");
      }

      if (responseEntity == null && expectedResult) {

        throw new RestException(
            "HTTP "
                + httpRequest.getMethod()
                + " to "
                + httpRequest.getURI()
                + " failed. CODE= "
                + statusCode
                + " HEADERS=\n"
                + sResponseHttpHeaders.toString()
                + " CONTENT:\n no content returned",
            statusCode,
            null);
      }

      String response = null;

      if (responseEntity != null && expectedResult) {

        StringWriter writer = new StringWriter();

        IOUtils.copy(responseEntity.getContent(), writer, Charset.forName("UTF-8"));

        response = writer.toString();

        if (statusCode >= 400) {

          throw new RestException(
              "HTTP "
                  + httpRequest.getMethod()
                  + " to "
                  + httpRequest.getURI()
                  + " failed. CODE= "
                  + statusCode
                  + " HEADERS=\n"
                  + sResponseHttpHeaders.toString()
                  + " CONTENT:\n"
                  + response,
              statusCode,
              response);
        }
      }

      return statusCode + "";

    } finally {

      if (httpResponse != null) {

        httpResponse.close();
      }

      if (httpClient != null) {

        httpClient.close();
      }
    }
  }

  private static FileResponse executeFile(
      File file, HttpRequestBase httpRequest, boolean ignoreSSL, Map<String, String> httpHeaders)
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
          ClientProtocolException, IOException, RestException {

    HttpClientBuilder httpClientBuilder = HttpClients.custom();

    CloseableHttpClient httpClient = null;

    CloseableHttpResponse httpResponse = null;

    try {

      if (ignoreSSL) {

        httpClientBuilder
            .setSSLContext(
                new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
      }

      RequestConfig.Builder requestBuilder = RequestConfig.custom();

      requestBuilder.setConnectTimeout(10 * 60 * 1000);

      requestBuilder.setConnectionRequestTimeout(10 * 60 * 1000);

      requestBuilder.setContentCompressionEnabled(true);

      requestBuilder.setSocketTimeout(10 * 60 * 1000);

      // Require to use system settings such as a proxy if one is set

      httpClientBuilder.useSystemProperties();

      httpClientBuilder.setDefaultRequestConfig(requestBuilder.build());

      httpClient = httpClientBuilder.build();

      setHeaders(httpRequest, httpHeaders);

      httpResponse = httpClient.execute(httpRequest);

      int statusCode = httpResponse.getStatusLine().getStatusCode();

      LOGGER.debug(
          "httpRequest endpoint URL : {} with status code as {}",
          httpRequest.getURI().toString(),
          statusCode);

      HttpEntity entity = httpResponse.getEntity();

      StringBuilder sResponseHttpHeaders = new StringBuilder();

      List<Header> responseHttpHeaders = Arrays.asList(httpResponse.getAllHeaders());

      for (Header responseHttpHeader : responseHttpHeaders) {

        sResponseHttpHeaders.append(responseHttpHeader.getName());

        sResponseHttpHeaders.append(": ");

        sResponseHttpHeaders.append(responseHttpHeader.getValue());

        sResponseHttpHeaders.append("\n");
      }

      if (entity == null) {

        throw new RestException(
            "HTTP "
                + httpRequest.getMethod()
                + " to "
                + httpRequest.getURI()
                + " failed. CODE= "
                + statusCode
                + " HEADERS=\n"
                + sResponseHttpHeaders.toString()
                + " CONTENT:\n no content returned",
            statusCode,
            null);
      }

      FileResponse fileResponse = new FileResponse();

      FileUtils.copyInputStreamToFile(entity.getContent(), file);

      entity.getContent().close();

      Header header = entity.getContentType();

      if (header != null) fileResponse.contentType = ContentType.parse(header.getValue());

      Header contentDispoisitionHeaders = httpResponse.getFirstHeader("Content-Disposition");

      if (contentDispoisitionHeaders != null) {

        HeaderElement[] headerElements = contentDispoisitionHeaders.getElements();

        for (HeaderElement headerElement : headerElements) {

          NameValuePair filaneme = headerElement.getParameterByName("filename");

          if (filaneme != null) {

            fileResponse.filename = filaneme.getValue();
          }
        }
      }

      return fileResponse;

    } finally {

      if (httpResponse != null) {

        httpResponse.close();
      }

      if (httpClient != null) {

        httpClient.close();
      }
    }
  }

  private static void setHeaders(HttpRequestBase request, Map<String, String> httpHeaders) {

    for (Map.Entry<String, String> httpHeaderEntry : httpHeaders.entrySet()) {

      String key = httpHeaderEntry.getKey();

      String value = httpHeaderEntry.getValue();

      request.setHeader(key, value);
    }
  }
}
