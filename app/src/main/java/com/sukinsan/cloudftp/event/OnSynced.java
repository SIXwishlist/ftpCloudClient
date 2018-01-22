package com.sukinsan.cloudftp.event;

import com.sukinsan.koshcloudcore.item.FtpItem;

import java.io.IOException;

/**
 * Created by victor on 1/7/2018.
 */

public class OnSynced {
    public int amount;
    public FtpItem ftpItem;

    public OnSynced(int amount, FtpItem ftpItem) {
        this.amount = amount;
        this.ftpItem = ftpItem;
    }
}
