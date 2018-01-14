package com.sukinsan.cloudftp.event;

/**
 * Created by victor on 1/14/2018.
 */

public class OnDownloaded {
    public String path;
    public long downloaded;

    public OnDownloaded(String path, long downloaded) {
        this.path = path;
        this.downloaded = downloaded;
    }
}
