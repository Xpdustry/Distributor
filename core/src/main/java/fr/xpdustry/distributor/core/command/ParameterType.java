package fr.xpdustry.distributor.core.command;

import arc.func.*;
import arc.struct.*;


public enum ParameterType{
    numeric((text) -> {
        if(text.startsWith("-") || text.startsWith("+")){
            text = text.substring(1);
        }

        if(!text.isEmpty()){
            return text.chars().allMatch(Character::isDigit);
        }else{
            return false;
        }
    }),

    decimal((text) -> {
        if(text.startsWith("-") || text.startsWith("+")){
            text = text.substring(1);
        }

        if(!text.isEmpty()){
            boolean[] hadComma = {false};

            return text.chars().allMatch(i -> {
                if(Character.isDigit(i)){
                    return true;
                }else if((i == '.' || i == ',') && !hadComma[0]){
                    hadComma[0] = true;
                    return true;
                }else{
                    return false;
                }
            });

        }else{
            return false;
        }
    }),

    bool((text) -> true),

    string((text) -> true);

    public final Boolf<String> check;
    public static final ObjectMap<String, ParameterType> all;

    static{
        all = new ObjectMap<>();
        for(ParameterType type : values()){
            all.put(type.name(), type);
        }
    }

    ParameterType(Boolf<String> check){
        this.check = check;
    }
}
