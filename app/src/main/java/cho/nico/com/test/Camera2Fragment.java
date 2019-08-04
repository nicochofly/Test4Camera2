package cho.nico.com.test;

import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cho.nico.com.test.callback.CameraResultCallback;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Fragment extends Fragment implements CameraOprCallback, CameraResultCallback {


    @BindView(R.id.capture_iv)
    Camera2ShutterBtn captureIv;
    private String TAG = getClass().getSimpleName();
    @BindView(R.id.main_texture)
    AutoFitTextureView mainTexture;
    @BindView(R.id.close_iv)
    ImageView closeIv;
    @BindView(R.id.flip_iv)
    ImageView flipIv;

    Unbinder unbinder;

    private boolean supportCamera = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? true : false;

    /**
     * 0 拍照
     * 2 翻拍
     */
    private int captureType;


    public static Camera2Fragment newInstance() {

        Camera2Fragment camera2Fragment = new Camera2Fragment();
        Bundle bundle = new Bundle();
        camera2Fragment.setArguments(bundle);
        return camera2Fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    public void initData() {
        captureIv.setCameraOprCallback(this);
        try {
            Camera2Tools.getInstance().init(getContext(), mainTexture);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (supportCamera) {
            mainTexture.setVisibility(View.VISIBLE);
        } else {
            mainTexture.setVisibility(View.GONE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera2, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.close_iv, R.id.flip_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.close_iv:
                getActivity().finish();
                break;
            case R.id.flip_iv:
                try {
                    Camera2Tools.getInstance().flip();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    @Override
    public void capturePic() {
        Camera2Tools.getInstance().takePicture();
    }

    @Override
    public void recordVideo() {
        Camera2Tools.getInstance().startRecord();
    }

    @Override
    public void stopRecord(boolean save) {
        Camera2Tools.getInstance().stopRecord(save);
    }

    @Override
    public void getMediaData(int mediatype, String mediaPath) {
        Intent intent = new Intent(getActivity(),TakepicActivity.class);
        intent.putExtra("path",mediaPath);
        intent.putExtra("type",0);
        getActivity().startActivity(intent);
    }

    @Override
    public void getNv21Data(byte[] nv21, String uuid, int width, int height) {

    }


    @Override
    public void getVideoData(String mediaPath) {
        Intent intent = new Intent(getActivity(),TakepicActivity.class);
        intent.putExtra("path",mediaPath);
        intent.putExtra("type",1);
        getActivity().startActivity(intent);
    }



    @Override
    public void onResume() {
        super.onResume();
        Camera2Tools.getInstance().resume(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Camera2Tools.getInstance().stop();
    }
}
