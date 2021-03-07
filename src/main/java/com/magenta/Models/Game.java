package com.magenta.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class Game {
    Set<String> sessions;
    Map<String,String> players;
}
