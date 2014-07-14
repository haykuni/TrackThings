package net.mootoh.trackthings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.WearableListenerService;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Date;

public class HandheldService extends WearableListenerService {
    private static final String TAG = "HandheldService";
    private static final String FIELD_CONTEXT = "context";
    public static final String SHARED_PREF = "TrackThingsSharedPref";

    @Override
    public void onCreate() {
        super.onCreate();
//        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents + " for " + getPackageName());

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String context = DataMap.fromByteArray(event.getDataItem().getData()).get(FIELD_CONTEXT);
                Log.d(TAG, "received context: " + context);

                saveCurrentContext(context);
                uploadContext(context);
                break;
            }
        }

    }

    private void kickMainActivity() {
        Intent intent = new Intent(this, HandheldActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // save the current context locally
    private void saveCurrentContext(String context) {
        SharedPreferences pref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.current_context), context);
        editor.putLong(getString(R.string.last_updated), (new Date().getTime()));
        editor.commit();
        Log.d(TAG, "saved to shared pref");
    }

    // save the current context to a server
    private void uploadContext(String context) {
        ParseObject po = new ParseObject("Context");
        po.put("thing", context);
        po.put("timestamp", new Date());
        po.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                kickMainActivity();
            }
        });
    }
}