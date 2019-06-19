package cho.nico.com.test;

public interface CameraOprCallback {
    void capturePic();

    void recordVideo();

    void stopRecord(boolean save);

}
