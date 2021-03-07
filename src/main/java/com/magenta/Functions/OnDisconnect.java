package com.magenta.Functions;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.magenta.DependencyFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OnDisconnect implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final Jedis jedis;


    public OnDisconnect() throws IOException {
        jedis = DependencyFactory.jedis();

    }
    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        String[] pinPlayer = jedis.hget("connections", event.getRequestContext().getConnectionId()).split(":");
        jedis.hdel("points:"+pinPlayer[0], pinPlayer[1]);
        jedis.srem(pinPlayer[0], pinPlayer[1]);

        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        return response;
    }
}
