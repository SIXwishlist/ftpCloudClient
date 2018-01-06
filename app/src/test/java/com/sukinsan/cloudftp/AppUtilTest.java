package com.sukinsan.cloudftp;

import com.sukinsan.cloudftp.util.AppUtil;
import com.sukinsan.cloudftp.util.AppUtilImpl;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AppUtilTest {

    private AppUtil appUtil = new AppUtilImpl();

    @Test
    public void check_if_getParentOf_works_correct() throws Exception {
        assertEquals(appUtil.getParentOf(null), "/");
        assertEquals(appUtil.getParentOf("/"), "/");
        assertEquals(appUtil.getParentOf("/asdf"), "/");
        assertEquals(appUtil.getParentOf("/asdf/"), "/");
        assertEquals(appUtil.getParentOf("/abc/d"), "/abc");
        assertEquals(appUtil.getParentOf("/abc/d/"), "/abc");
        assertEquals(appUtil.getParentOf("/abc def/g"), "/abc def");
        assertEquals(appUtil.getParentOf("/abc def/g/"), "/abc def");
    }
}