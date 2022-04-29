package me.pafias.boatracing.game.exceptions;

import me.pafias.boatracing.utils.CC;

public class GameNotFoundException extends Throwable {

    public GameNotFoundException() {

    }

    @Override
    public String getMessage() {
        return CC.translate("&cThere is currently no game available.");
    }

}
