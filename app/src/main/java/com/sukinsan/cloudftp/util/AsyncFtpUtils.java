package com.sukinsan.cloudftp.util;

import com.sukinsan.koshcloudcore.item.FtpItem;

import java.io.IOException;

/**
 * Created by victor on 1/4/2018.
 */

public interface AsyncFtpUtils {

    void connect(String host, int port, String username, String password, boolean ssl);

    boolean isConnected();

    void read(String pathOnConnected);

    int sync(FtpItem ftpItem) throws IOException;

    void disconnect();
}
