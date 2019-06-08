package cho.nico.com.test;

import android.view.ViewGroup;

public interface ResizeCameraViewCallback {
    void resizeView(int w, int h);

    void playVideo();

    void setVideoPath(String path);
}
