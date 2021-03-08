package com.magenta.Functions;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.DependencyFactory;
import com.magenta.Models.Message;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateGame implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final Jedis jedis;
    private final AmazonApiGatewayManagementApi api;
    ObjectMapper objectMapper = new ObjectMapper();

    LambdaLogger logger;



    public CreateGame() throws IOException {
        jedis = DependencyFactory.jedis();
        api = DependencyFactory.api();

    }

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        String pin = generatePin();
        Message message = null;
        try {
            message = objectMapper.readValue(event.getBody(), Message.class);
        } catch (JsonProcessingException e) {
            logger.log(Arrays.toString(e.getStackTrace()));
        }
        jedis.hset("points:"+pin, message.getSender(), "0");
        jedis.sadd(pin, event.getRequestContext().getConnectionId());

        jedis.hset("connections",event.getRequestContext().getConnectionId(),pin+":"+event.getBody());

        AwsProxyResponse awsProxyResponse = new AwsProxyResponse();
        awsProxyResponse.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        awsProxyResponse.setHeaders(headers);

        Message response = new Message();
        response.setType("Game Created");
        response.setSubject(pin);
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString());
        logger.log("CONNECTION-ID: "+event.getRequestContext().getConnectionId());

        PostToConnectionRequest post = new PostToConnectionRequest();

        try {
            post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(response).getBytes()));
        } catch (JsonProcessingException e) {
            logger.log(Arrays.toString(e.getStackTrace()));

        }
        post.setConnectionId(event.getRequestContext().getConnectionId());
        api.postToConnection(post);

        return awsProxyResponse;
    }


    /**
     * Generate pin string.
     *
     * @return the string
     */
    public String generatePin() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        String pin = String.format("%06d", num);
        if (jedis.exists(pin)) {
            pin = generatePin();
        }
        return pin;

    }
}
