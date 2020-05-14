package net.veldor.rutrackermobile.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.utils.TorrentOpener;
import net.veldor.rutrackermobile.utils.TorrentSharer;


public class TorrentActionsReceiver extends BroadcastReceiver {
    public static final String EXTRA_ACTION_TYPE = "action type";
    public static final String ACTION_TYPE_SHARE = "share";
    public static final String ACTION_TYPE_OPEN = "open";
    public static final String TORRENT_URI = "torrent uri";
    public static final String EXTRA_NOTIFICATION_ID = "notification id";

    @Override
    public void onReceive(Context context, Intent intent) {
        // закрою меню уведомлений
        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeIntent);
        // закрою уведомление, отправившее интент
        int intentId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        if(intentId > 0){
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.cancel(intentId);
        }
        // получу тип действия
        String actionType = intent.getStringExtra(EXTRA_ACTION_TYPE);
        if(actionType != null){
            String url = intent.getStringExtra(TORRENT_URI);
            Log.d("surprise", "TorrentActionsReceiver onReceive: Url is " + url);
            if(url != null){
                Uri uri = Uri.parse(url);
                DocumentFile torrent = DocumentFile.fromSingleUri(App.getInstance(), uri);
                if(torrent != null && torrent.isFile()){
                    if(actionType.equals(ACTION_TYPE_OPEN)){
                        TorrentOpener.requestOpen(torrent);
                    }
                    else if(actionType.equals(ACTION_TYPE_SHARE)){
                        TorrentSharer.requestShare(torrent);
                    }
                }
                else{
                    Toast.makeText(App.getInstance(), "Торрент-файл не найден",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
