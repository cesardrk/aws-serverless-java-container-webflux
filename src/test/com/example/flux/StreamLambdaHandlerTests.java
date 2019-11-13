package com.example.flux;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@SpringBootTest
public class StreamLambdaHandlerTests {

    @Test
    public void testValidRequestToLambda() throws IOException {
        StreamLambdaHandler h = new StreamLambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/?lat_pos=-23.570000&lon_pos=-46.692000&radius=3000", "GET").build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        h.handleRequest(
                new ByteArrayInputStream(LambdaContainerHandler.getObjectMapper().writeValueAsBytes(req)),
                os,
                new MockLambdaContext()
        );
        AwsProxyResponse resp = LambdaContainerHandler.getObjectMapper().readValue(os.toByteArray(), AwsProxyResponse.class);

        System.out.println("Body: " + resp.getBody());
        assertEquals(200, resp.getStatusCode());
        assertThat(resp.getBody(), not(isEmptyString()));
    }

}
