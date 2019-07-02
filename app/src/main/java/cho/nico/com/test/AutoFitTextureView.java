/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cho.nico.com.test;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

import cho.nico.com.test.callback.CameraViewTouchCallback;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AutoFitTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    public float finger_spacing = 0;
    public int zoom_level = 1;


    CameraViewTouchCallback callback;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTouchCallback(CameraViewTouchCallback c) {
        callback = c;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (callback != null) {
            try {
                CameraCharacteristics characteristics = callback.getCameraCharacteristics();

                float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;

                Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                int action = event.getAction();
                float current_finger_spacing;

                if (event.getPointerCount() > 1) {
                    // Multi touch logic
                    current_finger_spacing = getFingerSpacing(event);
                    if (finger_spacing != 0) {
                        if (current_finger_spacing > finger_spacing && maxzoom > zoom_level) {
                            zoom_level++;
                        } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                            zoom_level--;
                        }
                        int minW = (int) (m.width() / maxzoom);
                        int minH = (int) (m.height() / maxzoom);
                        int difW = m.width() - minW;
                        int difH = m.height() - minH;
                        int cropW = difW / 100 * (int) zoom_level;
                        int cropH = difH / 100 * (int) zoom_level;
                        cropW -= cropW & 3;
                        cropH -= cropH & 3;
                        Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
//                    mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

                        callback.setCropRegion(zoom);
                    }
                    finger_spacing = current_finger_spacing;
                } else {
                    if (action == MotionEvent.ACTION_UP) {
                        //single touch logic
                    }
                }

                try {
//                previewSession.setRepeatingRequest(mPreviewRequest,
//                        mPreviewCaptureCallback, backgroudHandler);
                    callback.setRepeatingRequest();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            } catch (CameraAccessException e) {
                throw new RuntimeException("can not access camera.", e);
            }
        }
        return true;
    }


    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
