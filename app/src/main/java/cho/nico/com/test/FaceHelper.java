package cho.nico.com.test;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class FaceHelper {
    private static final String TAG = "FaceHelper";
    private FaceEngine faceEngine;


    private int width;
    private int height;
    /**
     * fr 线程数，建议和ft初始化时的maxFaceNum相同
     */
    private int frThreadNum = 5;

    private List<FaceInfo> faceInfoList = new ArrayList<>();
    //    private List<LivenessInfo> livenessInfoList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean frThreadRunning = false;
    private FaceListener faceListener;
    private LinkedBlockingQueue<FaceRecognizeRunnable> faceRecognizeRunnables;
    //trackId相关
//    private int currentTrackId = 0;
//    private List<Integer> formerTrackIdList = new ArrayList<>();
//    private List<Integer> currentTrackIdList = new ArrayList<>();
    private List<Rect> formerFaceRectList = new ArrayList<>();
    private List<FacePreviewInfo> facePreviewInfoList = new ArrayList<>();
    private ConcurrentHashMap<Integer, String> nameMap = new ConcurrentHashMap<>();
    private static final float SIMILARITY_RECT = 0.3f;


    /**
     * 获取性别相关属性
     */
//    int genderCode = -1;
//    List<GenderInfo> genderInfoList = new ArrayList<>();
//
//    int ageCode = -1;
//    List<AgeInfo> ageInfos = new ArrayList<>();


//    List<Face3DAngle> face3DAngles = new ArrayList<>();
//    int face3dCode = -1;
    private FaceHelper(Builder builder) {
        faceEngine = builder.faceEngine;
        faceListener = builder.faceListener;
//        currentTrackId = builder.currentTrackId;
        width = builder.width;
        height = builder.height;
        if (builder.frThreadNum > 0) {
            frThreadNum = builder.frThreadNum;
            faceRecognizeRunnables = new LinkedBlockingQueue<FaceRecognizeRunnable>(frThreadNum);
        } else {
            Log.e(TAG, "frThread num must > 0,now using default value:" + frThreadNum);
        }
        if (width == 0 || height == 0) {
            throw new RuntimeException("previewSize must be specified!");
        }
    }


    /**
     * 请求获取人脸特征数据，需要传入FR的参数，以下参数同 AFR_FSDKEngine.AFR_FSDK_ExtractFRFeature
     *
     * @param nv21     NV21格式的图像数据
     * @param faceInfo 人脸信息
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像格式
     * @param trackId  请求人脸特征的唯一请求码，一般使用trackId
     */
    public void requestFaceFeature(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
        if (faceListener != null) {
            if (faceEngine != null && faceRecognizeRunnables != null && faceRecognizeRunnables.size() < frThreadNum && !frThreadRunning) {
                faceRecognizeRunnables.add(new FaceRecognizeRunnable(nv21, faceInfo, width, height, format, trackId));
                executor.execute(faceRecognizeRunnables.poll());
            } else {
                faceListener.onFaceFeatureInfoGet(null, nv21, faceInfo);
            }
        }
    }


    public void release() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        if (faceInfoList != null) {
            faceInfoList.clear();
        }
        if (faceRecognizeRunnables != null) {
            faceRecognizeRunnables.clear();
        }
        if (nameMap != null) {
            nameMap.clear();
        }
        faceRecognizeRunnables = null;
        nameMap = null;
        faceListener = null;
        faceInfoList = null;
    }


    int pic = 0;

    public List<FacePreviewInfo> onPreviewFrame(byte[] nv21) {
        if (faceListener != null) {
            if (faceEngine != null) {
                faceInfoList.clear();
                facePreviewInfoList.clear();
                int code = faceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code != ErrorInfo.MOK) {
                    faceListener.onFail(new Exception("ft failed,code is " + code));
                } else {

                    Log.e("MainActivity", "=============faceInfoList.size()==================" + faceInfoList.size());
                    if (faceInfoList.size() > 0) {
                        pic += 1;

                        Log.e("MainActivity", "提取 " + pic);
//                        int gCode = faceEngine.process(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);

//                        Log.e("MainActivity", "process gCode =" + gCode);
//                        if (gCode == ErrorInfo.MOK) {
                        //性别信息结果
//                            genderInfoList.clear();
//                            genderCode = faceEngine.getGender(genderInfoList);
//
//                            ageInfos.clear();
//                            ageCode = faceEngine.getAge(ageInfos);

//                        face3DAngles.clear();
//                        face3dCode = faceEngine.getFace3DAngle(face3DAngles);

//                        if (face3dCode == ErrorInfo.MOK) {
//                            Face3DAngle face3DAngle = face3DAngles.get(0);
//                            float yaw = face3DAngle.getYaw();
//                            float pitch = face3DAngle.getPitch();
//                            if (yaw > -20 && yaw < 20 && pitch > -15 && pitch < 15 && face3DAngle.getStatus() == 0) {
//
//                            }
//                        }

//                        Log.e("cdq",face3DAngle.toString());
                        for (int i = 0; i < faceInfoList.size(); i++) {
                            int gender = 0;
                            int age = 0;
//                                if (genderCode == ErrorInfo.MOK) {
//                                    if (i < genderInfoList.size()) {
//                                        gender = genderInfoList.get(i).getGender();
//                                    }
//                                }
//
//                                if (ageCode == ErrorInfo.MOK) {
//                                    if (i < ageInfos.size()) {
//                                        age = ageInfos.get(i).getAge();
//                                    }
//                                }
                            facePreviewInfoList.add(new FacePreviewInfo(faceInfoList.get(i), null, -1, gender, age));
                        }
//                        }
                    }
                }
            }
            return facePreviewInfoList;
        } else {
            facePreviewInfoList.clear();
            return facePreviewInfoList;
        }
    }

    /**
     * 人脸解析的线程
     */
    public class FaceRecognizeRunnable implements Runnable {
        private FaceInfo faceInfo;
        private int width;
        private int height;
        private int format;
        private Integer trackId;
        private byte[] nv21Data;

        private FaceRecognizeRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
            if (nv21Data == null) {
                return;
            }
            this.nv21Data = nv21Data;
            this.faceInfo = new FaceInfo(faceInfo);
            this.width = width;
            this.height = height;
            this.format = format;
            this.trackId = trackId;
        }

        @Override
        public void run() {


            synchronized (faceEngine) {
                frThreadRunning = true;


                Log.i(TAG, "faceListener != null && nv21Data != null = " + (faceListener != null && nv21Data != null));
                if (faceListener != null && nv21Data != null) {
                    Log.i(TAG, "faceEngine != null = " + (faceEngine != null));

                    if (faceEngine != null) {
                        FaceFeature faceFeature = new FaceFeature();
                        long frStartTime = System.currentTimeMillis();
                        int frCode;
                        frCode = faceEngine.extractFaceFeature(nv21Data, width, height, format, faceInfo, faceFeature);

                        Log.i(TAG, "run: fr frCode = " + frCode);

                        if (frCode == ErrorInfo.MOK) {
                            Log.i(TAG, "run: fr costTime = " + (System.currentTimeMillis() - frStartTime) + "ms");
                            faceListener.onFaceFeatureInfoGet(faceFeature, nv21Data, faceInfo);


                        } else {
                            faceListener.onFaceFeatureInfoGet(null, nv21Data, faceInfo);
                            faceListener.onFail(new Exception("fr failed errorCode is " + frCode));
                        }
                    } else {
                        faceListener.onFaceFeatureInfoGet(null, nv21Data, faceInfo);
                        faceListener.onFail(new Exception("fr failed ,frEngine is null"));
                    }
                    if (faceRecognizeRunnables != null && faceRecognizeRunnables.size() > 0) {
                        executor.execute(faceRecognizeRunnables.poll());
                    }
                }
                nv21Data = null;
                frThreadRunning = false;
            }
        }
    }


    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
//    private void refreshTrackId(List<FaceInfo> ftFaceList) {
//        currentTrackIdList.clear();
//        //每项预先填充-1
//        for (int i = 0; i < ftFaceList.size(); i++) {
//            currentTrackIdList.add(-1);
//        }
//        //前一次无人脸现在有人脸，填充新增TrackId
//        if (formerTrackIdList.size() == 0) {
//            for (int i = 0; i < ftFaceList.size(); i++) {
//                currentTrackIdList.set(i, ++currentTrackId);
//            }
//        } else {
//            //前后都有人脸,对于每一个人脸框
//            for (int i = 0; i < ftFaceList.size(); i++) {
//                //遍历上一次人脸框
//                for (int j = 0; j < formerFaceRectList.size(); j++) {
//                    //若是同一张人脸
//                    if (TrackUtil.isSameFace(SIMILARITY_RECT, formerFaceRectList.get(j), ftFaceList.get(i).getRect())) {
//                        //记录ID
//                        currentTrackIdList.set(i, formerTrackIdList.get(j));
//                        break;
//                    }
//                }
//            }
//        }
//        //上一次人脸框不存在此人脸，新增
//        for (int i = 0; i < currentTrackIdList.size(); i++) {
//            if (currentTrackIdList.get(i) == -1) {
//                currentTrackIdList.set(i, ++currentTrackId);
//            }
//        }
//        formerTrackIdList.clear();
//        formerFaceRectList.clear();
//        for (int i = 0; i < ftFaceList.size(); i++) {
//            formerFaceRectList.add(new Rect(ftFaceList.get(i).getRect()));
//            formerTrackIdList.add(currentTrackIdList.get(i));
//        }
//
//        //刷新nameMap
//        clearLeftName(currentTrackIdList);
//    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
//    public int getCurrentTrackId() {
//        return currentTrackId;
//    }

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void addName(int trackId, String name) {
        if (nameMap != null) {
            nameMap.put(trackId, name);
        }
    }

    public String getName(int trackId) {
        return nameMap == null ? null : nameMap.get(trackId);
    }

    /**
     * 清除map中已经离开的人脸
     *
     * @param trackIdList 最新的trackIdList
     */
    private void clearLeftName(List<Integer> trackIdList) {
        Set<Integer> keySet = nameMap.keySet();
        for (Integer integer : keySet) {
            if (!trackIdList.contains(integer)) {
                nameMap.remove(integer);
            }
        }
    }

    public static final class Builder {
        private FaceEngine faceEngine;
        private FaceListener faceListener;
        private int frThreadNum;
        private int currentTrackId;
        private int width;
        private int height;

        public Builder() {
        }


        public Builder faceEngine(FaceEngine val) {
            faceEngine = val;
            return this;
        }


        public Builder faceListener(FaceListener val) {
            faceListener = val;
            return this;
        }

        public Builder frThreadNum(int val) {
            frThreadNum = val;
            return this;
        }

        public Builder width(int val) {
            width = val;
            return this;
        }

        public Builder height(int val) {
            height = val;
            return this;
        }

        public Builder currentTrackId(int val) {
            currentTrackId = val;
            return this;
        }

        public FaceHelper build() {
            return new FaceHelper(this);
        }
    }


    /**
     * 注册人脸
     *
     * @param context 上下文对象
     * @param nv21    NV21数据
     * @param width   NV21宽度
     * @param height  NV21高度
     * @return 是否注册成功
     */
    public boolean register(/*Context context,*/ byte[] nv21, int width, int height) {
        synchronized (this) {
            Log.e(TAG, "&&&&&&&&&&&&   registerMethod");
            if (faceEngine == null ||/* context == null ||*/ nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2) {
                return false;
            }
            Log.e(TAG, "registerMethod    !(faceEngine == null || context == null || nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2)");

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

            Log.e(TAG, "registerMethod    dirExists " + dirExists);
            Log.e(TAG, "registerMethod    widthxheight" + width + "x" + height);
            //1.人脸检测
            Log.e(TAG, "faceEngine==null" + (faceEngine == null));
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int code = faceEngine.detectFaces(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList);
            Log.e(TAG, "registerMethod    faceEngine.detectFaces  code  == " + code + "  faceInfoList.size == " + faceInfoList.size());
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                {
                    try {
                        FaceFeature faceFeature = new FaceFeature();
                        //2.特征提取
                        code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
                        Log.e(TAG, "registerMethod  extractFaceFeaturecode == " + code);
                        String phone = String.valueOf(System.currentTimeMillis());


                        //3.保存注册结果（注册图、特征数据）
                        if (code == ErrorInfo.MOK) {
                            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);

                            //为了美观，扩大rect截取注册图
                            Rect cropRect = getBestRect(width, height, faceInfoList.get(0).getRect());

                            if (cropRect == null) {
                                return false;
                            }


                            File file = new File(imgDir + File.separator + phone + ".jpg");
                            FileOutputStream fosImage = new FileOutputStream(file);
                            yuvImage.compressToJpeg(cropRect, 80, fosImage);
                            fosImage.close();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.e(TAG, "registerMethod    code == ErrorInfo.MOK && faceInfoList.size() > 0");


            }
            return false;
        }

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
        //1.原rect边界未溢出宽高的情况
        int padding = rect.height() / 4;
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

}
