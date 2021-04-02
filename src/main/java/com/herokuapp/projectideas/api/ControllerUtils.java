package com.herokuapp.projectideas.api;

import java.util.UUID;

public class ControllerUtils {

    public static boolean isUUIDValid(String uuid) {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return true;
    }
}
