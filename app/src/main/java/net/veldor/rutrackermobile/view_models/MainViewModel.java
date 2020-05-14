package net.veldor.rutrackermobile.view_models;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import net.veldor.rutrackermobile.MainActivity;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.utils.Preferences;

public class MainViewModel extends ViewModel {

    public boolean permissionGranted(MainActivity mainActivity) {
        int writeResult;
        int readResult;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            writeResult = mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            readResult = mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            writeResult = PermissionChecker.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            readResult = PermissionChecker.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return writeResult == PackageManager.PERMISSION_GRANTED && readResult == PackageManager.PERMISSION_GRANTED;
    }

    public void handleUseExternalVpn(final MainActivity mainActivity) {
        // покажу диалог с объяснением последствий включения VPN
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
        dialogBuilder
                .setTitle("Использование внешнего VPN")
                .setMessage("Оповестить об использовании внешнего VPN. В этом случае внутренний клиент TOR будет отключен и траффик приложения не будет обрабатываться. В этом случае вся ответственность за получение контента ложится на внешний VPN. Если вы будете получать сообщения об ошибках загрузки- значит, он работает неправильно. Сделано для версий Android ниже 6.0, где могут быть проблемы с доступом, но может быть использовано по желанию на ваш страх и риск.")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Preferences.getInstance().switchExternalVpnUse();
                        mainActivity.torLoaded();
                    }
                });
        dialogBuilder.create().show();
    }
}
