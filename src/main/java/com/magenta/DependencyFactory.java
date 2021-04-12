
package com.magenta;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.Functions.Broadcast;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.util.Base64;

/**
 * The module containing all dependencies required by the {@link Broadcast}.
 */
public class DependencyFactory {

    private DependencyFactory() {
    }


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

        String configEndpoint = "magenta-redis.dngk3v.ng.0001.eun1.cache.amazonaws.com";
        int clusterPort = 6379;

        return new Jedis(configEndpoint, clusterPort);

    }

    public static AmazonApiGatewayManagementApi api() throws JsonProcessingException {
        String secret = getSecret();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(secret);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(actualObj.get("AccessID").textValue(), actualObj.get(
                "AccessKey").textValue());
        return AmazonApiGatewayManagementApiClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("2au27vmcn0.execute-api.eu-north-1.amazonaws.com/production", "eu-north-1"))
                .build();
    }

    public static String getSecret() {

        String secretName = "Credentials";
        String region = "eu-central-1";

        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
        // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
        // We rethrow the exception by default.

        String secret = null;
        String decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        }

        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
             secret = getSecretValueResult.getSecretString();
        }
        else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
        
        return secret;


    }


}
