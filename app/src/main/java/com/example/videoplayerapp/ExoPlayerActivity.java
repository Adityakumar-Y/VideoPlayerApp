package com.example.videoplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class ExoPlayerActivity extends AppCompatActivity implements Player.EventListener {

    private SimpleExoPlayer player;
    private ProgressBar progressBar;
    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private String youtubeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        initViews();
    }

    private void initViews() {
        playerView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializePlayer(String downloadUrl) {
        if(player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this);
        }
        player.addListener(this);
        Uri uri = Uri.parse(downloadUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
        playerView.setPlayer(player);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "test");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(TextUtils.isEmpty(youtubeUrl)) {
            extractYoutubeUrl();
        }else{
            initializePlayer(youtubeUrl);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl() {
        progressBar.setVisibility(View.VISIBLE);
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = 22;
                    youtubeUrl = ytFiles.get(itag).getUrl();
                    initializePlayer(youtubeUrl);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }.extract(getString(R.string.media_url), true, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == SimpleExoPlayer.STATE_BUFFERING || playbackState == SimpleExoPlayer.STATE_IDLE){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }
}
