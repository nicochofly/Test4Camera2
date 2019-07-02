package cho.nico.com.test.callback;



public interface CameraResultCallback {

    //0 照片 1 视频 2翻拍
    void getMediaData(int mediatype, String mediaPath);

    void getNv21Data(byte[] nv21, String uuid, int width, int height);

    void getVideoData(String mediaPath);
}
