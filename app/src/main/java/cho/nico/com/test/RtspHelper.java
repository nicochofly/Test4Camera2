package cho.nico.com.test;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayCallback;
import org.videolan.libvlc.MediaPlayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


/**
 * Created by liwentian on 2017/10/12.
 */

public class RtspHelper {

    private MediaPlayer mMediaPlayer;

    private LibVLC mVlc;

    private static RtspHelper sInstance = new RtspHelper();

    private ByteBuffer mByteBuffer;

    public static RtspHelper getInstance(Context c) {
        context = c;
        return sInstance;
    }

    private static Context context;

    public interface RtspCallback {
        void onPreviewFrame(ByteBuffer buffer, int width, int height);
        void onstop();
    }

    private RtspHelper() {

    }

    public void createPlayer(String url, final int width, final int height, final RtspCallback callback) {
        releasePlayer();

        mByteBuffer = ByteBuffer.allocateDirect(width * height * 4)
                .order(ByteOrder.nativeOrder());

        try {
            ArrayList<String> options = new ArrayList<String>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mVlc = new LibVLC(context, options);

            // Create media player
            mMediaPlayer = new MediaPlayer(mVlc);

            mMediaPlayer.setVideoFormat("RGBA", width, height, width * 4);
            mMediaPlayer.setVideoCallback(mByteBuffer, new MediaPlayCallback() {
                @Override
                public void onDisplay(final ByteBuffer byteBuffer) {
                    callback.onPreviewFrame(byteBuffer, width, height);
                }
            });

            Media m = new Media(mVlc, url);
            int cache = 1500;
            m.addOption(":network-caching=" + cache);
            m.addOption(":file-caching=" + cache);
            m.addOption(":live-cacheing=" + cache);
            m.addOption(":sout-mux-caching=" + cache);
            m.addOption(":codec=mediacodec,iomx,all");
            mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
                @Override
                public void onEvent(MediaPlayer.Event event) {
                    if (event.type == MediaPlayer.Event.Stopped) {

                        Log.e("rtsp","MediaPlayer.Event.Stopped");
                        callback.onstop();
                    }
                }
            });

            mMediaPlayer.setVolume(0);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
            Log.e("rtsp","play");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void releasePlayer() {
        if (mVlc == null) {
            return;
        }

        mMediaPlayer.setVideoCallback(null, null);
//        mMediaPlayer.stop();

        mVlc.release();
        mVlc = null;
    }


    private void showMediaView(SurfaceView mSurfaceView) {
        mMediaPlayer.getVLCVout().setVideoView(mSurfaceView);
//        mMediaPlayer.getVLCVout().setVideoSurface(mSurfaceView.getHolder().getSurface(), mSurfaceView.getHolder());
        //将SurfaceView贴到MediaPlayer上
        mMediaPlayer.getVLCVout().attachViews();
        //设置播放窗口的尺寸
        mMediaPlayer.getVLCVout().setWindowSize(mSurfaceView.getWidth(), mSurfaceView.getHeight());
    }
}
