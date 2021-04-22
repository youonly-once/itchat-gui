package cn.shu.zuul;

import com.netflix.appinfo.RefreshableAmazonInfoProvider;
import com.sun.jersey.api.client.ClientResponse;
import org.checkerframework.checker.units.qual.C;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author SXS
 * @since 4/22/2021
 */
@Component
public class AutoWechatFallback implements FallbackProvider {
    @Override
    public String getRoute() {
        return "autowechat";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
       return new ClientHttpResponse() {
           @Override
           public HttpStatus getStatusCode() throws IOException {
               return HttpStatus.OK;
           }

           @Override
           public int getRawStatusCode() throws IOException {
               return ClientResponse.Status.OK.getStatusCode();
           }

           @Override
           public String getStatusText() throws IOException {
               cause.printStackTrace();
               return cause.getMessage();
           }

           @Override
           public void close() {

           }

           @Override
           public InputStream getBody() throws IOException {
               return new ByteArrayInputStream("Error".getBytes());
           }

           @Override
           public HttpHeaders getHeaders() {
               HttpHeaders httpHeaders = new HttpHeaders();
               httpHeaders.set("AcceptHeader","SXS");
               return httpHeaders;
           }
       };
    }
}
