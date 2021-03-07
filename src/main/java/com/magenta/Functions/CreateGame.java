package com.magenta.Functions;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.magenta.DependencyFactory;
import com.magenta.Models.Message;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class CreateGame implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final Jedis jedis;
    private final AmazonApiGatewayManagementApi api;


    public CreateGame() throws IOException {
        jedis = DependencyFactory.jedis();
        api = DependencyFactory.api();

    }

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        String pin = generatePin();
        jedis.hset("points:"+pin, event.getBody(), "0");
        jedis.sadd(pin, event.getRequestContext().getConnectionId());

        jedis.hset("connections",event.getRequestContext().getConnectionId(),pin+":"+event.getBody());

        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);

        Message message = new Message();
        message.setType("Game Created");
        message.setSender(pin);

        PostToConnectionRequest post = new PostToConnectionRequest();
        post.setData(ByteBuffer.wrap(pin.getBytes()));
        post.setConnectionId(event.getRequestContext().getConnectionId());
        api.postToConnection(post);

        return response;
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
