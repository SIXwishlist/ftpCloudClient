package com.sukinsan.cloudftp.util;

/**
 * Created by victor on 1/4/2018.
 */

public interface AsyncFtpUtils {

    void connect(String host,int port, String username, String password, boolean ssl);

    boolean isConnected();

    void read(String pathOnConnected);

    void disconnect();
}
