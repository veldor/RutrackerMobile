package net.veldor.rutrackermobile.view_models;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.ui.SettingsActivity;
import net.veldor.rutrackermobile.ui.TopicActivity;
import net.veldor.rutrackermobile.utils.MyFileReader;
import net.veldor.rutrackermobile.utils.XMLHandler;
import net.veldor.rutrackermobile.workers.DownloadTorrentWorker;
import net.veldor.rutrackermobile.workers.RequestWorker;
import net.veldor.rutrackermobile.workers.SearchWorker;

import java.util.ArrayList;
import java.util.Stack;

import static net.veldor.rutrackermobile.workers.DownloadTorrentWorker.TORRENT_LINK;
import static net.veldor.rutrackermobile.workers.RequestWorker.REQUESTED_URL;
import static net.veldor.rutrackermobile.workers.RequestWorker.REQUEST_ACTION;
import static net.veldor.rutrackermobile.workers.SearchWorker.SEARCH_ACTION;
import static net.veldor.rutrackermobile.workers.SearchWorker.SEARCH_STRING;

public class BrowserViewModel extends ViewModel {

    private Stack<String> mHistory;

    public void loadPage(String url) {
        // передам URL, который нужно загрузить
        Data inputData = new Data.Builder()
                .putString(REQUESTED_URL, url)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(RequestWorker.class).addTag(REQUEST_ACTION).setInputData(inputData).setConstraints(constraints).build();
        WorkManager.getInstance(App.getInstance()).enqueueUniqueWork(REQUEST_ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }

    public void addToHistory(String url) {
        if (mHistory == null) {
            mHistory = new Stack<>();
        }
        mHistory.push(url);
    }

    public String getFromHistory(String mLastLoadedPage) {
        String last;
        while (mHistory != null && mHistory.size() > 0) {
            last = mHistory.pop();
            if (!last.equals(mLastLoadedPage)) {
                return last;
            }
        }
        return null;
    }

    public void viewTopic(String s) {
        Intent intent = new Intent(App.getInstance(), TopicActivity.class);
        intent.putExtra(TopicActivity.TOPIC_URL, s);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(intent);
    }

    public void makeSearch(String query) {
        Log.d("surprise", "BrowserViewModel makeSearch: search " + query);
        // запущу рабочего, который выполнит поиск
        // передам запрос, который нужно загрузить
        Data inputData = new Data.Builder()
                .putString(SEARCH_STRING, query)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest loginWork = new OneTimeWorkRequest.Builder(SearchWorker.class)
                .addTag(SEARCH_ACTION)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(App.getInstance())
                .enqueueUniqueWork(SEARCH_ACTION, ExistingWorkPolicy.REPLACE, loginWork);
    }

    public void openSearchSettingsWindow() {
        Intent intent = new Intent(App.getInstance(), SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(intent);
    }

    public ArrayList<String> getSearchAutocomplete() {
        String content = MyFileReader.getSearchAutocomplete();
        return XMLHandler.getSearchAutocomplete(content);
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
}
