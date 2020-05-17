package net.veldor.rutrackermobile.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.utils.Preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SettingsActivity extends FragmentActivity {
    public static final String KEY_HIDE_NO_SEED = "hide_no_seed";


    private static final HashMap<String, String> SORT_BY = new HashMap<String, String>() {{
        put("1", "дате регистрации");
        put("2", "названию темы");
        put("4", "количеству скачиваний");
        put("10", "количеству сидов");
        put("11", "количеству личей");
        put("7", "размеру");
    }};


    public static final String KEY_SORT_BY = "sort_by";
    public static final String KEY_LOW_TO_HIGH = "low to high";
    private static final int DOWNLOAD_FOLDER_SELECT_REQUEST_CODE = 1;
    private static final String KEY_CHANGE_DOWNLOAD_FOLDER = "change download folder";
    public static final String KEY_OPEN_TORRENT_IMMEDIATELY = "open torrent immediately";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            PreferenceFragment preferenceFragment = new PreferenceFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.pref_container, preferenceFragment);
            ft.commit();
        }

    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {
        private androidx.preference.SwitchPreference mHideNoSeed;
        private DropDownPreference mSortBy;
        private SwitchPreference mLowToHigh;
        private Preference mFolderChooser;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(getActivity());
                setPreferenceScreen(rootScreen);

                boolean hideEmpty = Preferences.getInstance().getHideNoSeed();
                mHideNoSeed = new SwitchPreference(activity);
                mHideNoSeed.setKey(SettingsActivity.KEY_HIDE_NO_SEED);
                mHideNoSeed.setTitle(R.string.hide_no_seed_message);
                mHideNoSeed.setChecked(hideEmpty);
                rootScreen.addPreference(mHideNoSeed);

                mSortBy = new DropDownPreference(activity);

                Set<String> keys = SORT_BY.keySet();
                Collection<String> values = SORT_BY.values();

                mSortBy.setEntries(values.toArray(new String[0]));
                mSortBy.setEntryValues(keys.toArray(new String[0]));
                mSortBy.setValueIndex(1);
                mSortBy.setKey(KEY_SORT_BY);
                mSortBy.setTitle(R.string.sort_by_message);
                mSortBy.setSummary("Результаты сортирутся по " + SORT_BY.get(Preferences.getInstance().mSharedPreferences.getString(KEY_SORT_BY, "10")));
                rootScreen.addPreference(mSortBy);

                mSortBy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        // обновлю информацию о выбранном способе сортировки
                        String sortBy = newValue.toString();
                        mSortBy.setSummary("Результаты сортируются по " + SORT_BY.get(sortBy));
                        return true;
                    }
                });

                boolean lowToHigh = Preferences.getInstance().mSharedPreferences.getBoolean(SettingsActivity.KEY_HIDE_NO_SEED, true);
                mLowToHigh = new SwitchPreference(activity);
                mLowToHigh.setKey(SettingsActivity.KEY_LOW_TO_HIGH);
                mLowToHigh.setTitle(R.string.from_low_to_high_message);
                mLowToHigh.setChecked(lowToHigh);
                rootScreen.addPreference(mLowToHigh);

                boolean openTorrentImmediately = Preferences.getInstance().mSharedPreferences.getBoolean(SettingsActivity.KEY_OPEN_TORRENT_IMMEDIATELY, false);
                SwitchPreference openTorrent = new SwitchPreference(activity);
                openTorrent.setKey(SettingsActivity.KEY_OPEN_TORRENT_IMMEDIATELY);
                openTorrent.setTitle("Открывать торрент после скачивания");
                openTorrent.setSummary("После скачивания торрента, он будет незамедлительно открыт в поддерживаемом приложении");
                openTorrent.setChecked(openTorrentImmediately);
                rootScreen.addPreference(openTorrent);

                // выбор папки для загрузки торрент-файлов
                mFolderChooser = new Preference(activity);

                mFolderChooser.setSummary(Preferences.getInstance().getDownloadFolder().getUri().toString());
                mFolderChooser.setKey(SettingsActivity.KEY_CHANGE_DOWNLOAD_FOLDER);
                mFolderChooser.setTitle(getString(R.string.download_folder_pref_label));
                rootScreen.addPreference(mFolderChooser);
                setHasOptionsMenu(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            mHideNoSeed.setChecked(Preferences.getInstance().mSharedPreferences.getBoolean(SettingsActivity.KEY_HIDE_NO_SEED, true));
            mLowToHigh.setChecked(Preferences.getInstance().mSharedPreferences.getBoolean(SettingsActivity.KEY_LOW_TO_HIGH, false));
            mFolderChooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    changeDownloadsFolder();
                    return false;
                }
            });
        }

        private void changeDownloadsFolder() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                        |Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        |Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        |Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                );
                startActivityForResult(intent, DOWNLOAD_FOLDER_SELECT_REQUEST_CODE);
            }
        /*else {
            Intent intent = new Intent(this, FolderPicker.class);
            startActivityForResult(intent, READ_REQUEST_CODE);
        }*/
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if (requestCode == DOWNLOAD_FOLDER_SELECT_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri treeUri = data.getData();
                        if (treeUri != null) {
                            // проверю наличие файла
                            DocumentFile dl = DocumentFile.fromTreeUri(App.getInstance(), treeUri);
                            if(dl != null && dl.isDirectory()){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    App.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    App.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                                mFolderChooser.setSummary(treeUri.toString());
                                Preferences.getInstance().saveDownloadLocation(treeUri);
                            }
                        }


                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
