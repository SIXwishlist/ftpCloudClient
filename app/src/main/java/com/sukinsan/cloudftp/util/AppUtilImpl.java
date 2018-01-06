package com.sukinsan.cloudftp.util;

/**
 * Created by victor on 1/4/2018.
 */

public class AppUtilImpl implements AppUtil {
    private final static String patternToFindLastChild = "(.*)(\\/.+?)$";

    @Override
    public String getParentOf(String filePath) {
        if (filePath == null) {
            return "/";
        }
        String res = filePath.trim().replaceFirst(patternToFindLastChild, "$1");
        return res.isEmpty() ? "/" : res;
    }
}
