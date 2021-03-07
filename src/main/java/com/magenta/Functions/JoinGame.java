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
        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);

        try {
            Message message = objectMapper.readValue(event.getBody(), Message.class);
            Game game = joinGame(message.getDestination(), message.getSender(),
                    event.getRequestContext().getConnectionId());

            PostToConnectionRequest post = new PostToConnectionRequest();
            post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(game).getBytes()));
            post.setConnectionId(event.getRequestContext().getConnectionId());
            api.postToConnection(post);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            response.setStatusCode(400);
            return response;
        }

        return response;
    }

    public Game joinGame(String pin, String player, String connectionID) {
        jedis.sadd(pin, connectionID);
        if (!player.equals("Scoreboard")) {
            jedis.hset("points:" + pin, player, "0");
        }
        jedis.hset("connections",connectionID,pin+":"+player);

        return new Game(jedis.smembers(pin), jedis.hgetAll("points#" + pin));
    }
}
