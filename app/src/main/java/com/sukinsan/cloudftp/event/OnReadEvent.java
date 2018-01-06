package com.sukinsan.cloudftp.event;

import com.sukinsan.koshcloudcore.item.FtpItem;

import java.io.IOException;
import java.util.List;

/**
 * Created by victor on 1/4/2018.
 */

public class OnReadEvent {
    public List<FtpItem> list;
    public String errorMessage;

    public OnReadEvent(List<FtpItem> list) {
        this.list = list;
    }

    public OnReadEvent(IOException e) {
        this.errorMessage = e.getMessage();
    }
}
