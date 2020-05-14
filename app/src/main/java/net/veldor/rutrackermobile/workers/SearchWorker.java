package net.veldor.rutrackermobile.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.http.TorWebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class SearchWorker extends Worker {
    public SearchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String SEARCH_STRING = "search string";
    public static final String SEARCH_ACTION = "search_action";

    @NonNull
    @Override
    public Result doWork() {
        Log.d("surprise", "SearchWorker doWork: start search");
        Data data = getInputData();
        String searchString = data.getString(SEARCH_STRING);
        TorWebClient webClient = new TorWebClient();
        InputStream response = webClient.search(searchString);
        if (response != null) {
            // сохраню ответ как строку
            InputStreamReader isReader = null;
            try {
                isReader = new InputStreamReader(response, "CP1251");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //Creating a BufferedReader object
            BufferedReader reader = new BufferedReader(isReader);
            StringBuilder sb = new StringBuilder();
            String str;
            while (true) {
                try {
                    if ((str = reader.readLine()) == null) break;
                    sb.append(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            App.getInstance().mLiveRequest.postValue(sb.toString());
            return Result.success();
        }
        return Result.failure();
    }
}
