package com.sukinsan.cloudftp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sukinsan.cloudftp.util.CloudStorageImpl;
import com.sukinsan.koshcloudcore.item.FtpItem;
import com.sukinsan.koshcloudcore.util.CloudSyncUtil;
import com.sukinsan.koshcloudcore.util.CloudSyncUtilImpl;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;
import com.sukinsan.koshcloudcore.util.MyCloudStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FtpUtilsTest {

    private FtpUtils ftpUtils;
    private CloudSyncUtil cloudSyncUtil;
    private String host, username, password;
    private int port;
    private MyCloudStorage myCloudStorage;
    private Context context;

    @Before
    public void setUp() {
        ftpUtils = FtpUtilsImpl.newInstance(null);
        context = InstrumentationRegistry.getContext();
        myCloudStorage = new CloudStorageImpl(context);
        cloudSyncUtil = new CloudSyncUtilImpl(ftpUtils, Constant.getCloudFolder(), myCloudStorage);
        host = "109.227.106.230";
        username = "test";
        password = "test";
        port = 21;
    }

    @After
    public void finish() {
        ftpUtils.disconnect();
    }

    @Test
    public void check_can_get_list_of_files() throws IOException {
        ftpUtils.connect(host, port, username, password, false);

        assertThat(ftpUtils.readFolder("/").size(), is(1));
        assertThat(ftpUtils.readFolder("/test").size(), is(1));
        assertThat(ftpUtils.readFolder("/test/music").size(), is(1));
        assertThat(ftpUtils.readFolder("/test/music/sod2001").size(), is(3));
        assertThat(ftpUtils.readFolder("/test/music/sod2001/CD1").size(), is(18));
        assertThat(ftpUtils.readFolder("/test/music/sod2001/CD1/Covers").size(), is(3));
        assertThat(ftpUtils.readFolder("/test/music/sod2001/CD2 [Bonus Disc]").size(), is(6));
    }


    @Test
    public void check_if_paths_correct() throws IOException {
        ftpUtils.connect(host, port, username, password, false);

        FtpItem testFolder = ftpUtils.readFolder("/").get(0);
        FtpItem musicFolder = ftpUtils.readFolder(testFolder.getPath()).get(0);
        FtpItem sodFolder = ftpUtils.readFolder(musicFolder.getPath()).get(0);
        FtpItem sodCD1Folder = ftpUtils.readFolder(sodFolder.getPath()).get(0);
        List<FtpItem> sodCD1CoverFolder = ftpUtils.readFolder(sodCD1Folder.getPath());
        FtpItem sodCD2Folder = ftpUtils.readFolder(sodFolder.getPath()).get(1);

        assertThat(testFolder.getPath(), is("/test"));
        assertThat(musicFolder.getPath(), is("/test/music"));
        assertThat(sodFolder.getPath(), is("/test/music/sod2001"));
        assertThat(sodCD1Folder.getPath(), is("/test/music/sod2001/CD1"));
        assertThat(sodCD1CoverFolder.get(0).getPath(), is("/test/music/sod2001/CD1/Covers"));
        assertThat(sodCD2Folder.getPath(), is("/test/music/sod2001/CD2 [Bonus Disc]"));
    }

    @Test
    public void check_storage() throws IOException {
        SharedPreferences pref = context.getSharedPreferences(CloudSyncUtilImpl.class.getSimpleName(), Context.MODE_PRIVATE);

        pref.edit().putString("other setting","something").commit();

        assertThat(myCloudStorage.getAllPathStatuses().size(), is(0));

        myCloudStorage.setPathStatus("a", CloudSyncUtil.SyncStatus.SYNC_FINISHED);
        myCloudStorage.setPathStatus("b", CloudSyncUtil.SyncStatus.SYNC_PENDING);
        myCloudStorage.setPathStatus("b", CloudSyncUtil.SyncStatus.SYNC_NOT);

        assertThat(myCloudStorage.getAllPathStatuses().size(), is(2));
    }

    @Test
    public void check_syncing() throws IOException {
        //File cloudFolder = new File(Constant.getCloudFolder());
        FtpItem item = new FtpItem("/", "/test", true, 0);
        cloudSyncUtil.unSync(item);

        ftpUtils.connect(host, port, username, password, false);
        FtpItem ftpItem = ftpUtils.readFolder("/test/music/sod2001/CD1/").get(0);
        cloudSyncUtil.sync(null, ftpItem);

        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/back_2.jpg").exists(), is(true));
        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/Back.jpg").exists(), is(true));
        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/Front.jpg").exists(), is(true));

        cloudSyncUtil.unSync(item);
        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/back_2.jpg").exists(), is(false));
        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/Back.jpg").exists(), is(false));
        assertThat(new File(Constant.getCloudFolder(), "/test/music/sod2001/CD1/Covers/Front.jpg").exists(), is(false));
    }
}