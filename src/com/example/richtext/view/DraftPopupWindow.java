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
public class DraftPopupWindow extends PopupWindow {
	private Button btn_save_draft, btn_give_up, btn_cancel_draft;

	public DraftPopupWindow(Context context, OnClickListener onClickListener) {
		View contentView = View.inflate(context, R.layout.draft_popupwindows, null);
		
		this.setContentView(contentView);
		this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		/**点击popupWindow范围以外的地方,让popupWindow消失*/
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(new BitmapDrawable());
		//找到对应的控件
		btn_save_draft = (Button) contentView.findViewById(R.id.btn_save_draft);
		btn_give_up = (Button) contentView.findViewById(R.id.btn_give_up);
		btn_cancel_draft = (Button) contentView.findViewById(R.id.btn_cancel_draft);
		
		btn_give_up.setOnClickListener(onClickListener);
		btn_save_draft.setOnClickListener(onClickListener);
		btn_cancel_draft.setOnClickListener(onClickListener);
		//加入动画
		this.setAnimationStyle(R.style.AnimBottom);

	}
}
