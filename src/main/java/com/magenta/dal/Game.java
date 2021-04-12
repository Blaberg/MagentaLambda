package com.magenta.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@DynamoDBTable(tableName = "PLACEHOLDER_GAMES_TABLE_NAME")
public class Game {
    private static final String GAMES_TABLE_NAME = System.getenv("GAMES_TABLE_NAME");
    private final DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private String pin;
    private Set<String> connections;
    private List<Player> players;


    @DynamoDBHashKey(attributeName = "pin")
    public String getPin(){
        return pin;
    }
    public void setPin(String pin){
        this.pin = pin;
    }

    @DynamoDBAttribute(attributeName = "connections")
    public Set<String> getConnections(){
        return connections;
    }
    public void setConnections(Set<String> connections){
        this.connections = connections;
    }

    @DynamoDBAttribute(attributeName = "players")
    public List<Player> getPlayers(){
        return players;
    }
    public void setPlayers(List<Player> players){
        this.players = players;
    }

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
    @DynamoDBDocument
    public static class Player {
        private String name;
        private int points;

        @DynamoDBAttribute(attributeName = "name")
        public String getName(){
            return name;
        }
        public void setName(String name){
            this.name = name;
        }

        @DynamoDBAttribute(attributeName = "points")
        public int getPoints(){
            return points;
        }
        public void setPoints(int points){
            this.points = points;
        }
    }



}
