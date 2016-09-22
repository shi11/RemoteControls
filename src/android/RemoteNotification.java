package guichaguri.remotecontrols;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v7.app.NotificationCompat;

/**
 * The Notification Updater
 * @author Guilherme Chaguri
 */
public class RemoteNotification extends BroadcastReceiver {
    static final String ACTION_BUTTON = "remote-controls-button";

    private static final int BUTTON_PLAY = 0;
    private static final int BUTTON_PAUSE = 1;
    private static final int BUTTON_STOP = 2;
    private static final int BUTTON_NEXT = 3;
    private static final int BUTTON_PREVIOUS = 4;
    private static final int BUTTON_REMOVE = 5;

    private final RemoteControls rc;

    String title, album, artist;
    Bitmap cover;
    Integer color = null;
    boolean playButton, pauseButton, stopButton, nextButton, previousButton;
    boolean isPlaying;
    boolean showWhenPaused = true;

    RemoteNotification(RemoteControls rc) {
        this.rc = rc;
    }

    void update() {
        if(!rc.session.isActive() || (!isPlaying && !showWhenPaused)) {
            remove();
            return;
        }
        Activity activity = rc.cordova.getActivity();

        NotificationCompat.Builder nc = new NotificationCompat.Builder(activity);
        nc.setStyle(new NotificationCompat.MediaStyle().setMediaSession(rc.session.getSessionToken()));
        nc.setLargeIcon(cover);
        nc.setContentTitle(title);
        nc.setContentText(artist);
        nc.setContentInfo(album);
        nc.setOngoing(isPlaying);
        nc.setColor(color == null ? NotificationCompat.COLOR_DEFAULT : color);

        Resources r = activity.getResources();
        String packageName = activity.getPackageName();
        int previous = r.getIdentifier("previous", "drawable", packageName);
        int pause = r.getIdentifier("pause", "drawable", packageName);
        int play = r.getIdentifier("play", "drawable", packageName);
        int stop = r.getIdentifier("stop", "drawable", packageName);
        int next = r.getIdentifier("next", "drawable", packageName);

        nc.setSmallIcon(play);

        if(previousButton)
            nc.addAction(createAction(activity, previous, "Previous", BUTTON_PREVIOUS));
        if(pauseButton && isPlaying)
            nc.addAction(createAction(activity, pause, "Pause", BUTTON_PAUSE));
        if(playButton && !isPlaying)
            nc.addAction(createAction(activity, play, "Play", BUTTON_PLAY));
        if(stopButton)
            nc.addAction(createAction(activity, stop, "Stop", BUTTON_STOP));
        if(nextButton)
            nc.addAction(createAction(activity, next, "Next", BUTTON_NEXT));

        showWhenPaused = true;

        // Open the app when the notification is clicked
        Intent openApp = new Intent(activity, activity.getClass());
        openApp.setAction(Intent.ACTION_MAIN);
        openApp.addCategory(Intent.CATEGORY_LAUNCHER);
        nc.setContentIntent(PendingIntent.getActivity(activity, 0, openApp, 0));

        if(!isPlaying) {
            // Remove notification
            Intent remove = new Intent(ACTION_BUTTON).putExtra("button", BUTTON_REMOVE);
            nc.setDeleteIntent(PendingIntent.getBroadcast(activity, BUTTON_REMOVE, remove, 0));
        }

        NotificationManagerCompat.from(activity).notify(0, nc.build());
    }

    void remove() {
        NotificationManagerCompat.from(rc.cordova.getActivity()).cancel(0);
    }

    private NotificationCompat.Action createAction(Activity activity, int icon, String title, int buttonId) {
        Intent intent = new Intent(ACTION_BUTTON).putExtra("button-action", buttonId);
        PendingIntent i = PendingIntent.getBroadcast(activity, buttonId, intent, 0);
        return new NotificationCompat.Action(icon, title, i);
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        String action = intent.getAction();
        boolean isButton = action.equals(ACTION_BUTTON) && intent.hasExtra("button-action");
        boolean isMedia = action.equals(Intent.ACTION_MEDIA_BUTTON) && intent.hasExtra(Intent.EXTRA_KEY_EVENT);
        if(!isButton && !isMedia) return;

        final int button = isButton ? intent.getIntExtra("button-action", -1) : -1;

        rc.cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if(button == -1) {
                    MediaButtonReceiver.handleIntent(rc.session, intent);
                } else if(button == BUTTON_PLAY) {
                    rc.eventHandler.onPlay();
                } else if(button == BUTTON_PAUSE) {
                    rc.eventHandler.onPause();
                } else if(button == BUTTON_STOP) {
                    rc.eventHandler.onStop();
                } else if(button == BUTTON_NEXT) {
                    rc.eventHandler.onSkipToNext();
                } else if(button == BUTTON_PREVIOUS) {
                    rc.eventHandler.onSkipToPrevious();
                } else if(button == BUTTON_REMOVE) {
                    showWhenPaused = false;
                }
            }
        });
    }

    public static class NotificationService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return START_NOT_STICKY;
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            if(RemoteControls.instance != null && RemoteControls.instance.notification != null) {
                RemoteControls.instance.notification.remove();
            }
            stopSelf();
        }

    }
}
