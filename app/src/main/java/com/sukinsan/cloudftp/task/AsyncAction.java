package com.sukinsan.cloudftp.task;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by victor on 1/3/2018.
 */

public class AsyncAction extends AsyncTask<AsyncAction.Event, Void, Boolean> {

    private static final String TAG = AsyncAction.class.getSimpleName();

    public interface Event {
        void OnAsyncAction();
    }

    @Override
    protected Boolean doInBackground(Event... events) {
        for (Event event : events) {
            event.OnAsyncAction();
            Log.i(TAG,"event.OnAsyncAction() finished");
        }
        return true;
    }
}
