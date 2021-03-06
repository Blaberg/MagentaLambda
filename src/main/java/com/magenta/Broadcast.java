package com.magenta;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.services.s3.S3Client;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

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
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    LambdaLogger logger;


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
        LambdaLogger logger = context.getLogger();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);

        broadcast(jedis.smembers("Connections"), event.getRequestContext().getConnectionId(), event.getBody());
        return response;
    }

    public void broadcast(Set<String> connections, String connectionID, String message) {

        PostToConnectionRequest post = new PostToConnectionRequest();
        post.setData(ByteBuffer.wrap(message.getBytes()));

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
