package com.sukinsan.cloudftp.util;

import com.sukinsan.koshcloudcore.item.FtpItem;

import java.io.IOException;

/**
 * Created by victor on 1/4/2018.
 */

public interface AsyncFtpUtils {

    boolean isConnected();

    void connect(String host, int port, String username, String password, boolean ssl);

    void read(String pathOnConnected);

    void delete(FtpItem ftpItem);

    void unSync(FtpItem ftpItem);

    void disconnect();
}
