package cho.nico.com.test;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.myapplication.R;


/**
 * 通用确认对话框
 * 
 * @author dqcao
 * 
 */
public class TipsDialog extends Dialog {

	private Context context;
    private TextView titleName;



	public TipsDialog(Context context)
	{
		super(context, R.style.alert_dialog);
		this.context = context;
		LayoutInflater li = LayoutInflater.from(context);
		ViewGroup contentView = (ViewGroup) li.inflate(
				R.layout.dialog_tips1, null);
		contentView.bringToFront();

		titleName = (TextView) contentView.findViewById(R.id.tv_title_name);


		setCanceledOnTouchOutside(false);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;
		onWindowAttributesChanged(lp);
		setContentView(contentView);


	}




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = 510;
		lp.height = (int) (getScreenHeight(getContext()) * 0.35f);
		lp.gravity = Gravity.CENTER;
		getWindow().setAttributes(lp);
	}

	public void setTitleMsg(String msg) {
		if (titleName != null) {
			titleName.setText(msg);
		}
	}

	public void setErrorImage()
	{
		if (titleName != null) {

			Drawable top = getContext().getResources().getDrawable(R.mipmap.detection_failure);
			titleName.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);
		}


	}

	public  int getScreenWidth(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
		windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
		return outMetrics.widthPixels;
	}


	public  int getScreenHeight(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
		windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
		return outMetrics.heightPixels;
	}


}
