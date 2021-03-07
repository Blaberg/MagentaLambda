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
import com.magenta.Models.Message;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class Broadcast implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private final Jedis jedis;
    private final S3Client s3Client;
    private final AmazonApiGatewayManagementApi api;
    ObjectMapper objectMapper = new ObjectMapper();



    public Broadcast() throws IOException {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        jedis = DependencyFactory.jedis();
        api = DependencyFactory.api();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    @Override
    public Object handleRequest(final APIGatewayV2WebSocketEvent event, final Context context) {
        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        try {
            Message message = objectMapper.readValue(event.getBody(), Message.class);
            broadcast(message.getDestination(), message.getSender(), event.getBody());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            response.setStatusCode(400);
        }
        return response;
    }

    public void broadcast(String game, String connectionID, String message) {

        PostToConnectionRequest post = new PostToConnectionRequest();
        post.setData(ByteBuffer.wrap(message.getBytes()));
        Set<String> connections = jedis.smembers(game);

        for (String connection : connections) {
            if (!connection.equals(connectionID)) {
                try {
                    post.setConnectionId(connection);
                    api.postToConnection(post);
                }catch (Exception e){
                    jedis.srem(connection, "Connections");
                }
            }
        }
    }
}
