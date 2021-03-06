package com.example.richtext.view;



import com.example.richtext.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
/**
 * 
 * @author lijianchang
 *
 */
public class SelectPhotoPopupWindow extends PopupWindow {
	private Button btn_take_photo, btn_pick_photo, btn_cancel;

	public SelectPhotoPopupWindow(Context context, OnClickListener onClickListener) {
		View contentView = View.inflate(context, R.layout.picselect_popupwindows, null);
		this.setContentView(contentView);
		this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		/**点击popupWindow范围以外的地方,让popupWindow消失*/
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(new BitmapDrawable());
		//找到对应的控件
		btn_take_photo = (Button) contentView.findViewById(R.id.btn_upload_pic_popupwindows_camera);
		btn_pick_photo = (Button) contentView.findViewById(R.id.btn_upload_pic_popupwindows_photo);
		btn_cancel = (Button) contentView.findViewById(R.id.btn_upload_pic_popupwindows_cancel);
		
		btn_take_photo.setOnClickListener(onClickListener);
		btn_pick_photo.setOnClickListener(onClickListener);
		btn_cancel.setOnClickListener(onClickListener);
		//加入动画
		this.setAnimationStyle(R.style.AnimBottom);

	}
}
