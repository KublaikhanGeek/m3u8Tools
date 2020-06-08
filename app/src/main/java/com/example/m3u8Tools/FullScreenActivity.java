package com.example.m3u8Tools;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import jaygoo.library.m3u8downloader.server.EncryptM3U8Server;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2017/11/21
 * 描    述:
 * ================================================
 */
public class FullScreenActivity extends Activity{

    private StandardGSYVideoPlayer videoPlayer;
    private EncryptM3U8Server m3u8Server = new EncryptM3U8Server();
    private OrientationUtils orientationUtils;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        // set videoplayer
        VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp");
        VideoOptionModel videoOptionModel2 =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_extensions", "ALL");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        list.add(videoOptionModel2);
        GSYVideoManager.instance().setOptionModelList(list);
        videoPlayer = (StandardGSYVideoPlayer)findViewById(R.id.videoView);

        String url = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            url = bundle.getString("M3U8_URL");
        }
        m3u8Server.execute();
        videoPlayer.getCurrentPlayer().setUp(m3u8Server.createLocalHttpUrl(url),false,"");
        //videoPlayer.startWindowFullscreen(this,false,false);
        //设置返回键
        videoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);
        videoPlayer.setAutoFullWithSize(true);
        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //设置旋转
        orientationUtils = new OrientationUtils(this, videoPlayer);
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });
        videoPlayer.getCurrentPlayer().startPlayLogic();

    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m3u8Server.finish();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        videoPlayer.setVideoAllCallBack(null);
        super.onBackPressed();
    }
}
