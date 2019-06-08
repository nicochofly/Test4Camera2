package cho.nico.com.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtils {


    private static CameraUtils cameraUtils;

    public Context context;

    private String TAG = getClass().getSimpleName();

    private SensorManager sensorManager = null;

    private SurfaceHolder surfaceHolder;

    private int screenWidth, screenHeight;

    /**
     * 硬件旋转角度
     */
    int hardWareAngle = 0;

    private Camera camera;

    int mCameraId = -1;

    int angle;

    WindowManager windowManager;

    ResizeCameraViewCallback resizeCallback;

    private int cameraAngle = 90;

    private CameraSizeComparator sizeComparator = new CameraSizeComparator();


    private MediaRecorder mediaRecorder;


    private boolean isRecorder;

    private String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    Camera.Size mPreviewSize;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (isRecorder) {
                mPreviewSize = camera.getParameters().getPreviewSize();
//            if (nativeEncoder != null) {
//                nativeEncoder.onPreviewFrame(data, mPreviewSize.width, mPreviewSize.height);
//            }
//            Log.e(TAG,"data");

                FaceServer.getInstance().detectNv21(data, context, new FaceDetectCallback() {
                    @Override
                    public void detectFinish(int size, long times) {

                    }

                    @Override
                    public void detectFailed() {

                    }
                }, mPreviewSize.width, mPreviewSize.height);
            }
        }
    };


    NativeEncoder nativeEncoder;


    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            initCameraId();
            openCamera();
            initParameter();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private CameraUtils(Context ctx, SurfaceHolder sholder, ResizeCameraViewCallback resize) {
        context = ctx;
        surfaceHolder = sholder;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(displayMetrics);
        } else {
            display.getMetrics(displayMetrics);
        }
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        resizeCallback = resize;

        nativeEncoder = new NativeEncoder();
    }


    public static CameraUtils getInstance(Context ctx, SurfaceHolder shodler, ResizeCameraViewCallback resizeCameraView) {
        if (cameraUtils == null) {
            cameraUtils = new CameraUtils(ctx, shodler, resizeCameraView);
        }
        return cameraUtils;
    }


    public static CameraUtils getInstance() {

        return cameraUtils;
    }


    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                return true;
            }
        }
        return false;
    }


    void registerSensorManager(Context context) {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    void unregisterSensorManager(Context context) {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sensorManager.unregisterListener(sensorEventListener);
    }


    private SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            angle = getSensorAngle(values[0], values[1]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    public int getSensorAngle(float x, float y) {
        if (Math.abs(x) > Math.abs(y)) {
            /**
             * 横屏倾斜角度比较大
             */
            if (x > 4) {
                /**
                 * 左边倾斜
                 */
                return 270;
            } else if (x < -4) {
                /**
                 * 右边倾斜
                 */
                return 90;
            } else {
                /**
                 * 倾斜角度不够大
                 */
                return 0;
            }
        } else {
            if (y > 7) {
                /**
                 * 左边倾斜
                 */
                return 0;
            } else if (y < -7) {
                /**
                 * 右边倾斜
                 */
                return 180;
            } else {
                /**
                 * 倾斜角度不够大
                 */
                return 0;
            }
        }
    }


    public Camera.Size getPreviewSize(List<Camera.Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Preview :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    public Camera.Size getPictureSize(List<Camera.Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Picture :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    private Camera.Size getBestSize(List<Camera.Size> list, float rate) {
        float previewDisparity = 100;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Camera.Size cur = list.get(i);
            float prop = (float) cur.width / (float) cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }
        return list.get(index);
    }

    private boolean isSupportAutoFocus(List<String> supportedFocusModes, String flashModeAuto) {
        boolean supportAutoFocus = false;
        for (String str : supportedFocusModes) {
            if (TextUtils.equals(str, flashModeAuto)) {
                supportAutoFocus = true;
                break;
            }
        }
        return supportAutoFocus;

    }

    private ViewGroup.LayoutParams
    reSizeCameraView(ViewGroup.LayoutParams params) {
        Camera.Size previewsize = getSupportPreviewSize(0, camera.getParameters(), screenWidth, screenHeight);
        if (screenHeight != previewsize.width || screenWidth != previewsize.height) {
            if (screenHeight > previewsize.width) {
                int resultW = screenHeight * previewsize.height / previewsize.width;
                int resultH = screenHeight;
                params.width = resultW;
                params.height = resultH;
            } else {
                int resultW = previewsize.height;
                int resultH = previewsize.width;
                params.width = resultW;
                params.height = resultH;
            }

        }
        return params;


    }


    public int getCameraDisplayOrientation(Context context,
                                           int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public void stopAndReleaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

//    @Override
//    public void onPictureTaken(byte[] data, Camera camera) {
//        String path = processImg(defaultCamera == Camera.CameraInfo.CAMERA_FACING_BACK ? true : false, data);
//        if (!TextUtils.isEmpty(path)) {
//            if (captureType == 0) {
//                TakepicActivity.launch(getActivity(), path, defaultCamera == Camera.CameraInfo.CAMERA_FACING_BACK ? true : false, hardWareAngle);
//            } else if (captureType == 1) {
//                RecaptureActivity.launch(getActivity(), path, defaultCamera == Camera.CameraInfo.CAMERA_FACING_BACK ? true : false, hardWareAngle);
//            }
//        }
//    }


    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }


    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }


    /**
     * 返回相机，默认取后置相机
     *
     * @return
     */
    private void initCameraId() {
        int cameraNums = Camera.getNumberOfCameras();
        if (cameraNums == 0) {
            mCameraId = -1;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }


    private void openCamera() {
        camera = Camera.open(mCameraId);
    }


    public void startPreview() {
        surfaceHolder.addCallback(surfaceCallback);
    }

    private void initParameter() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewsize = getSupportPreviewSize(0, parameters, screenWidth, screenHeight);
        parameters.setPreviewSize(previewsize.width, previewsize.height);

        if (screenHeight != previewsize.width || screenWidth != previewsize.height) {
            if (screenHeight > previewsize.width) {
                int resultW = screenHeight * previewsize.height / previewsize.width;
                int resultH = screenHeight;
                resizeCallback.resizeView(resultW, resultH);
            } else {
                int resultW = previewsize.height;
                int resultH = previewsize.width;
                resizeCallback.resizeView(resultW, resultH);
            }

        }


        if (isSupportedFocusMode(
                parameters.getSupportedFocusModes(),
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            flashIv.setVisibility(View.VISIBLE);
//            flashIv.setImageResource(R.drawable.flash_auto);
//            flashFlag = 0;
        } else {
//            flashIv.setVisibility(View.GONE);
        }

        if (isSupportedPictureFormats(parameters.getSupportedPictureFormats(),
                ImageFormat.JPEG)) {
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(100);
        }

        if (isSupportAutoFocus(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }


        Camera.Size picSize = getSupportPreviewSize(1, parameters, screenWidth, screenHeight);
        parameters.setPictureSize(picSize.width, picSize.height);
        camera.setParameters(parameters);
        int orientation = getCameraDisplayOrientation(context, mCameraId, camera);
        camera.setDisplayOrientation(orientation);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param type         0 预览尺寸 1 拍照尺寸
     * @param parameters
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public Camera.Size getSupportPreviewSize(int type, Camera.Parameters parameters, int screenWidth, int screenHeight) {
        List<Camera.Size> sizes = null;
        Camera.Size resultSize = null;
        float ratio = (screenHeight * 1.0f) / (screenWidth * 1.0f);
        if (type == 0) {
            sizes = parameters.getSupportedPreviewSizes();
            resultSize = getPreviewSize(sizes, 1000, ratio);
        } else if (type == 1) {
            sizes = parameters.getSupportedPictureSizes();
            resultSize = getPictureSize(sizes, 1200, ratio);
        }
        return resultSize;
    }


    //启动录像
    public void startRecord() {
//        camera.setPreviewCallback(null);
        final int nowAngle = (angle + 90) % 360;
        Camera.Parameters parameters = camera.getParameters();
        Matrix matrix = new Matrix();
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            matrix.setRotate(nowAngle);
        } else if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            matrix.setRotate(270);
        }

        if (isRecorder) {
            return;
        }

        if (camera == null) {
            openCamera();
        }
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        if (parameters == null) {
            parameters = camera.getParameters();
        }
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
        camera.unlock();
        mediaRecorder.reset();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        float ratio = (screenHeight * 1.0f) / (screenWidth * 1.0f);
        Camera.Size videoSize;
        if (parameters.getSupportedVideoSizes() == null) {
            videoSize = getPreviewSize(parameters.getSupportedPreviewSizes(), 600, ratio);
        } else {
            videoSize = getPreviewSize(parameters.getSupportedVideoSizes(), 600, ratio);
        }
        Log.i(TAG, "setVideoSize    width = " + videoSize.width + "height = " + videoSize.height);
        mediaRecorder.setVideoSize(videoSize.width, videoSize.height);

        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //手机预览倒立的处理
            if (cameraAngle == 270) {
                //横屏
                if (nowAngle == 0) {
                    mediaRecorder.setOrientationHint(180);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(270);
                } else {
                    mediaRecorder.setOrientationHint(90);
                }
            } else {
                if (nowAngle == 90) {
                    mediaRecorder.setOrientationHint(270);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(90);
                } else {
                    mediaRecorder.setOrientationHint(nowAngle);
                }
            }
        } else {
            mediaRecorder.setOrientationHint(nowAngle);
        }
        mediaRecorder.setVideoEncodingBitRate(MEDIA_QUALITY_MIDDLE);

//        if (DeviceUtil.isHuaWeiRongyao()) {
//            mediaRecorder.setVideoEncodingBitRate(4 * 100000);
//        } else {
//            mediaRecorder.setVideoEncodingBitRate(MEDIA_QUALITY_MIDDLE);
//        }
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        String saveVideoPath = basePath + File.separator + "video";

        File file = new File(saveVideoPath);
        if (!file.exists()) {
            file.mkdir();
        }
        String videoFileAbsPath = saveVideoPath + File.separator + videoFileName;
        mediaRecorder.setOutputFile(videoFileAbsPath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecorder = true;

            resizeCallback.setVideoPath(videoFileAbsPath);
        } catch (IllegalStateException e) {
            resizeCallback.setVideoPath("");
            e.printStackTrace();
        } catch (IOException e) {
            resizeCallback.setVideoPath("");
            e.printStackTrace();
        } catch (RuntimeException e) {
            resizeCallback.setVideoPath("");
        }
    }

    //停止录像
    public void stopRecord() {
        if (!isRecorder) {
            return;
        }
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                mediaRecorder = null;
                mediaRecorder = new MediaRecorder();
            } finally {
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }
                mediaRecorder = null;
                isRecorder = false;
            }
        }
//        camera.stopPreview();

//        resizeCallback.playVideo();
    }

    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
}
