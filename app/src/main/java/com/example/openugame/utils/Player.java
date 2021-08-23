package com.example.openugame.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Player {
    private String name;

    public Player(String name) throws Exception {
        if(!isValidName(name)){
            throw new Exception("Name is not valid");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", this.name);
        return result;
    }

    public static boolean isValidName(String name){
        String regex = "[a-zA-Z0-9]{2,16}";
        return Pattern.compile(regex).matcher(name).matches();
    }
}
