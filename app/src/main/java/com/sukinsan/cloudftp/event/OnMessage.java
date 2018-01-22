package com.sukinsan.cloudftp.event;

/**
 * Created by victor on 1/11/2018.
 */

public class OnMessage {
    public enum Action {
        SYNC_START,
        SYNC_SUCCESS,
        SYNC_FAILED,
        TEXT_MESSAGE, TEXT_ERROR, UNSYNC_SUCCESS,
    }

    public Action action;
    public String message;

    public OnMessage(Action action) {
        this.action = action;
    }

    public OnMessage(Action action, String message) {
        this.action = action;
        this.message = message;
    }
}
