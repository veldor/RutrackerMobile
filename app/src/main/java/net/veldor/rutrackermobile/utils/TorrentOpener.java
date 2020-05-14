package net.veldor.rutrackermobile.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import net.veldor.rutrackermobile.App;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class TorrentOpener {
    public static void requestOpen(DocumentFile torrent) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(torrent.getUri(), "application/x-bittorrent");
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION|FLAG_ACTIVITY_NEW_TASK);
        if(intentCanBeHandled(intent)){
            App.getInstance().startActivity(intent);
        }
        else{
            Toast.makeText(App.getInstance(), "Не найдено приложение, открывающее торрент-файлы",Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean intentCanBeHandled(Intent intent){
        PackageManager packageManager = App.getInstance().getPackageManager();
        return intent.resolveActivity(packageManager) != null;
    }

    public static void magnetOpen(String url) {
        Log.d("surprise", "TorrentOpener magnetOpen: opening magnet...");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if(intentCanBeHandled(intent)){
            App.getInstance().startActivity(intent);
        }
        else{
            Toast.makeText(App.getInstance(), "Не найдено приложение, открывающее magnet",Toast.LENGTH_SHORT).show();
        }
    }
}
