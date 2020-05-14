package net.veldor.rutrackermobile.view_models;

import android.content.Intent;
import android.widget.ImageView;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.workers.DownloadTorrentWorker;
import net.veldor.rutrackermobile.workers.TopicRequestWorker;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static net.veldor.rutrackermobile.utils.TorrentOpener.intentCanBeHandled;
import static net.veldor.rutrackermobile.workers.DownloadTorrentWorker.TORRENT_LINK;
import static net.veldor.rutrackermobile.workers.SearchWorker.SEARCH_ACTION;
import static net.veldor.rutrackermobile.workers.TopicRequestWorker.REQUESTED_URL;
import static net.veldor.rutrackermobile.workers.TopicRequestWorker.REQUEST_ACTION;


public class TopicViewModel extends ViewModel {

    public void requestPage(String url) {
        // передам URL, который нужно загрузить
        Data inputData = new Data.Builder()
                .putString(REQUESTED_URL, url)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(TopicRequestWorker.class).addTag(REQUEST_ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(REQUEST_ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }

    public void loadPoster(String imageUrl, ImageView mPosterView) {
        // гружу картинку на место
        Glide
            .with(mPosterView)
            .load(imageUrl)
            .into(mPosterView);
    }


    public void downloadTorrent(String torrentUrl) {
        // запущу рабочего, который загрузит torrent
        // передам запрос, который нужно загрузить
        Data inputData = new Data.Builder()
                .putString(TORRENT_LINK, torrentUrl)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(DownloadTorrentWorker.class)
                .addTag(SEARCH_ACTION)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(App.getInstance())
                .enqueueUniqueWork(SEARCH_ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }

    public void handleMagnet(String magnet) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_TEXT, magnet);
        if(intentCanBeHandled(shareIntent)) {
            App.getInstance().startActivity(shareIntent);
        }
    }
}
