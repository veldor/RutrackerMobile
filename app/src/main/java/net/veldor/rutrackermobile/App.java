package net.veldor.rutrackermobile;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;

import net.veldor.rutrackermobile.utils.Preferences;
import net.veldor.rutrackermobile.workers.StartTorWorker;

import static net.veldor.rutrackermobile.workers.StartTorWorker.START_TOR;

public class App extends Application {
    public static final String RUTRACKER_MAIN_PAGE = "https://rutracker.org/forum/index.php";
    public static final String RUTRACKER_BASE = "https://rutracker.org/forum/";
    public static int sTorStartTry;
    private static App instance;
    // место для хранения TOR клиента
    public final MutableLiveData<AndroidOnionProxyManager> mTorManager = new MutableLiveData<>();
    // хранилище статуса HTTP запроса
    public final MutableLiveData<String> RequestStatus = new MutableLiveData<>();
    // хранилище ответа на HTTP запрос
    public final MutableLiveData<String> mLiveRequest = new MutableLiveData<>();
    public final MutableLiveData<String> mLiveTopicRequest = new MutableLiveData<>();
    public final MutableLiveData<String> mPageTitle = new MutableLiveData<>();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        // запущу TOR
        startTor();
    }

    void startTor() {
        // если используется внешний VPN- TOR не нужен
        if(!Preferences.getInstance().isExternalVpn()){
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            // запускаю tor
            OneTimeWorkRequest startTorWork = new OneTimeWorkRequest.Builder(StartTorWorker.class).addTag(START_TOR).setConstraints(constraints).build();
            WorkManager.getInstance(this).enqueueUniqueWork(START_TOR, ExistingWorkPolicy.REPLACE, startTorWork);
        }
    }
}
