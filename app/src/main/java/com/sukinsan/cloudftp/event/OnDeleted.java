package com.sukinsan.cloudftp.event;

import com.sukinsan.koshcloudcore.item.FtpItem;

import java.io.IOException;

/**
 * Created by victor on 1/7/2018.
 */

public class OnDeleted {
    public FtpItem ftpItem;

    public OnDeleted(FtpItem ftpItem) {
        this.ftpItem = ftpItem;
    }
}
