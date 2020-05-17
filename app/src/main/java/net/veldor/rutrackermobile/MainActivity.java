package net.veldor.rutrackermobile;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import net.veldor.rutrackermobile.ui.BrowserActivity;
import net.veldor.rutrackermobile.utils.Preferences;
import net.veldor.rutrackermobile.view_models.MainViewModel;
import net.veldor.rutrackermobile.workers.StartTorWorker;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_WRITE_READ = 22;
    private static final int DOWNLOAD_FOLDER_SELECT_REQUEST_CODE = 23;
    private MainViewModel mMyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // запрошу разрешения
        if (!mMyViewModel.permissionGranted(this)) {
            // показываю диалог с требованием предоставить разрешения
            showPermissionDialog();
        } else {
            // предложу выбрать папку для сохранения файлов
            if(Preferences.getInstance().getDownloadDir() == null){
                showSelectDownloadFolderDialog();
            }
            else{
                handleStart();
            }
        }

        // проверю, не запущено ли приложение с помощью интента, если да- перейду на присланную страницу
        if(getIntent().getData()!=null){
            Uri data = getIntent().getData();
            App.getInstance().externalUrl = data.toString();
        }
    }

    private void handleStart() {
        setupObservers();
    }

    private void setupObservers() {
        if (!Preferences.getInstance().isExternalVpn()) {
            // получу данные о работе
            LiveData<List<WorkInfo>> workInfoData = WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(StartTorWorker.START_TOR);
            workInfoData.observe(this, new Observer<List<WorkInfo>>() {
                @Override
                public void onChanged(List<WorkInfo> workInfos) {
                    if (workInfos != null && workInfos.size() > 0) {
                        // переберу статусы
                        WorkInfo data = workInfos.get(0);
                        switch (data.getState()) {
                            case FAILED:
                                showTorNotWorkDialog();
                                break;
                            case SUCCEEDED:
                                torLoaded();
                        }
                    }
                }
            });
        } else {
            torLoaded();
        }
    }

    public void torLoaded() {
        // запускаю активити для просмотра
        startActivity(new Intent(this, BrowserActivity.class));
        finish();
    }

    private void showPermissionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Необходимо предоставить разрешения")
                .setMessage("Для загрузки торрент-файлов необходимо предоставить доступ к памяти устройства")
                .setCancelable(false)
                .setPositiveButton("Предоставить разрешение", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_READ);
                    }
                })
                .setNegativeButton("Нет, закрыть приложение", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dialogBuilder.create().show();
    }


    private void showTorNotWorkDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.tor_cant_load_message))
                .setMessage(getString(R.string.tor_not_start_body))
                .setPositiveButton(getString(R.string.try_again_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        App.getInstance().startTor();

                    }
                })
                .setNegativeButton(getString(R.string.try_later_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNeutralButton(getString(R.string.use_external_proxy_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMyViewModel.handleUseExternalVpn(MainActivity.this);
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_READ && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog();
            } else {
                // предложу выбрать папку для сохранения файлов
                if(Preferences.getInstance().getDownloadDir() == null){
                    showSelectDownloadFolderDialog();
                }
                else{
                    handleStart();
                }
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showSelectDownloadFolderDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Выберите папку для сохранения")
                .setMessage("Выберите папку, в которой будут храниться скачанные торренты")
                .setCancelable(false)
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                })
                .setNegativeButton("Нет, закрыть приложение", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dialogBuilder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                            Preferences.getInstance().setDownloadDir(treeUri);
                            handleStart();
                            return;
                        }
                    }
                }
            }
            showSelectDownloadFolderDialog();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

