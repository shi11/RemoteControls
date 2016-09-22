package guichaguri.remotecontrols;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * RemoteControls for Android
 * @author Guilherme Chaguri
 */
public class RemoteControls extends CordovaPlugin {

    static RemoteControls instance;

    MediaSessionCompat session;
    PlaybackStateCompat.Builder pb;
    MediaMetadataCompat.Builder md;

    RemoteEventHandler eventHandler;
    RemoteEventHandler.RemoteVolumeHandler volumeHandler;
    RemoteNotification notification;

    private void init(CordovaInterface cordova) {
        instance = this;

        Activity activity = cordova.getActivity();

        ComponentName name = new ComponentName(activity, RemoteNotification.class);
        session = new MediaSessionCompat(activity, "RemoteControls", name, null);

        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        pb = new PlaybackStateCompat.Builder();
        md = new MediaMetadataCompat.Builder();

        eventHandler = new RemoteEventHandler(this);
        volumeHandler = eventHandler.new RemoteVolumeHandler(true, 100);
        notification = new RemoteNotification(this);

        session.setCallback(eventHandler);
        session.setPlaybackToRemote(volumeHandler);

        activity.registerReceiver(notification, new IntentFilter(RemoteNotification.ACTION_BUTTON));

        activity.startService(new Intent(activity.getBaseContext(), RemoteNotification.NotificationService.class));
    }

    private void destroy() {
        notification.remove();
        session.release();
        cordova.getActivity().unregisterReceiver(notification);

        if(instance == this) instance = null;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        init(cordova);
    }

    @Override
    public void onDestroy() {
        destroy();
    }

    @Override
    public void onReset() {
        destroy();
        init(cordova);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("updatePlayback")) {
            JSONObject obj = args.length() > 0 ? args.getJSONObject(0) : null;
            updatePlayback(obj, callbackContext);
            return true;
        } else if(action.equals("updateMetadata")) {
            JSONObject obj = args.length() > 0 ? args.getJSONObject(0) : null;
            updateMetadata(obj, callbackContext);
            return true;
        } else if(action.equals("updateActions")) {
            JSONObject obj = args.length() > 0 ? args.getJSONObject(0) : null;
            updateActions(obj, callbackContext);
            return true;
        } else if(action.equals("handleEvents")) {
            eventHandler.eventCallback = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }

    private void updateActions(JSONObject obj, CallbackContext callbackContext) throws JSONException {
        if(obj != null) {
            long actions = 0;

            boolean stop = obj.has("stop") && obj.getBoolean("stop");
            boolean pause = obj.has("pause") && obj.getBoolean("pause");
            boolean play = obj.has("play") && obj.getBoolean("play");
            boolean previous = obj.has("skipToPrevious") && obj.getBoolean("skipToPrevious");
            boolean next = obj.has("skipToNext") && obj.getBoolean("skipToNext");

            notification.stopButton = stop;
            notification.pauseButton = pause;
            notification.playButton = play;
            notification.previousButton = previous;
            notification.nextButton = next;

            if(stop)
                actions |= PlaybackStateCompat.ACTION_STOP;
            if(pause)
                actions |= PlaybackStateCompat.ACTION_PAUSE;
            if(play)
                actions |= PlaybackStateCompat.ACTION_PLAY;
            if(previous)
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            if(next)
                actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
            if(obj.has("rate") && obj.getBoolean("rate"))
                actions |= PlaybackStateCompat.ACTION_SET_RATING;
            if(obj.has("seekTo") && obj.getBoolean("seekTo"))
                actions |= PlaybackStateCompat.ACTION_SEEK_TO;

            boolean volumeChangeable = obj.has("volume") && obj.getBoolean("volume");
            if(volumeChangeable != volumeHandler.isChangeable()) {
                volumeHandler = eventHandler.new RemoteVolumeHandler(volumeChangeable, volumeHandler.getCurrentVolume());
                session.setPlaybackToRemote(volumeHandler);
            }

            pb.setActions(actions);
            session.setPlaybackState(pb.build());
            notification.update();
            callbackContext.success("Actions updated");
        } else {
            callbackContext.error("Missing properties");
        }
    }

    private void updatePlayback(JSONObject obj, CallbackContext callbackContext) throws JSONException {
        if(obj != null) {
            int state = PlaybackStateCompat.STATE_NONE;
            notification.isPlaying = false;
            switch(obj.getInt("state")) {
                case -1:
                    state = PlaybackStateCompat.STATE_ERROR;
                    break;
                case 0:
                    state = PlaybackStateCompat.STATE_STOPPED;
                    break;
                case 1:
                    state = PlaybackStateCompat.STATE_PLAYING;
                    notification.isPlaying = true;
                    break;
                case 2:
                    state = PlaybackStateCompat.STATE_PAUSED;
                    break;
                case 3:
                    state = PlaybackStateCompat.STATE_BUFFERING;
                    notification.isPlaying = true;
                    break;
            }

            float speed = obj.has("speed") ? (float)obj.getDouble("speed") : 1;
            int elapsedTime = obj.has("elapsedTime") ? obj.getInt("elapsedTime") : 0;
            pb.setState(state, elapsedTime, speed);

            if(obj.has("bufferedTime"))
                pb.setBufferedPosition(obj.getInt("bufferedTime"));
            if(obj.has("volume"))
                volumeHandler.setCurrentVolume(obj.getInt("volume"));

            session.setPlaybackState(pb.build());
            notification.update();
            callbackContext.success("Playback updated");
        } else {
            callbackContext.error("Missing properties");
        }
    }

    private void updateMetadata(final JSONObject obj, final CallbackContext callbackContext) {
        if(obj != null) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String artist = obj.getString("artist");
                        String title = obj.getString("title");

                        md.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
                        md.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
                        notification.artist = artist;
                        notification.title = title;
                        notification.album = null;
                        notification.cover = null;
                        notification.color = null;

                        if(obj.has("album")) {
                            String album = obj.getString("album");
                            md.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
                            notification.album = album;
                        }
                        if(obj.has("genre"))
                            md.putString(MediaMetadataCompat.METADATA_KEY_GENRE, obj.getString("genre"));
                        if(obj.has("rating"))
                            md.putRating(MediaMetadataCompat.METADATA_KEY_RATING, RatingCompat.newPercentageRating(obj.getInt("rating")));
                        if(obj.has("description"))
                            md.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, obj.getString("description"));
                        if(obj.has("date"))
                            md.putString(MediaMetadataCompat.METADATA_KEY_DATE, obj.getString("date"));
                        if(obj.has("color"))
                            notification.color = obj.getInt("color");
                        if(obj.has("duration"))
                            md.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, obj.getLong("duration"));
                        if(obj.has("cover"))
                            loadCover(obj.getString("cover"));

                        session.setMetadata(md.build());
                        session.setActive(true);
                        notification.update();
                        callbackContext.success("Metadata updated");
                    } catch(JSONException e) {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                    } catch(IOException e) {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
                    }
                }
            });
        } else {
            session.setActive(false);
            notification.remove();
            callbackContext.success("Metadata removed");
        }
    }

    private void loadCover(final String coverUri) throws IOException {
        Bitmap cover;
        InputStream input;

        if(coverUri.matches("https?:\\/\\/.*")) { // HTTP URL
            URLConnection connection = new URL(coverUri).openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            cover = BitmapFactory.decodeStream(input);
        } else { // ASSET FILE
            input = cordova.getActivity().getAssets().open("www/" + coverUri);
            cover = BitmapFactory.decodeStream(input);
        }

        md.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, cover);
        notification.cover = cover;
        input.close();
    }
}
