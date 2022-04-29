package me.pafias.boatracing.game.exceptions;

import me.pafias.boatracing.utils.CC;

public class GameFullException extends Throwable{

    public GameFullException(){

    }

    @Override
    public String getMessage(){
        return CC.translate("&cThe game you are trying to join is full.");
    }

}
