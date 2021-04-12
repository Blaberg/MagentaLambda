package com.magenta.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.services.s3.model.PutBucketAccelerateConfigurationRequest;

import java.util.ArrayList;
import java.util.HashSet;

@Data
@DynamoDBTable(tableName = "PLACEHOLDER_CONNECTIONS_TABLE_NAME")
public class Connection {
    private static final String CONNECTIONS_TABLE_NAME = System.getenv("CONNECTION_TABLE_NAME");
    private final DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    @DynamoDBHashKey(attributeName = "Id")
    private String id;

    @DynamoDBAttribute(attributeName = "Game Pin")
    private String gamePin;

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


}
