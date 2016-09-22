package guichaguri.remotecontrols;

import android.support.v4.media.RatingCompat;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The event handler
 * @author Guilherme Chaguri
 */
public class RemoteEventHandler extends MediaSessionCompat.Callback {
    private final RemoteControls rc;
    CallbackContext eventCallback;

    RemoteEventHandler(RemoteControls rc) {
        this.rc = rc;
    }

    private void sendEvent(String action, String key, Object value) {
        if(eventCallback == null) return;
        try {
            JSONObject obj = new JSONObject();

            obj.put("action", action);
            if(key != null) obj.put(key, value);

            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            eventCallback.sendPluginResult(result);
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
    }
    private void sendEvent(String action) {
        sendEvent(action, null, null);
    }

    @Override
    public void onPlay() {
        sendEvent("play");
    }
    @Override
    public void onPause() {
        sendEvent("pause");
    }
    @Override
    public void onStop() {
        sendEvent("stop");
    }
    @Override
    public void onSeekTo(long pos) {
        sendEvent("seek", "position", pos);
    }
    @Override
    public void onSkipToPrevious() {
        sendEvent("previous");
    }
    @Override
    public void onSkipToNext() {
        sendEvent("next");
    }
    @Override
    public void onSetRating(RatingCompat rating) {
        sendEvent("rate", "rating", rating.getPercentRating());
    }

    class RemoteVolumeHandler extends VolumeProviderCompat {
        RemoteVolumeHandler(boolean changeable, int currentVolume) {
            super(changeable ? VOLUME_CONTROL_ABSOLUTE : VOLUME_CONTROL_FIXED, 100, currentVolume);
        }

        boolean isChangeable() {
            return VOLUME_CONTROL_FIXED != getVolumeControl();
        }

        @Override
        public void onSetVolumeTo(int volume) {
            sendEvent("volume", "volume", volume);
        }
    }

}
