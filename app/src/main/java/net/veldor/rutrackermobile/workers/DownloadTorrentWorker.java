package net.veldor.rutrackermobile.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.rutrackermobile.http.TorWebClient;
import net.veldor.rutrackermobile.notyfy.Notificator;
import net.veldor.rutrackermobile.utils.Preferences;
import net.veldor.rutrackermobile.utils.TorrentOpener;

public class DownloadTorrentWorker extends Worker {
    public static final String TORRENT_LINK = "torrent link";

    public DownloadTorrentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String href = data.getString(TORRENT_LINK);
        TorWebClient webClient = new TorWebClient();
        DocumentFile torrent = webClient.downloadTorrent(href);
        if(torrent != null){
            if(Preferences.getInstance().isTorrentOpen()){
                TorrentOpener.requestOpen(torrent);
            }
            // отправлю сообщение о скачанном файле
            Notificator.getInstance().sendTorrentLoadedNotification(torrent);
        }
        return Result.success();
    }
}
