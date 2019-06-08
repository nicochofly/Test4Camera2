package cho.nico.com.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.VersionInfo;
import com.example.myapplication.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements RtspHelper.RtspCallback, ResizeCameraViewCallback {
    private String[] permissonArray = new String[]
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
            };

    private List<String> mRequestPermission = new ArrayList<String>();

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    private FaceEngine faceEngine;

    VideoView surfaceView;
    SurfaceHolder surfaceHolder;

    //    VideoView videoView;
    CaptureButton captureButton;

    private String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    TipsDialog opyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;


        startrequestPermission();

        initEngine();
        initCameraView();
    }


    private void initView() {
        surfaceView = findViewById(R.id.camera_sv);
        surfaceHolder = surfaceView.getHolder();
        captureButton = findViewById(R.id.btn);
        CameraUtils.getInstance(getBaseContext(), surfaceHolder, this).startPreview();

        File dir = new File(basePath + "/abc/");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


    private void startrequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissonArray) {
                if (PackageManager.PERMISSION_GRANTED != checkPermission(permission, Process.myPid(), Process.myUid())) {
                    mRequestPermission.add(permission);
                }
            }
            if (!mRequestPermission.isEmpty()) {
                requestPermissions(mRequestPermission.toArray(new String[mRequestPermission.size()]), ACTION_REQUEST_PERMISSIONS);
            } else {
                try {
//                    test();
//                    play();
                    initView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
//                test();
//                play();
                initView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> picPaths = new ArrayList<>();


    private int num = 0;

    @Override
    public void onPreviewFrame(ByteBuffer buffer, int width, int height) {


        if (mBuffer != null) {
            synchronized (mBuffer) {
                mBuffer.rewind();

                if (num % 3 == 0) {
                    Bitmap bmp = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(mBuffer);
                    try {
                        Log.e(TAG, "onPreviewFrame");
                        File file = new File(basePath + "/abc/" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                }

                num += 1;
                mBuffer.rewind();
                buffer.rewind();
                mBuffer.put(buffer);
            }
        }
    }

    long startTimes = 0l;

    @Override
    public void onstop() {
        Log.e(TAG, "onStop");
        RtspHelper.getInstance(this).releasePlayer();

        startTimes = System.currentTimeMillis();

        FaceServer.getInstance().detect(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abc/", this, new FaceDetectCallback() {
            @Override
            public void detectFinish(final int size, final long times) {


                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        opyDialog.setTitleMsg("一共有检测出" + size + "个特征码\n耗时" + ((times - startTimes) / 1000));
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abc/");
                        File[] files = file.listFiles();
                        for (File f : files) {
                            f.delete();
                        }

                    }
                });


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        opyDialog.dismiss();
                    }
                }, 3000);
            }

            @Override
            public void detectFailed() {

            }
        });

        showProcessDialog();
    }


    int screenWidth, screenHeight;
    ByteBuffer mBuffer;

    private void play() {

        String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera" + File.separator + "acdq.mp4";


        mBuffer = ByteBuffer.allocateDirect(screenWidth * screenHeight * 4)
                .order(ByteOrder.nativeOrder());

        Log.e("caodongquan", "开始时间" + System.currentTimeMillis());
        RtspHelper.getInstance(this).createPlayer(url, screenWidth, screenHeight, this);
    }


    int i = 0;

    private void onPreviewFrame(byte[] nv21) {
        long startTime = System.currentTimeMillis();
        i += 1;
        Log.e(TAG, "arcsoft onPreviewFrame start " + i);
        List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
        Log.e(TAG, "arcsoft onPreviewFrame end cost " + (System.currentTimeMillis() - startTime) + "  " + (facePreviewInfoList.isEmpty()));
//        Log.e(TAG, "onPreviewFrame === nv21 ");
        if (facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            for (int i = 0; i < facePreviewInfoList.size(); i++) {
                faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), screenWidth, screenHeight, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
            }
        }
    }

    /**
     * Bitmap转化为ARGB数据，再转化为NV21数据
     *
     * @param src    传入的Bitmap，格式为{@link Bitmap.Config#ARGB_8888}
     * @param width  NV21图像的宽度
     * @param height NV21图像的高度
     * @return nv21数据
     */
    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
    private static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }

                ++index;
            }
        }
        return nv21;
    }

    private static final int VALUE_FOR_4_ALIGN = 0b11;
    private static final int VALUE_FOR_2_ALIGN = 0b01;

    public static Bitmap alignBitmapForBgr24(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() < 4) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        boolean needAdjust = false;

        //保证宽度是4的倍数
        if ((width & VALUE_FOR_4_ALIGN) != 0) {
            width &= ~VALUE_FOR_4_ALIGN;
            needAdjust = true;
        }

        if (needAdjust) {
            bitmap = imageCrop(bitmap, new Rect(0, 0, width, height));
        }
        return bitmap;
    }


    public static Bitmap imageCrop(Bitmap bitmap, Rect rect) {
        if (bitmap == null || rect == null || rect.isEmpty() || bitmap.getWidth() < rect.right || bitmap.getHeight() < rect.bottom) {
            return null;
        }
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), null, false);
    }

    private static final int MAX_DETECT_NUM = 10;
    private final String TAG = getClass().getSimpleName();
    private int afCode = -1;
    private FaceHelper faceHelper;


    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;

    private int registerStatus = REGISTER_STATUS_READY;

    private ExecutorService excepool = Executors.newCachedThreadPool();

    public static final String APP_ID = "6F8VPaapjic6BkoU2dbrfp1WKwdCjokFNqyFLAYFMRfi";
    public static final String SDK_KEY = "G2owqnL7C3CggWpTVGWQmw6b2JoMnB98DK5kkyibufwR";

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();

        faceEngine.active(this, APP_ID, SDK_KEY);


        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
        }
    }


    private void initCameraView() {


        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final byte[] nv21, final FaceInfo faceInfo) {
                //FR成功
                if (faceFeature != null) {
//                    Log.e(TAG, "开始  " + System.currentTimeMillis());

//                    register(nv21, screenWidth, screenHeight);


//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            register(nv21,screenWidth,screenHeight);
//                        }
//                    });

                    FaceServer.getInstance().register(nv21, screenWidth, screenHeight);


                } else {
                    Log.e(TAG, "faceFeature != null  ");
                }
            }

        };
        faceHelper = new FaceHelper.Builder()
                .faceEngine(faceEngine)
                .frThreadNum(MAX_DETECT_NUM)
                .faceListener(faceListener)
                .width(screenWidth).height(screenHeight)
                .build();

//        faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());


    }


    private Handler handler = new Handler();

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (afCode == ErrorInfo.MOK) {
//            FaceServer.getInstance().unInit();
        }
    }


    private String generateRandomPhone() {
        return "s" + String.format("%09d", new Random().nextInt(1000000000)) + String.format("%02d", new Random().nextInt(10));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                for (String one : permissonArray) {
                    if (permissions[i].equals(one) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mRequestPermission.remove(one);
                    }
                }
            }
        }
    }

    /**
     * bitmap转化为bgr数据，格式为{@link Bitmap.Config#ARGB_8888}
     *
     * @param image 传入的bitmap
     * @return bgr数据
     */
    public static byte[] bitmapToBgr(Bitmap image) {
        if (image == null) {
            return null;
        }
        int bytes = image.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        image.copyPixelsToBuffer(buffer);
        byte[] temp = buffer.array();
        byte[] pixels = new byte[(temp.length / 4) * 3];
        for (int i = 0; i < temp.length / 4; i++) {
            pixels[i * 3] = temp[i * 4 + 2];
            pixels[i * 3 + 1] = temp[i * 4 + 1];
            pixels[i * 3 + 2] = temp[i * 4];
        }
        return pixels;
    }




    @Override
    public void resizeView(int w, int h) {
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        if (w != -1 && h != -1) {
            params.width = w;
            params.height = h;
            surfaceView.setLayoutParams(params);
            surfaceView.invalidate();
        }
    }

    @Override
    public void playVideo() {

        if (!TextUtils.isEmpty(videoPath)) {
//            surfaceView.setVisibility(View.GONE);
//            videoView.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.GONE);


//            videoPlay();


//            Log.e(TAG, "playVideo  " + videoPath);
//
//            mBuffer = ByteBuffer.allocateDirect(screenWidth * screenHeight * 4)
//                    .order(ByteOrder.nativeOrder());
//            Log.e("caodongquan", "开始时间" + System.currentTimeMillis());
//            RtspHelper.getInstance(this).createPlayer(videoPath, screenWidth, screenHeight, this);
//            RtspHelper.getInstance(this).createPlayer(videoPath, screenWidth, screenHeight, this);


            VideoToFrames videoToFrames = new VideoToFrames();
            videoToFrames.setCallback(new VideoToFrames.Callback() {
                @Override
                public void onFinishDecode() {

                }

                @Override
                public void onDecodeFrame(byte[] nv21) {

                    onPreviewFrame(nv21);

                }
            });
            try {
                OutputImageFormat outputImageFormat = OutputImageFormat.NV21;

                String path = basePath+"/abc/";
                videoToFrames.setSaveFrames(path, outputImageFormat);
                videoToFrames.decode(videoPath);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private String videoPath;


    private MediaPlayer mMediaPlayer;


    private void videoPlay() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {

                    Log.e(TAG, "videoPath " + videoPath);
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();

                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(videoPath);
                    mMediaPlayer.setSurface(surfaceView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            FrameLayout.LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * screenWidth);
            videoViewParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            surfaceView.setLayoutParams(videoViewParam);
        }
    }

    @Override
    public void setVideoPath(String path) {
        videoPath = path;

    }

    private void showProcessDialog() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                opyDialog = new TipsDialog(MainActivity.this);
                opyDialog.setTitleMsg("照片检测中...");
                opyDialog.show();
            }
        });

    }
}
