package cho.nico.com.test;

public interface CameraResultCallback {

    void getMediaData(int mediatype, String mediaPath);

    void getNv21Data(byte[] bytes);
}
