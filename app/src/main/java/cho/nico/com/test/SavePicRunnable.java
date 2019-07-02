package cho.nico.com.test;

import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cho.nico.com.test.callback.CameraResultCallback;

/**
 * 录制视频，保存图片runnable
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SavePicRunnable implements Runnable {

    /**
     * The JPEG image
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    private CameraResultCallback cameraResultCallback;

    private int type;

    public SavePicRunnable(Image image, File file, int type, CameraResultCallback callback) {
        mImage = image;
        mFile = file;
        this.cameraResultCallback = callback;
        this.type = type;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cameraResultCallback.getMediaData(type, mFile.getAbsolutePath());
        }
    }
}
