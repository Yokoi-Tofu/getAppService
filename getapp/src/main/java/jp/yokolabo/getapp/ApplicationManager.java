package jp.yokolabo.getapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yokoi on 2015/11/14.
 */
public class ApplicationManager{

    String topApp;                                                  //現在Topに立ち上がっているアプリ名
    ArrayList<String> runningAppList;                               //現在起動しているアプリの一覧を格納するArrayList
    List<ActivityManager.RunningAppProcessInfo> oldRunningAppList;  //起動しているアプリの履歴を格納するList
    Context context;

    public ApplicationManager(Context context){
        topApp = "";
        runningAppList = new ArrayList();
        this.context = context;
    }

    public String getTopApp(String topApp) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        // 起動中のアプリ情報を取得
        List<ActivityManager.RunningAppProcessInfo> runningApp = activityManager.getRunningAppProcesses();
        PackageManager packageManager = context.getPackageManager();

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
                System.out.println(topApp + "がトップです");

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
                            System.out.println(topApp + "が閉じられました。");
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
                        System.out.println(runningAppList.get(cnt) + "が完全に終了しました。");
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
