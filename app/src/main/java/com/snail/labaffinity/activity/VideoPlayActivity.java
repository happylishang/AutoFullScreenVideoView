package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import com.danikula.videocache.HttpProxyCacheServer;
import com.snail.labaffinity.R;
import com.snail.labaffinity.app.LabApplication;
import com.snail.resizevideo.view.AutoSizeVideoView;
import com.snail.resizevideo.view.IFullScreenVideoContainer;
import com.snail.resizevideo.view.VideoPlayControlView;

import butterknife.BindView;

public class VideoPlayActivity extends BaseActivity implements IFullScreenVideoContainer {

    @BindView(R.id.vedio)
    AutoSizeVideoView mVideo;

    @BindView(R.id.vedio1)
    AutoSizeVideoView mVideo1;

    @BindView(R.id.conrol)
    VideoPlayControlView myControler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HttpProxyCacheServer proxy = LabApplication.getProxy(this);
        String proxyUrl = proxy.getProxyUrl("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        setContentView(R.layout.activity_videoview);
//        mVideo.setUrl((proxyUrl));
//        https://yanxuan.nosdn.127.net/73b567051ddc32c7a59cfc36136ef910.mp4
        mVideo.setUrl(("http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4"));
//http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4
//      http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4
        mVideo.registerOutContainer(this);
        mVideo.registerOriginContainer((ViewGroup) mVideo.getParent());
        mVideo.setmOnPlayStateChangeListener(myControler);
        mVideo.start();
        myControler.bindVideoView(mVideo);
    }

    @Override
    public ViewGroup getOutContainer() {
        return (ViewGroup) findViewById(R.id.outcontain);
    }


}
