package com.sukinsan.cloudftp.event;

/**
 * Created by victor on 1/11/2018.
 */

public class OnMessage {
    public enum Action {
        SYNC_STATUS,
        SYNC_UNSYNCED,
        TEXT_MESSAGE
    }

    public Action action;
    public String message;

    public OnMessage(String message) {
        this.action = Action.TEXT_MESSAGE;
        this.message = message;
    }

    public OnMessage(Action action) {
        this.action = action;
    }

    public OnMessage(Action action, String message) {
        this.action = action;
        this.message = message;
    }
}
