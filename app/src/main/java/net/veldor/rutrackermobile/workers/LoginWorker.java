package net.veldor.rutrackermobile.workers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.http.TorWebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginWorker extends Worker {

    public static final String USER_LOGIN = "user login";
    public static final String USER_PASSWORD = "user password";
    public static final String LOGIN_ACTION = "login";

    public LoginWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String login = data.getString(USER_LOGIN);
        String password = data.getString(USER_PASSWORD);
        Log.d("surprise", "LoginWorker doWork prepare work");
        try {
            // создам запрос на аутентификацию
            String url = "https://rutracker.org/forum/login.php?convert_post=1" + "&login_username=" + URLEncoder.encode(login, "windows-1251") + "&login_password=" + URLEncoder.encode(password, "windows-1251") + "&login=" + URLEncoder.encode("вход", "windows-1251");
            App.getInstance().RequestStatus.postValue(App.getInstance().getString(R.string.prepare_request_message));
            TorWebClient webClient = new TorWebClient();
            Uri request = Uri.parse(url);
            App.getInstance().RequestStatus.postValue(App.getInstance().getString(R.string.send_request_message));
            boolean result = webClient.login(request);
            if(result){
                return Result.success();
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("surprise", "LoginWorker doWork не могу преобразовать запрос");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.failure();
    }
}
