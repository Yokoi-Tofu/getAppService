package jp.yokolabo.getappservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
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
    String oldApp;
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
        oldApp = "";
        appTimer = null;
        handler = new Handler();
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
                        topApp = am.getTopApp(oldApp);
                        if(!topApp.equals(oldApp) && topApp.equals("Chrome")){
                            showText("グーグルクロームが立ち上がりました。");
                        }
                        oldApp = topApp;
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
}
