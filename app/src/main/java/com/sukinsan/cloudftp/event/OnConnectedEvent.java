package com.sukinsan.cloudftp.event;

/**
 * Created by victor on 1/4/2018.
 */

public class OnConnectedEvent {
    public Boolean success;

    public OnConnectedEvent(Boolean success) {
        this.success = success;
    }
}
