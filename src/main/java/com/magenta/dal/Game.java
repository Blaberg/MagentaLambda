package com.magenta.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@DynamoDBTable(tableName = "PLACEHOLDER_GAMES_TABLE_NAME")
public class Game {
    private static final String GAMES_TABLE_NAME = System.getenv("GAMES_TABLE_NAME");
    private final DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    @DynamoDBHashKey(attributeName = "pin")
    private String pin;

    @DynamoDBAttribute(attributeName = "connections")
    private Set<String> connections;

    @DynamoDBAttribute(attributeName = "players")
    private List<Player> players;

    public Game() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(GAMES_TABLE_NAME))
                .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
        this.connections = new HashSet<>();
        this.players = new ArrayList<>();
    }


    public void save(Game game){
        this.mapper.save(game);
    }

    public Game get(String id){
        return this.mapper.load(Game.class, id);
    }

    public Boolean delete(String id){
        Game game = get(id);
        if (game != null) {
            this.mapper.delete(game);
        } else {
            return false;
        }
        return true;
    }





}
