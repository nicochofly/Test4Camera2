package cho.nico.com.test;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcsoft.face.FaceEngine;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends FragmentActivity  {

    private FaceEngine faceEngine;


    private final String TAG = getClass().getSimpleName();

    public static final String APP_ID = "6F8VPaapjic6BkoU2dbrfp1WKwdCjokFNqyFLAYFMRfi";
    public static final String SDK_KEY = "G2owqnL7C3CggWpTVGWQmw6b2JoMnB98DK5kkyibufwR";

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
       int code = faceEngine.active(this, APP_ID, SDK_KEY);
        Log.e("caodongquan","code ==" +code);
    }

    private String[] permissonArray = new String[]
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
            };

    private List<String> mRequestPermission = new ArrayList<String>();

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        initEngine();

        startrequestPermission();


//        surfaceView = findViewById(R.id.camera_sv);
//
//        surfaceHolder = surfaceView.getHolder();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
//                    initView();
                    getSupportFragmentManager().beginTransaction().add(R.id.fl_layout, CameraFragment.newInstance()).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
//                initView();
                getSupportFragmentManager().beginTransaction().add(R.id.fl_layout, CameraFragment.newInstance()).commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

}
