package net.veldor.rutrackermobile.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import net.veldor.rutrackermobile.App;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static net.veldor.rutrackermobile.utils.TorrentOpener.intentCanBeHandled;

public class TorrentSharer {
    public static void requestShare(DocumentFile torrent) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, torrent.getUri());
        shareIntent.setType("application/x-bittorrent");
        shareIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_ACTIVITY_NEW_TASK);
        if(intentCanBeHandled(shareIntent)) {
            App.getInstance().startActivity(shareIntent);
        }
    }

    public static void magnetShare(String url) {
        Log.d("surprise", "TorrentSharer magnetShare: share magnet " + url);
    }
}
