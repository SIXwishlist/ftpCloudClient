package com.sukinsan.cloudftp.util;

import android.app.Activity;
import android.net.Uri;

import java.io.File;

/**
 * Created by victor on 1/8/2018.
 */

public interface SystemUtils {

    void exec(File file);

    void share(File file);

    Uri getUri(File file);

    String getFileMimeType(File file);

    boolean doWeNeedAnyPermissions();

    boolean doWeNeedToExplainWhyWeNeedThem(Activity activity);

    void askAllPermissionsWeNeed(Activity activity, int reqCode);

}
