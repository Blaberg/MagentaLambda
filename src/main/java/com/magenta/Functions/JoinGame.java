package com.magenta.Functions;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.DependencyFactory;
import com.magenta.Models.Game;
import com.magenta.Models.Message;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class JoinGame implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final Jedis jedis;
    private final AmazonApiGatewayManagementApi api;
    ObjectMapper objectMapper = new ObjectMapper();


    public JoinGame() throws IOException {
        jedis = DependencyFactory.jedis();
        api = DependencyFactory.api();
    }

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        AwsProxyResponse awsResponse = new AwsProxyResponse();
        awsResponse.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        awsResponse.setHeaders(headers);

        try {
            Message message = objectMapper.readValue(event.getBody(), Message.class);
            String connectionID = event.getRequestContext().getConnectionId();
            joinGame(message.getDestination(), message.getSender(),
                    connectionID);

            Message response = new Message();
            response.setType("Joined Game");
            response.setSubject(objectMapper.writeValueAsString(jedis.hgetAll(message.getDestination())));

            PostToConnectionRequest post = new PostToConnectionRequest();
            post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(message).getBytes()));
            post.setConnectionId(connectionID);
            api.postToConnection(post);
            broadcast(message.getDestination(), message.getSender(), connectionID);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            awsResponse.setStatusCode(400);
            return awsResponse;
        }

        return awsResponse;
    }

    public void joinGame(String pin, String player, String connectionID) {
        jedis.sadd(pin, connectionID);
        if (!player.equals("Scoreboard")) {
            jedis.hset("points:" + pin, player, "0");
        }
        jedis.hset("connections", connectionID, pin + ":" + player);
    }

    public void broadcast(String pin, String player, String connectionID) throws JsonProcessingException {
        PostToConnectionRequest post = new PostToConnectionRequest();
        Message message = new Message();
        message.setType("Player Joined");
        message.setSubject("0");
        message.setSender(player);
        post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(message).getBytes()));
        for (String connection : jedis.smembers(pin)) {
            if (!connection.equals(connectionID)) {
                post.setConnectionId(connection);
                api.postToConnection(post);
            }
        }
    }

}
