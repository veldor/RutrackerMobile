package net.veldor.rutrackermobile.notyfy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.receivers.TorrentActionsReceiver;

public class Notificator {

    private static final String DOWNLOADED_TORRENT_CHANNEL_ID = "Downloaded torrent";
    private static final int START_SHARING_REQUEST_CODE = 1;
    private static final int START_OPEN_REQUEST_CODE = 2;
    private static Notificator instance;
    private int mLastNotificationId = 100;

    private final App mContext;
    private final NotificationManager mNotificationManager;

    public static Notificator getInstance() {
        if(instance == null){
            instance = new Notificator();
        }
        return instance;
    }

    private Notificator(){
        mContext = App.getInstance();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // создам каналы уведомлений
        createChannels();
    }


    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager != null) {
                // создам канал уведомлений о скачанном торренте
                NotificationChannel nc = new NotificationChannel(DOWNLOADED_TORRENT_CHANNEL_ID, mContext.getString(R.string.timer_channel_description), NotificationManager.IMPORTANCE_HIGH);
                nc.setDescription(mContext.getString(R.string.timer_channel_description));
                nc.enableLights(true);
                nc.setLightColor(Color.BLUE);
                nc.enableVibration(true);
                mNotificationManager.createNotificationChannel(nc);
            }
        }
    }

    public void sendTorrentLoadedNotification(DocumentFile torrent) {
        String torrentUri = torrent.getUri().toString();
        // создам интент для функции отправки файла
        Intent shareIntent = new Intent(mContext, TorrentActionsReceiver.class);
        shareIntent.setData(torrent.getUri());
        shareIntent.putExtra(TorrentActionsReceiver.EXTRA_ACTION_TYPE, TorrentActionsReceiver.ACTION_TYPE_SHARE);
        shareIntent.putExtra(TorrentActionsReceiver.TORRENT_URI, torrentUri);
        shareIntent.putExtra(TorrentActionsReceiver.EXTRA_NOTIFICATION_ID, mLastNotificationId);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent sharePendingIntent = PendingIntent.getBroadcast(mContext, START_SHARING_REQUEST_CODE, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // создам интент для функции открытия файла

        Intent openIntent = new Intent(mContext, TorrentActionsReceiver.class);
        openIntent.putExtra(TorrentActionsReceiver.EXTRA_ACTION_TYPE, TorrentActionsReceiver.ACTION_TYPE_OPEN);
        openIntent.putExtra(TorrentActionsReceiver.TORRENT_URI, torrentUri);
        openIntent.putExtra(TorrentActionsReceiver.EXTRA_NOTIFICATION_ID, mLastNotificationId);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent openPendingIntent = PendingIntent.getBroadcast(mContext, START_OPEN_REQUEST_CODE, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, DOWNLOADED_TORRENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_download_green_24dp)
                .setContentTitle("Торрент загружен")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(torrent.getName()))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_share_black_24dp, "Отправить", sharePendingIntent)
                .addAction(R.drawable.ic_open_white_24dp, "Открыть", openPendingIntent);
        Notification notification = notificationBuilder.build();
        mNotificationManager.notify(mLastNotificationId, notification);
        mLastNotificationId++;
    }
}
