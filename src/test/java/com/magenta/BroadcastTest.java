package com.magenta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.magenta.Functions.Broadcast;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BroadcastTest {

    @Test
    public void handleRequest_shouldReturnConstantValue() throws IOException {
        Broadcast function = new Broadcast();
        APIGatewayV2WebSocketEvent proxyRequest = new APIGatewayV2WebSocketEvent();
        Object result = function.handleRequest(proxyRequest, null);
        assertEquals("echo", result);
    }
}
