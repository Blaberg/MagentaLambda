package com.magenta.Functions;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.magenta.DependencyFactory;
import lombok.SneakyThrows;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class OnOpen implements RequestHandler<APIGatewayV2WebSocketEvent, Object>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    ObjectMapper objectMapper = new ObjectMapper();
    private final Jedis jedis;

    public OnOpen() throws IOException {
        jedis = DependencyFactory.jedis();
    }


    @SneakyThrows
    @Override
    public APIGatewayV2WebSocketResponse handleRequest(final APIGatewayV2WebSocketEvent event, final Context context)
    {

        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);


        return response;
    }

}