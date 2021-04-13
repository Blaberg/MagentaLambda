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
import com.magenta.dal.Connection;
import com.magenta.dal.Game;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JoinGame implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final AmazonApiGatewayManagementApi api;
    ObjectMapper objectMapper = new ObjectMapper();
    LambdaLogger logger;



    public JoinGame() throws IOException {
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
            Game game = new Game();
            game = game.get(message.getDestination());

            joinGame(game, message.getSender(),
                    connectionID);

            Message response = new Message();
            response.setType("Joined Game");
            response.setSubject(objectMapper.writeValueAsString(game.get(message.getDestination()).getPlayers()));

            LambdaLogger logger = context.getLogger();
            logger.log("DYNAMODB: "+game.get(message.getDestination()).getPlayers());

            PostToConnectionRequest post = new PostToConnectionRequest();
            post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(response).getBytes()));
            post.setConnectionId(connectionID);
            api.postToConnection(post);
            broadcast(game, message.getSender(), connectionID);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            awsResponse.setStatusCode(400);
            return awsResponse;
        }

        return awsResponse;
    }

    public void joinGame(Game game, String name, String connectionID) {
        game.getConnections().add(connectionID);
        if (!name.equals("Scoreboard")) {
            Game.Player player = new Game.Player();
            player.setName(name);
            player.setPoints(0);
            player.setId(connectionID);
            game.getPlayers().add(player);
        }
        game.save(game);
        Connection connection = new Connection();
        connection.setId(connectionID);
        connection.setGamePin(game.getPin());
        connection.save(connection);
    }

    public void broadcast(Game game, String player, String connectionID) throws JsonProcessingException {
        PostToConnectionRequest post = new PostToConnectionRequest();
        Message message = new Message();
        message.setType("Player Joined");
        message.setSubject("0");
        message.setSender(player);
        post.setData(ByteBuffer.wrap(objectMapper.writeValueAsString(message).getBytes()));
        for (String connection : game.getConnections()) {
            if (!Objects.equals(connection, connectionID)) {
                //logger.log(String.valueOf(Objects.equals(connection, connectionID)));
                try {
                    post.setConnectionId(connection);
                    api.postToConnection(post);
                }catch (Exception e){
                    game.getConnections().remove(connection);
                    game.save(game);
                }
            }
        }
    }

}
