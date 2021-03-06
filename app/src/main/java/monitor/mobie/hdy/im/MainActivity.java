package monitor.mobie.hdy.im;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Set;

import monitor.mobie.hdy.im.service.MonitorService;
import monitor.mobie.hdy.im.service.NotificationCollectorService;
import monitor.mobie.hdy.im.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private Intent serviceIntent;
    private MonitorService monitorService;
    private boolean flag;
    private Button button;
    private FloatingActionButton fab;
    private ToastUtils toastUtils;
    private SharedPreferences data;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        if (serviceIntent == null) {
            serviceIntent = new Intent(MainActivity.this, MonitorService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(serviceIntent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(serviceIntent);
    }


    private void init() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginDisplay();
            }
        });
        toastUtils = new ToastUtils(MainActivity.this);
        data = getSharedPreferences("data", Context.MODE_MULTI_PROCESS);
        edit = data.edit();
        if (!isNotificationListenerServiceEnabled(this)) {
            Toast.makeText(this, "请先勾选手机监听器的读取通知栏权限!", Toast.LENGTH_LONG).show();
            return;
        }
        this.findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Uri uri = Uri.parse("http://sc.ftqq.com/3.version");
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNotificationListenerServiceEnabled(this)) {
            openNotificationAccess();
            toggleNotificationListenerService();
            Toast.makeText(this, "请先勾选手机监听器的读取通知栏权限!", Toast.LENGTH_LONG).show();
            return;
        }
    }


    private void openLoginDisplay() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.login_layout, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        final EditText SCKEY_input = (EditText) view.findViewById(R.id.sckey_input);
        SCKEY_input.setText(data.getString("SCKEY", ""));
        Button button = (Button) view.findViewById(R.id.login_button);
        serviceIntent = new Intent(MainActivity.this, MonitorService.class);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(serviceIntent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里进行提交登录
                edit.putString("SCKEY", SCKEY_input.getText().toString()).commit();
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationCollectorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, NotificationCollectorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    private static boolean isNotificationListenerServiceEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private void openNotificationAccess() {
        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

}
