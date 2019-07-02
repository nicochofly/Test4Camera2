package cho.nico.com.test;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * 按钮
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2ShutterBtn extends View {


    //00DBDB  绿色
    //ECECEC  背景透明色
    private int outcirclecolor = 0xFF888888;
    private int innercirclecolor = 0xFF00DBDB;
    private int progresscolor = 0xffff4060;

    //点击控件的时间
    long downMillus = 0;

    Handler handler = new Handler();

    private int viewWidth, viewHeight;
    private int centerX, centerY;


    private int duration;

    private RectF rectF;


    CountDownTimer countDownTimer;

    private int outscale = 50;

    private int inscale = 30;

    private int progresswidth = 10;

    private int marign;

    //外环和内环的大小
    float out, in, origin;

    //
    float progressAngle = 0;
    /**
     * 0 拍照
     * 1 录影
     * -1 初始化
     */
    private int state = 0;

    private CameraOprCallback callback;

    private int captureType = 0;

    public Camera2ShutterBtn(Context context) {
        super(context);


    }

    public Camera2ShutterBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray t = getContext().obtainStyledAttributes(attrs,
                R.styleable.Camera2ShutterBtn);
        duration = t.getInt(R.styleable.Camera2ShutterBtn_duration, 15);
        outcirclecolor = t.getColor(R.styleable.Camera2ShutterBtn_backgroudcolor, outcirclecolor);
        innercirclecolor = t.getColor(R.styleable.Camera2ShutterBtn_innercirclecolor, innercirclecolor);
        progresscolor = t.getColor(R.styleable.Camera2ShutterBtn_progresscolor, progresscolor);
        progresswidth = t.getDimensionPixelOffset(R.styleable.Camera2ShutterBtn_progresswidth, progresswidth);
        marign = (int) t.getDimension(R.styleable.ConstraintSet_android_layout_marginLeft, 0);
        t.recycle();
    }

    public Camera2ShutterBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray t = getContext().obtainStyledAttributes(attrs,
                R.styleable.Camera2ShutterBtn);

        duration = t.getInt(R.styleable.Camera2ShutterBtn_duration, 15);
        outcirclecolor = t.getColor(R.styleable.Camera2ShutterBtn_backgroudcolor, outcirclecolor);
        innercirclecolor = t.getColor(R.styleable.Camera2ShutterBtn_innercirclecolor, innercirclecolor);
        progresscolor = t.getColor(R.styleable.Camera2ShutterBtn_progresscolor, progresscolor);

        progresswidth = t.getInt(R.styleable.Camera2ShutterBtn_progresswidth, progresswidth);
        t.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Log.e(getClass().getSimpleName(), "onMeasure");

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        centerX = (int) (viewWidth * 0.5f);
        centerY = (int) (viewHeight * 0.5f);

        origin = (viewWidth - marign * 2) / 3;
        out = origin;
        in = (origin - inscale);

        outscale = viewWidth / 6;
        inscale = outscale / 2;


        float left = (viewWidth / 2 - out - (outscale) + progresswidth / 2);
        float top = (viewHeight / 2 - out - (outscale) + progresswidth / 2);
        float right = (viewWidth / 2 + out + (outscale) - progresswidth / 2);
        float bottom = (viewHeight / 2 + out + (outscale) - progresswidth / 2);
        rectF = new RectF(left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.e(getClass().getSimpleName(), "onDraw");
        Paint paint = new Paint();

        paint.setColor(outcirclecolor);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, out, paint);
        paint.reset();
        paint.setColor(innercirclecolor);
        paint.setAntiAlias(true);
        canvas.drawCircle(centerX, centerY, in, paint);


        if (state == 1) {
            paint.reset();
            paint.setColor(progresscolor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(progresswidth);
            paint.setAntiAlias(true);
            canvas.drawArc(rectF, -90, progressAngle, false, paint);
        }

    }


    /**
     * 开始录影
     */
    private void startRecordAnim() {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(out, out + outscale);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(in, in - inscale);

        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                out = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                in = (float) animation.getAnimatedValue();

                invalidate();
            }
        });


        AnimatorSet set = new AnimatorSet();
        //当动画结束后启动录像Runnable并且回调录像开始接口


        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                countDownTimer = new CountDownTimer(duration * 1000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
//                        int left = (int) ((duration) - millisUntilFinished / 1000);  //这边+1 因为millisUntilFinished总是比整秒数少几毫秒，类似1998，2999，这样导致进度圆画不全
//                        progressAngle = ((360 * 1.0f / duration) * (left));
                        int left = (int) ((duration * 1000) - millisUntilFinished);  //这边+1 因为millisUntilFinished总是比整秒数少几毫秒，类似1998，2999，这样导致进度圆画不全
                        progressAngle = ((360 * 1.00f / (duration * 1000)) * (left));
                        invalidate();
                    }

                    @Override
                    public void onFinish() {
                        progressAngle = 360 * 1.0f;
                        invalidate();


                        MotionEvent upEvent = MotionEvent.obtain(downMillus, System.currentTimeMillis(), MotionEvent.ACTION_UP, centerX, centerY, 0);
                        dispatchTouchEvent(upEvent);
//                        resetAnim();
                    }


                };

                countDownTimer.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.playTogether(outside_anim, inside_anim);
        set.setDuration(200);
        set.start();
    }


    /**
     * 按钮复位
     */
    private void resetAnim() {
        Log.e(getClass().getSimpleName(), "resetAnim");
        state = -1;
        ValueAnimator outside_anim = ValueAnimator.ofFloat(out, origin);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(in, (origin - inscale));
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                out = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                in = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        AnimatorSet set = new AnimatorSet();
        //当动画结束后启动录像Runnable并且回调录像开始接口

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                progressAngle = 0;
                countDownTimer.cancel();
                consume = consume & true;
                if (consume) {
                    saveVideo();
                } else {
                    deleteVideo();
                }
//                processhasFinish = true;
                postInvalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.playTogether(outside_anim, inside_anim);
        set.setDuration(200);
        set.start();
    }

    boolean consume = true;

//    boolean processhasFinish = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downMillus = System.currentTimeMillis();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        state = 1;
                        startRecordAnim();
                        startRecord();
                    }
                }, 500);
                break;

            case MotionEvent.ACTION_UP:
                long aa = System.currentTimeMillis() - downMillus;
                if (aa <= 500) {
                    //短按
                    handler.removeCallbacksAndMessages(null);
                    takePic();
                } else {
                    if (aa < 3000) {
                        //恢复 并删除视频
//                        resetAnim();
//                        deleteVideo();
                        consume = false;
                    } else {
                        //长按
//                        saveVideo();
                        consume = true;
                    }
                    resetAnim();
                }
                break;
        }
        return true;
    }


    public void setCameraOprCallback(CameraOprCallback cameraOprCallback) {
        callback = cameraOprCallback;
    }

    /**
     * record time too short , delete it
     */
    private void deleteVideo() {
        if (callback != null) {
            callback.stopRecord(false);
        }
    }


    /**
     * start record
     */
    private void startRecord() {
        if (callback != null) {
            callback.recordVideo();
        }
    }


    /**
     * save the video to sdcard
     */
    private void saveVideo() {
        if (callback != null) {
            callback.stopRecord(true);
        }
    }

    /**
     * take pic
     */
    private void takePic() {
        if (callback != null) {
            callback.capturePic();
        }
    }

    public void setCaptureType(int captureType) {
        this.captureType = captureType;
    }


}
