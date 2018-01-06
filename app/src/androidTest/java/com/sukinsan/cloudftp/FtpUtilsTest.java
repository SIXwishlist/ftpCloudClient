package com.sukinsan.cloudftp;

import android.support.test.runner.AndroidJUnit4;
import com.sukinsan.koshcloudcore.util.FtpItem;
import com.sukinsan.koshcloudcore.util.FtpUtils;
import com.sukinsan.koshcloudcore.util.FtpUtilsImpl;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    private String host, username, password;

    @Before
    public void setUp() {
        ftpUtils = new FtpUtilsImpl();
        host = "10.33.124.173";
        username = "guest";
        password = "guest";
    }

    @After
    public void finish() {
        ftpUtils.disconnect();
    }

    @Test
    public void check_can_get_list_of_files() {
//        FTPClientConfig config = new new FTPClientConfig();
//
//        ftpUtils = new FtpUtilsImpl()

        //ftpUtils.getFtpClient().setpass

        ftpUtils.connect(host, username, password, false);

        assertThat(ftpUtils.cdLs("/").size(), is(1));
        assertThat(ftpUtils.cdLs("/test").size(), is(1));
        assertThat(ftpUtils.cdLs("/test/music").size(), is(1));
        assertThat(ftpUtils.cdLs("/test/music/sod2001").size(), is(3));
        assertThat(ftpUtils.cdLs("/test/music/sod2001/CD1").size(), is(18));
        assertThat(ftpUtils.cdLs("/test/music/sod2001/CD1/Covers").size(), is(3));
        assertThat(ftpUtils.cdLs("/test/music/sod2001/CD2 [Bonus Disc]").size(), is(6));
    }

    @Test
    public void check_if_paths_correct() {
        ftpUtils.connect(host, username, password, false);
        FtpItem testFolder = ftpUtils.cdLs("/").get(0);
        FtpItem musicFolder = ftpUtils.cdLs(testFolder.getPath()).get(0);
        FtpItem sodFolder = ftpUtils.cdLs(musicFolder.getPath()).get(0);
        FtpItem sodCD1Folder = ftpUtils.cdLs(sodFolder.getPath()).get(0);
        List<FtpItem> sodCD1CoverFolder = ftpUtils.cdLs(sodCD1Folder.getPath());
        FtpItem sodCD2Folder = ftpUtils.cdLs(sodFolder.getPath()).get(1);

        assertThat(testFolder.getPath(), is("/test"));
        assertThat(musicFolder.getPath(), is("/test/music"));
        assertThat(sodFolder.getPath(), is("/test/music/sod2001"));
        assertThat(sodCD1Folder.getPath(), is("/test/music/sod2001/CD1"));
        assertThat(sodCD1CoverFolder.get(0).getPath(), is("/test/music/sod2001/CD1/Covers"));
        assertThat(sodCD2Folder.getPath(), is("/test/music/sod2001/CD2 [Bonus Disc]"));
    }
}
