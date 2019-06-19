package cho.nico.com.test;

import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.camera2lib.Camera2Utils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraFragment extends Fragment implements View.OnClickListener, CameraOprCallback,CameraResultCallback {


    private View view;

    private AutoFitTextureView cameraTexture;

    private ImageView flipIv;

    private CaptureButton captureButton;

    public static CameraFragment newInstance() {

        Bundle args = new Bundle();

        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera, container, false);
        cameraTexture = view.findViewById(R.id.main_texture);
        flipIv = view.findViewById(R.id.flip_iv);
        flipIv.setOnClickListener(this);
        captureButton = view.findViewById(R.id.btn);
        captureButton.setCameraOprCallback(this);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            Camera2Utils1.getInstance().init(getContext(), cameraTexture);
            Camera2Utils1.getInstance().setResultCallback(this);
//            Camera2Utils1.getInstance().startPreview();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.flip_iv:
                try {
                    Camera2Utils1.getInstance().flip();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    @Override
    public void capturePic() {
        Camera2Utils1.getInstance().takePicture();
    }

    @Override
    public void recordVideo() {
        Camera2Utils1.getInstance().startRecord();
    }

    @Override
    public void stopRecord(boolean save) {
        Camera2Utils1.getInstance().stopRecord(save);
    }


    @Override
    public void getMediaData(int mediatype, String mediaPath) {

        Intent intent = new Intent(getActivity(), CaptureResultActivity.class);
        intent.putExtra("path", mediaPath);
        intent.putExtra("type", mediatype);
        getActivity().startActivity(intent);
    }

    @Override
    public void getNv21Data(byte[] bytes) {

    }


}
