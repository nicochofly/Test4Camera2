package cho.nico.com.test;

public interface FaceDetectCallback {

   void detectFinish(int size, long times);

   void detectFailed();
}
