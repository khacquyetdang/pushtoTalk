package com.pushtotalk.dang.pushtotalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class HeadsetButtonReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = HeadsetButtonReceiver.class.getSimpleName();
    private static final int LONG_PRESS_DELAY = 500;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String intentAction = intent.getAction();
        Log.i(LOG_TAG, intentAction);
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = (KeyEvent) intent
                .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            Log.i(LOG_TAG, "NULL");
            return;
        }
        int action = event.getAction();

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                Log.i(LOG_TAG, "  press");
                if (action == KeyEvent.ACTION_UP) {
                    if (SystemClock.uptimeMillis() - event.getDownTime() > LONG_PRESS_DELAY) {
                        Log.i(LOG_TAG, " long press");
                    }
                }
                break;
        }
        abortBroadcast();


    }

}
