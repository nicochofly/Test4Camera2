package cho.nico.com.test;

public interface CameraOprCallback {
     void capturePic();

     void recordVideo();

     void stopRecord();

     void delFile(String path);

}
