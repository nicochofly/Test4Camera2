package cho.nico.com.test;

import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    SavePicRunnable(Image image, File file, CameraResultCallback callback) {
        mImage = image;
        mFile = file;
        this.cameraResultCallback = callback;
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
            cameraResultCallback.getMediaData(0, mFile.getAbsolutePath());
        }
    }
}
