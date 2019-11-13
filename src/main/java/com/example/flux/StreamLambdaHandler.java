package com.example.flux;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            Optional<String> listOfActiveSpringProfiles = Optional.ofNullable(System.getenv("SPRING_PROFILE"));
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class,
                    getProfilesAsArray(listOfActiveSpringProfiles));

            //For applications that take longer than 10 seconds to start, use the async builder:
            /*
            long startTime = Instant.now().toEpochMilli();
            handler = new SpringBootProxyHandlerBuilder()
                               .defaultProxy()
                               .asyncInit(startTime)
                               .springBootApplication(Application.class)
                               .buildAndInitialize();
             */
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    public StreamLambdaHandler() {
        // we enable the timer for debugging. This SHOULD NOT be enabled in production.
        Timer.enable();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }

    private static String[] getProfilesAsArray(Optional<String> activeProfiles) {
        return activeProfiles.map(s -> s.split(",")).orElse(new String[]{"default"});
    }

}
