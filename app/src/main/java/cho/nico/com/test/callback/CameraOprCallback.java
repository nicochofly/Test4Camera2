package cho.nico.com.test.callback;

public interface CameraOprCallback {
    //0 正常拍照 2 翻拍
    void capturePic(int type);

    void recordVideo();

    void stopRecord(boolean save);

}
