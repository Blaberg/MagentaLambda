package com.magenta.Models;

import lombok.Data;

@Data
public class Message {
    private String action;
    private String type;
    private String subject;
    private String answer;
    private String destination;
    private String sender;
}
