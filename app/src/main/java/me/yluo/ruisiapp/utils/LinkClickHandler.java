package me.yluo.ruisiapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.activity.BaseActivity;
import me.yluo.ruisiapp.activity.LoginActivity;
import me.yluo.ruisiapp.activity.NewPostActivity;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.activity.PostsActivity;
import me.yluo.ruisiapp.activity.UserDetailActivity;
import me.yluo.ruisiapp.activity.ViewImgActivity;
import me.yluo.ruisiapp.downloadfile.DownloadService;

/**
 * Created by free2 on 16-4-12.
 * 处理WebView和 链接点击
 * <p>
 */
public class LinkClickHandler {
    public static final String VOTE_URL = "rsvote://";

    public static void handleClick(final Context context, String url) {
        Log.d("handle the link", url);
        //点击了图片
        if (url.contains("from=album")) {
            ViewImgActivity.open(context, url);
        } else if (url.contains("forum.php?mod=viewthread&tid=") || url.contains("forum.php?mod=redirect&goto=findpost")) { // 帖子
            PostActivity.open(context, url, null);
        } else if (url.contains("home.php?mod=space&uid=")) { // 用户
            String imageUrl = UrlUtils.getAvaterurlb(url);
            UserDetailActivity.open(context, "name", imageUrl, "");
        } else if (url.contains("forum.php?mod=post&action=newthread")) { //发帖链接
            context.startActivity(new Intent(context, NewPostActivity.class));
        } else if (url.contains("member.php?mod=logging&action=login")) {//登陆
            LoginActivity.open(context);
        } else if (url.contains("forum.php?mod=forumdisplay&fid=")) {
            int fid = GetId.getFroumFid(url);
            PostsActivity.open(context, fid, "分区帖子");
        } else if (url.startsWith("forum.php?mod=post&action=reply")) { //回复
            if (context instanceof PostActivity) {
                PostActivity a = (PostActivity) context;
                a.showReplyKeyboard();
            }
        } else if (url.contains("forum.php?mod=attachment")) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 没有权限
                new AlertDialog.Builder(context).
                        setTitle("权限错误").
                        setMessage("没有写入内部存储的权限,无法下载").
                        setPositiveButton("授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((BaseActivity) context,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                //请求结果在onRequestPermissionsResult返回
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(true)
                        .create()
                        .show();
            } else {
                // 启动下载服务
                final String finalUrl = url;
                new AlertDialog.Builder(context).
                        setTitle("下载附件").
                        setMessage("你要开始下载此附件吗？").
                        setPositiveButton("下载", (dialog, which) -> {
                            Intent intent = new Intent(context, DownloadService.class);
                            intent.putExtra("download_url", finalUrl);
                            context.startService(intent);
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(true)
                        .create()
                        .show();
            }
        } else if (url.startsWith(VOTE_URL)) {
            if (context instanceof PostActivity) {
                PostActivity a = (PostActivity) context;
                a.showVoteView();
            }


        } else {
            if (!url.startsWith("http")) {
                url = App.getBaseUrl() + url;
            }
            IntentUtils.openBroswer(context, url);
        }
    }
}
