package com.sukinsan.cloudftp.task;

import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by victor on 1/3/2018.
 */

public class AsyncAction extends AsyncTask<AsyncAction.Event, Void, Boolean> {

    private static final String TAG = AsyncAction.class.getSimpleName();

    private Object res;
    private boolean finished;

    public interface Event {
        void OnAsyncAction();
    }

    public AsyncAction() {
        finished = false;
    }

    public void setRes(Object res) {
        this.res = res;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    protected Boolean doInBackground(Event... events) {
        for (Event event : events) {
            event.OnAsyncAction();
            Log.i(TAG, "event.OnAsyncAction() finished");
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        try {
            finished = true;
        } finally {
            EventBus.getDefault().post(res);
        }
    }
}
