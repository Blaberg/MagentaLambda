package com.magenta.Models;

import lombok.Data;

@Data
public class Message {
    private String type;
    private String snippet;
    private String answer;
    private String destination;
    private String sender;
}
