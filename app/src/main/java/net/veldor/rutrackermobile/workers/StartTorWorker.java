package net.veldor.rutrackermobile.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.utils.Preferences;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.client.protocol.HttpClientContext;

public class StartTorWorker extends Worker {

    public static final String START_TOR = "start_tor";

    public StartTorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // попробую стартовать TOR
        while (App.sTorStartTry < 4 && !isStopped()) {
            // есть три попытки, если все три неудачны- верну ошибку
            try {
                Log.d("surprise", "StartTorWorker doWork: start tor, try # " + App.sTorStartTry);
                startTor();
                Log.d("surprise", "StartTorWorker doWork: tor success start");
                // обнулю счётчик попыток
                App.sTorStartTry = 0;
                return Result.success();
            } catch (Exception e) {
                // попытка неудачна, плюсую счётчик попыток
                App.sTorStartTry++;
                Log.d("surprise", "StartTorWorker doWork: tor wrong start try");
            }
        }
        Log.d("surprise", "StartTorWorker doWork: i can't load TOR");
        if(isStopped()){
            Log.d("surprise", "StartTorWorker doWork: i stopped");
            return Result.retry();
        }
        return Result.failure();
    }

    public static boolean startTor() throws Exception {
        AndroidOnionProxyManager tor;
        if (App.getInstance().mTorManager.getValue() != null) {
            tor = App.getInstance().mTorManager.getValue();
        } else {
            tor = new AndroidOnionProxyManager(App.getInstance(), Preferences.TOR_FILES_LOCATION);
        }
        // добавлю полученный TOR для отслеживания его состояния
        App.getInstance().mTorManager.postValue(tor);
        // просто создание объекта, не запуск
        // тут- время, которое отводится на попытку запуска
        int totalSecondsPerTorStartup = (int) TimeUnit.MINUTES.toSeconds(3);
        // количество попыток запуска
        int totalTriesPerTorStartup = 1;
            boolean ok = tor.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
            if (!ok) {
                // TOR не запущен, оповещу о том, что запуск не удался
                throw new Exception();
            }
            if (tor.isRunning()) {
                //Returns the socks port on the IPv4 localhost address that the Tor OP is listening on
                int port = tor.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                HttpClientContext context = HttpClientContext.create();
                context.setAttribute("socks.address", socksaddr);
                App.getInstance().mTorManager.postValue(tor);
                return true;
            } else {
                // TOR не запущен, оповещу о том, что запуск не удался
                throw new Exception();
            }
    }
}
