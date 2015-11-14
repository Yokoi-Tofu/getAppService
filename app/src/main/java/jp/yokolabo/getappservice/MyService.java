package jp.yokolabo.getappservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.yokolabo.getapp.ApplicationManager;

/**
 * Created by Yokoi on 2015/11/14.
 */
public class MyService extends Service{

    Notification n;                                                 //通知を表示するオブジェクト
    private Timer appTimer;                                         //アプリの状況を取得する間隔を設定するオブジェクト
    Handler handler;
    String topApp;                                                  //現在Topに立ち上がっているアプリ名
    ArrayList<String> runningAppList;                               //現在起動しているアプリの一覧を格納するArrayList
    List<ActivityManager.RunningAppProcessInfo> oldRunningAppList;  //起動しているアプリの履歴を格納するList
    ApplicationManager am;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * テキストを表示する
     *
     * @param text 表示したいテキスト
     */
    private void showText(Context ctx, final String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    // ______________________________________________________________________________

    /**
     * テキストを表示する
     *
     * @param text テキスト
     */
    private void showText(final String text) {
        showText(this, text);
    }

    @Override   // onCreate:サービスが作成されたときに呼びされる(最初に1回だけ)
    public void onCreate() {
        this.showText("サービスが開始されました。");
        super.onCreate();
        //---------------------------------------------------------------------------
        //初期化処理
        topApp = "";
        appTimer = null;
        handler = new Handler();
        runningAppList = new ArrayList();
        am = new ApplicationManager(this);
    }

        @Override   // onStartCommand:
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // 通知バーを表示する
        showNotification(this);

        //タイマーの設定
        appTimer = new Timer(true);
        appTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //現在のApplicationの状態を取得
                        topApp = am.getTopApp(topApp);
                    }
                });
            }
        }, 1000, 750);

        //サービスの強制終了をさせなくする
        startForeground(1, n);
        // 戻り値でサービスが強制終了されたときの挙動が変わる
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //タイマーの終了
        if (appTimer != null) {
            appTimer.cancel();
            appTimer = null;
        }

        //ノーティフィケーションの削除
        this.stopNotification(this);
        //サービスの永続化の停止
        stopForeground(true);
        this.showText("サービスが終了されました。");
    }

    // ______________________________________________________________________________
    // 通知バーを消す
    private void stopNotification(final Context ctx) {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(R.layout.activity_main);
    }

    // ______________________________________________________________________________
    // 通知バーを出す
    private void showNotification(final Context ctx) {

        // 通知バーの内容を決める
        n = new NotificationCompat.Builder(ctx)
                .setTicker("サービスが起動しました。")
                .setWhen(System.currentTimeMillis())    // 時間
                .setContentTitle("サービス起動中")
                .build();
    }

    public String getTopApp(String topApp) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // 起動中のアプリ情報を取得
        List<ActivityManager.RunningAppProcessInfo> runningApp = activityManager.getRunningAppProcesses();
        PackageManager packageManager = getPackageManager();

        //現在一番上に立ち上がっているアプリを取得
        String topAppNow = "";
        try {
            topAppNow = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(runningApp.get(0).processName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //アプリの生存確認のフラグ
        boolean flag = false;

        //立ち上がっているアプリがあるか確認
        if (runningApp != null) {

            if (!topAppNow.equals(topApp)) {
                showText(topAppNow + "が起動しました。");

                //現在ランニングアプリの中にすでにいま立ち上がったアプリがあるかチェック
                for (int cnt = 0; cnt < runningAppList.size(); cnt++) {
                    if (runningAppList.get(cnt).equals(topAppNow)) {
                        //今立ち上がったアプリが既に立ち上がっていた場合(初回起動時の条件と同じにする)
                        flag = true;

                    }
                }
                if (!flag) {
                    //現在立ち上がっているアプリをランニングアプリに追加する
                    runningAppList.add(topAppNow);
                    //アプリ初回起動時
                }


                //一番上に立っていたアプリがどこにいるのか探す
                for (ActivityManager.RunningAppProcessInfo app : runningApp) {
                    try {
                        // アプリ名をリストに追加
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(app.processName, 0);
                        //一番上に立ち上がっていたアプリが一番上以外の場所にいたとき
                        if ((packageManager.getApplicationLabel(appInfo)).equals(topApp)) {
                            //アプリが閉じられた時
                            this.showText(topApp + "が閉じられました。");
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        //e.printStackTrace();
                    }
                }
            }

            //立ち上がっているアプリの一覧が更新されているかチェック
            if (oldRunningAppList != null && runningApp.size() == oldRunningAppList.size()) {
            } else {
                //System.out.println("更新されました。");
                //更新されている場合削除されたプロセスがあるか確認
                for (int cnt = 0; cnt < runningAppList.size(); cnt++) {
                    flag = false;
                    for (ActivityManager.RunningAppProcessInfo app : runningApp) {
                        try {
                            if (packageManager.getApplicationLabel(packageManager.getApplicationInfo(app.processName, 0)).equals(runningAppList.get(cnt))) {
                                //System.out.println(runningAppList.get(cnt) + "は生きています。");
                                flag = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {

                        }
                    }
                    if (!flag) {
                        //キャッシュ削除時
                        this.showText(runningAppList.get(cnt) + "が完全に終了しました。");
                        runningAppList.remove(cnt);
                        cnt--;
                    }
                }
            }
        }
        //System.out.println(topAppNow + "がtopです。");
        //現在起動しているプロセスを保存
        oldRunningAppList = runningApp;
        //一番上にあるアプリを返す
        return topAppNow;
    }
}
