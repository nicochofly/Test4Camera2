package cho.nico.com.test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class CaptureResultActivity extends Activity implements View.OnClickListener {


    /**
     * 媒体类型
     * 0 图片
     * 1 视频
     */
    private int mediaType = 0;

    private String mediaPath;

    private VideoView videoView;

    private ImageView imageView;

    private ImageView backIv, submitIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captureresult);

        mediaType = getIntent().getIntExtra("type", 0);
        mediaPath = getIntent().getStringExtra("path");

        videoView = findViewById(R.id.takepic_vv);
        imageView = findViewById(R.id.takepic_iv);
        backIv = findViewById(R.id.back_iv);
        submitIv = findViewById(R.id.submit_iv);
        backIv.setOnClickListener(this);
        submitIv.setOnClickListener(this);

        if (TextUtils.isEmpty(mediaPath)) {
            finish();
        } else {

            switch (mediaType) {
                case 0:
                    videoView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    break;
                case 1:
                    videoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);

                    break;
            }


        }

    }

    private void loadView(int mediaType) {

        switch (mediaType) {
            case 0:

                Glide.with(this)
                        .load(mediaPath)
                        .placeholder(R.mipmap.default_we).error(R.mipmap.default_we).centerCrop()
                        .into(imageView);
                break;
            case 1:

                playVideo(mediaPath);

                break;
        }

    }


    private MediaPlayer mediaPlayer;


    public void playVideo(final String url) {

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    } else {
                        mediaPlayer.reset();
                    }
                    mediaPlayer.setDataSource(url);

                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mediaPlayer.getVideoWidth(), mediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.setSurface(videoView.getHolder().getSurface());
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopVideo() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            FrameLayout.LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * videoView.getWidth());
            videoViewParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            videoView.setLayoutParams(videoViewParam);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        loadView(mediaType);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pageOnPause();
    }

    private void pageOnPause() {
        if (mediaType == 1) {
            stopVideo();
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.back_iv:

                finish();

                break;
            case R.id.submit_iv:


                break;
        }
    }
}
