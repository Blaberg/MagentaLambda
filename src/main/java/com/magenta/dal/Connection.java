package com.magenta.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationRequest;

import java.util.ArrayList;
import java.util.HashSet;


@DynamoDBTable(tableName = "PLACEHOLDER_CONNECTIONS_TABLE_NAME")
public class Connection {
    private static final String CONNECTIONS_TABLE_NAME = System.getenv("CONNECTIONS_TABLE_NAME");
    private final DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private String id;
    private String gamePin;


    @DynamoDBHashKey(attributeName = "id")
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "gamePin")
    public String getGamePin(){
        return gamePin;
    }
    public void setGamePin(String gamePin){
        this.gamePin = gamePin;
    }

    public Connection() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(CONNECTIONS_TABLE_NAME))
                .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    public void save(Connection connection){
        this.mapper.save(connection);
    }

    public Connection get(String id){
        return this.mapper.load(Connection.class, id);
    }

    public void delete(String  id){
        Connection connection = get(id);
        this.mapper.delete(connection);
    }


}
