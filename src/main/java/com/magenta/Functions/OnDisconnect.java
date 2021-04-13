package com.magenta.Functions;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.magenta.DependencyFactory;
import com.magenta.dal.Connection;
import com.magenta.dal.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnDisconnect implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {
    
    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        Connection connection = new Connection();
        connection = connection.get(event.getRequestContext().getConnectionId());

        Game game = new Game();
        game = game.get(connection.getGamePin());
        List<Game.Player> removelist = new ArrayList<>();
        for(Game.Player player: game.getPlayers()){
            if(player.getId().equals(connection.getId())){
                removelist.add(player);
            }
        }
        game.getPlayers().removeAll(removelist);
        game.getConnections().remove(connection.getId());
        if(game.getConnections().size()>0){
            game.save(game);
        }else {
            game.delete(game.getPin());
        }
        connection.delete(connection.getId());


        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);
        return response;
    }
}
