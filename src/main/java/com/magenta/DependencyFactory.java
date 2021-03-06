
package com.magenta;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

/**
 * The module containing all dependencies required by the {@link Broadcast}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of S3Client
     */
    public static S3Client s3Client() {
        return S3Client.builder()
                       .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                       .region(Region.EU_NORTH_1)
                       .httpClientBuilder(UrlConnectionHttpClient.builder())
                       .build();
    }

    public static Jedis jedis() throws IOException {

        String configEndpoint = "meganta-redis.dngk3v.0001.eun1.cache.amazonaws.com";
        int clusterPort = 6379;

        return new Jedis(configEndpoint,clusterPort);

    }

    public static AmazonApiGatewayManagementApi api() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIA23FTGTHROLMSI6RP", "ghPHt1RIS/q+x//K" +
                "/lZtS5QvH4iAfuRMKJJO/F+z");
        return AmazonApiGatewayManagementApiClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("uyssdmmwll" +
                        ".execute-api" +
                        ".eu-north-1.amazonaws.com/test", "eu-north-1"))
                .build();
    }
}
