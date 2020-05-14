package net.veldor.rutrackermobile.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.documentfile.provider.DocumentFile;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.ui.SettingsActivity;

import java.io.File;

public class Preferences {
    private static final File DEFAULT_DOWNLOAD_FOLDER_LOCATION = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final String PREFERENCE_DOWNLOAD_LOCATION = "download_location";
    public static final String TOR_FILES_LOCATION = "torfiles";
    private static Preferences instance;
    public final SharedPreferences mSharedPreferences;

    private static final String EXTERNAL_VPN = "external vpn";
    private static final String AUTH_COOKIE = "auth_cookie";

    public static Preferences getInstance(){
        if(instance == null){
            instance = new Preferences();
        }
        return instance;
    }

    private Preferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    public boolean isExternalVpn() {
        return (mSharedPreferences.getBoolean(EXTERNAL_VPN, false));
    }

    public void switchExternalVpnUse() {
        mSharedPreferences.edit().putBoolean(EXTERNAL_VPN, !isExternalVpn()).apply();
    }

    public boolean isUserLogged() {
            return mSharedPreferences.getString(AUTH_COOKIE, null) != null;
    }

    public void saveLoginCookie(String cookie) {
        mSharedPreferences.edit().putString(AUTH_COOKIE, cookie).apply();
    }

    public String getAuthCookie() {
        return mSharedPreferences.getString(AUTH_COOKIE, null);
    }

    public boolean getHideNoSeed() {
        return mSharedPreferences.getBoolean(SettingsActivity.KEY_HIDE_NO_SEED, true);
    }

    public DocumentFile getDownloadFolder() {
        // возвращу папку для закачек
        String download_location = mSharedPreferences.getString(PREFERENCE_DOWNLOAD_LOCATION, null);
        if(download_location != null){
            DocumentFile dl = DocumentFile.fromTreeUri(App.getInstance(), Uri.parse(download_location));
            if(dl != null){
                if(dl.isDirectory()){
                    return dl;
                }
            }
        }
        // верну путь к папке загрузок
        return DocumentFile.fromFile(DEFAULT_DOWNLOAD_FOLDER_LOCATION);
    }

    public void saveDownloadLocation(Uri uri) {
        mSharedPreferences.edit().putString(PREFERENCE_DOWNLOAD_LOCATION, uri.toString()).apply();
    }

    public boolean isTorrentOpen() {
        return mSharedPreferences.getBoolean(SettingsActivity.KEY_OPEN_TORRENT_IMMEDIATELY, false);
    }
}
