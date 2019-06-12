package cho.nico.com.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.example.camera2lib.Camera2Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 人脸库操作类，包含注册和搜索
 */
public class FaceServer {
    private static final String TAG = "FaceServer";
    public static final String IMG_SUFFIX = ".jpg";
    private static FaceEngine faceEngine = null;
    private static FaceServer faceServer = null;
    public static String ROOT_PATH;
    public static final String SAVE_IMG_DIR = "register" + File.separator + "imgs";
    private static final String SAVE_FEATURE_DIR = "register" + File.separator + "features";

    /**
     * 是否正在搜索人脸，保证搜索操作单线程进行
     */
    private boolean isProcessing = false;

    public static FaceServer getInstance() {
        if (faceServer == null) {
            synchronized (FaceServer.class) {
                if (faceServer == null) {
                    faceServer = new FaceServer();
                }
            }
        }
        return faceServer;
    }

    /**
     * 初始化
     *
     * @param context 上下文对象
     * @return 是否初始化成功
     */
    public boolean init(Context context) {
        synchronized (this) {
            if (faceEngine == null && context != null) {
                faceEngine = new FaceEngine();
                int engineCode = faceEngine.init(context, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_HIGHER_EXT, 32, 50, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
                if (engineCode == ErrorInfo.MOK) {
//                    initFaceList(context);
                    return true;
                } else {
                    faceEngine = null;
                    Log.e(TAG, "init: failed! code = " + engineCode);
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * 销毁
     */
    public void unInit() {
        synchronized (this) {
//            if (faceRegisterInfoList != null) {
//                faceRegisterInfoList.clear();
//                faceRegisterInfoList = null;
//            }
            if (faceEngine != null) {
                faceEngine.unInit();
                faceEngine = null;
            }
        }
    }

    /**
     * 初始化人脸特征数据以及人脸特征数据对应的注册图
     *
     * @param context 上下文对象
     */
    private void initFaceList(Context context) {
//        synchronized (this) {
//            if (ROOT_PATH == null) {
//                ROOT_PATH = context.getFilesDir().getAbsolutePath();
//            }
//            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
//            if (!featureDir.exists() || !featureDir.isDirectory()) {
//                return;
//            }
//            File[] featureFiles = featureDir.listFiles();
//            if (featureFiles == null || featureFiles.length == 0) {
//                return;
//            }
//            faceRegisterInfoList = new ArrayList<>();
//            for (File featureFile : featureFiles) {
//                try {
//                    FileInputStream fis = new FileInputStream(featureFile);
//                    byte[] feature = new byte[FaceFeature.FEATURE_SIZE];
//                    fis.read(feature);
//                    fis.close();
//                    faceRegisterInfoList.add(new FaceRegisterInfo(feature, featureFile.getName()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public int getFaceNumber(Context context) {
        synchronized (this) {
            if (context == null) {
                return 0;
            }
            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }

            File featureFileDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            int featureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                String[] featureFiles = featureFileDir.list();
                featureCount = featureFiles == null ? 0 : featureFiles.length;
            }
            int imageCount = 0;
            File imgFileDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                String[] imageFiles = imgFileDir.list();
                imageCount = imageFiles == null ? 0 : imageFiles.length;
            }
            return featureCount > imageCount ? imageCount : featureCount;
        }
    }


    /**
     * 注册人脸
     *
     * @param nv21   NV21数据
     * @param width  NV21宽度
     * @param height NV21高度
     * @return 是否注册成功
     */
    public boolean register(/*Context context,*/ byte[] nv21, int width, int height) {


        Log.e(TAG, "FaceServer register start");
        if (faceEngine == null ||/* context == null ||*/ nv21 == null /*|| width % 4 != 0 || nv21.length != width * height * 3 / 2*/) {
            return false;
        }
        Log.e(TAG, "FaceServer  register  step1 ");

        String ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

        boolean dirExists = true;
        //图片存储的文件夹
        File imgDir = new File(ROOT_PATH + File.separator + "aaaa");
        if (!imgDir.exists()) {
            dirExists = imgDir.mkdirs();
        }
        if (!dirExists) {
            return false;
        }

//        Log.e(TAG, "registerMethod    dirExists " + dirExists);
//        Log.e(TAG, "registerMethod    widthxheight" + width + "x" + height);
        //1.人脸检测
//        Log.e(TAG, "faceEngine==null" + (faceEngine == null));
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int code = faceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
        Log.e(TAG, "FaceServer  register  code  == " + code + "  faceInfoList.size == " + faceInfoList.size());
        if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
            {
                try {
                    FaceFeature faceFeature = new FaceFeature();
                    //2.特征提取
                    code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
                    Log.e(TAG, "FaceServer  register  extractFaceFeaturecode == " + code);
                    String phone = String.valueOf(System.currentTimeMillis());


                    //3.保存注册结果（注册图、特征数据）
                    if (code == ErrorInfo.MOK) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(nv21, 0, nv21.length);
//                        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);

                        //为了美观，扩大rect截取注册图
                        Rect cropRect = getBestRect(width, height, faceInfoList.get(0).getRect());

                        if (cropRect == null) {
                            return false;
                        }


                        File file = new File(imgDir + File.separator + phone + ".jpg");
                        FileOutputStream fosImage = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fosImage);
//                        yuvImage.compressToJpeg(cropRect, 80, fosImage);
                        fosImage.close();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.e(TAG, "FaceServer  register end");


        }
        return false;

    }


    String headerPath;

    /**
     * 保存图片到本地
     *
     * @param nv21
     * @param faceInfo
     * @return
     */
    @SuppressLint("NewApi")
    private String saveHeaderImg(byte[] nv21, FaceInfo faceInfo, int width, int height) {
        if (nv21 == null || faceInfo == null) {
            return "";
        }

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);

        String name = String.valueOf(UUID.randomUUID());

        headerPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "videodetect" + File.separator + Camera2Utils.getInstance().getCurrentVideoName() + File.separator;
        FileOutputStream fileOutputStream;
        Bitmap resultBmp = null;
        try {


            Rect rectResult = getBestRect(width, height, faceInfo.getRect());
            File headerDir = new File(headerPath);
            if (!headerDir.exists()) {
                headerDir.mkdirs();
            }
            File headerFile = new File(headerPath + name + ".jpg");
            fileOutputStream = new FileOutputStream(headerFile);
            yuvImage.compressToJpeg(rectResult, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();


            Bitmap bitmap = BitmapFactory.decodeFile(headerFile.getAbsolutePath());
            //判断人脸旋转角度，若不为0度则旋转注册图
            boolean needAdjust = false;
            if (bitmap != null) {
                switch (faceInfo.getOrient()) {
                    case FaceEngine.ASF_OC_0:
                        break;
                    case FaceEngine.ASF_OC_90:
                        bitmap = getRotateBitmap(bitmap, 90);
                        needAdjust = true;
                        break;
                    case FaceEngine.ASF_OC_180:
                        bitmap = getRotateBitmap(bitmap, 180);
                        needAdjust = true;
                        break;
                    case FaceEngine.ASF_OC_270:
                        bitmap =getRotateBitmap(bitmap, 270);
                        needAdjust = true;
                        break;
                    default:
                        break;
                }
            }
            if (needAdjust) {
                fileOutputStream = new FileOutputStream(headerFile.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (resultBmp != null) {
                resultBmp.recycle();
                resultBmp = null;
            }
        }
        return headerPath + name + ".jpg";
    }
    public  Bitmap getRotateBitmap(Bitmap b, float rotateDegree) {
        if (b == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
    }

    /**
     * 在特征库中搜索
     *
     * @param faceFeature 传入特征数据
     * @return 比对结果
     */
    public CompareResult getTopOfFaceLib(FaceFeature faceFeature) {
//        if (faceEngine == null || isProcessing || faceFeature == null || faceRegisterInfoList == null || faceRegisterInfoList.size() == 0) {
//            return null;
//        }
//        FaceFeature tempFaceFeature = new FaceFeature();
//        FaceSimilar faceSimilar = new FaceSimilar();
//        float maxSimilar = 0;
//        int maxSimilarIndex = -1;
//        isProcessing = true;
//        for (int i = 0; i < faceRegisterInfoList.size(); i++) {
//            tempFaceFeature.setFeatureData(faceRegisterInfoList.get(i).getFeatureData());
//            faceEngine.compareFaceFeature(faceFeature, tempFaceFeature, faceSimilar);
//            if (faceSimilar.getScore() > maxSimilar) {
//                maxSimilar = faceSimilar.getScore();
//                maxSimilarIndex = i;
//            }
//        }
//        isProcessing = false;
//        if (maxSimilarIndex != -1) {
//
//        }
        return null;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    private static Rect getBestRect(int width, int height, Rect srcRect) {

        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);
        //1.原rect边界已溢出宽高的情况
        int maxOverFlow = 0;
        int tempOverFlow = 0;
        if (rect.left < 0) {
            maxOverFlow = -rect.left;
        }
        if (rect.top < 0) {
            tempOverFlow = -rect.top;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.right > width) {
            tempOverFlow = rect.right - width;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (rect.bottom > height) {
            tempOverFlow = rect.bottom - height;
            if (tempOverFlow > maxOverFlow) {
                maxOverFlow = tempOverFlow;
            }
        }
        if (maxOverFlow != 0) {
            rect.left += maxOverFlow;
            rect.top += maxOverFlow;
            rect.right -= maxOverFlow;
            rect.bottom -= maxOverFlow;
            return rect;
        }
        //2.原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;
        //若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0 && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }

        rect.left -= padding;
        rect.top -= padding;
        rect.right += padding;
        rect.bottom += padding;
        return rect;
    }


    Executor executor = Executors.newSingleThreadExecutor();
    //    LinkedBlockingQueue<FaceRunnable> tasks = new LinkedBlockingQueue();
    LinkedBlockingQueue<FaceRunnableNv21> tasksnv21 = new LinkedBlockingQueue();
    FaceEngine localFaceEngine;

    FaceDetectCallback faceDetectCallback;


    public void detectNv21(byte[] nv21, Context context, FaceDetectCallback faceDetectCallback, int width, int height) {

        this.faceDetectCallback = faceDetectCallback;


        Log.e(TAG, width + "x" + height);

        if (localFaceEngine == null) {
            localFaceEngine = new FaceEngine();
            int engineCode = localFaceEngine.init(context, FaceEngine.ASF_DETECT_MODE_VIDEO, FaceEngine.ASF_OP_0_HIGHER_EXT, 16, 50, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
        }


        FaceRunnableNv21 faceRunnable = new FaceRunnableNv21(nv21, width, height);
        tasksnv21.add(faceRunnable);

        executor.execute(tasksnv21.poll());

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


    private List<FaceFeature> faceFeatures = new ArrayList<>();


    public class FaceRunnableNv21 implements Runnable {

        byte[] nv21;
        int width;
        int height;

        private FaceRunnableNv21(byte[] nv21, int width, int height) {
            this.nv21 = nv21;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {


            synchronized (localFaceEngine) {


//                Bitmap originBmp = BitmapFactory.decodeFile(filePath);
//                Bitmap newBmp = alignBitmapForBgr24(originBmp);
//                byte[] rgb = bitmapToBgr(newBmp);

                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = localFaceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList);
                Log.e("caodongquan", "1  code " + code);

                if (code == 0) {
                    if (!faceInfoList.isEmpty()) {

                        for (FaceInfo f : faceInfoList) {
                            FaceFeature faceFeature = new FaceFeature();
                            int eCode = localFaceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, f, faceFeature);

                            Log.e("caodongquan", "2  ecode " + eCode);
                            if (eCode == 0) {
                                addtoFaceFeature(nv21, f, faceFeature, width, height);
                            }
                        }
                    }
                }

                FaceRunnableNv21 runnable = tasksnv21.poll();
                if (runnable == null) {


//                    faceDetectCallback.detectFinish(faceFeatures.size(), System.currentTimeMillis());
                    Log.e(TAG, "一共有===" + faceFeatures.size() + "个特征码   " + System.currentTimeMillis());
//                    Log.e("caodongquan", "结束时间   " + System.currentTimeMillis());

                } else {
                    executor.execute(runnable);
                }
            }


        }
    }

    private void addtoFaceFeature(byte[] nv21, FaceInfo faceinfo, FaceFeature faceFeature, int width, int height) {
        if (faceFeatures.isEmpty()) {
            faceFeatures.add(faceFeature);

            if (nv21 != null) {
                String p = saveHeaderImg(nv21, faceinfo, width, height);
            }
        } else {
            boolean hasYet = false;
            for (FaceFeature f : faceFeatures) {
                FaceSimilar faceSimilar = new FaceSimilar();
                int cCode = localFaceEngine.compareFaceFeature(f, faceFeature, faceSimilar);
                if (cCode == 0) {
                    if (faceSimilar.getScore() > 0.85f) {
                        hasYet = true;
                        break;
                    }
                }
            }
            if (!hasYet) {
                if (nv21 != null) {
                    String p = saveHeaderImg(nv21, faceinfo, width, height);
                }
                faceFeatures.add(faceFeature);
            }
        }
    }


}
