package com.magenta.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class Player {
    @DynamoDBAttribute
    private String name;

   @DynamoDBAttribute
   private int points;
}
