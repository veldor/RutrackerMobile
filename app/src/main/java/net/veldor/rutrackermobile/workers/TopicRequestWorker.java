package net.veldor.rutrackermobile.workers;

import android.content.Context;

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

public class TopicRequestWorker extends Worker {

    public static final String REQUESTED_URL = "requested_url";
    public static final String REQUEST_ACTION = "request_action";

    public TopicRequestWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String url = data.getString(REQUESTED_URL);
        // запрошу страницу
        InputStream response = (new TorWebClient()).requestPage(url);
        if(response != null){
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
            while(true){
                try {
                    if ((str = reader.readLine()) == null) break;
                    sb.append(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            App.getInstance().mLiveTopicRequest.postValue(sb.toString());
        }
        return Result.success();
    }
}
