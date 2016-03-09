package com.example.richtext.ui;

import java.util.ArrayList;
import java.util.List;

import com.example.richtext.R;
import com.example.richtext.R.id;
import com.example.richtext.bean.DraftInfo;
import com.example.richtext.dao.DraftDao;
import com.example.richtext.util.LogUtils;
import com.example.richtext.util.PhotoUtilChange;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;


@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity  {
	/**
	 * 显示富文本item的listView
	 * 
	 */
	@ViewInject(R.id.lv_topic)
	private ListView mListView;
	/**
	 * 添加新的富文本的按钮
	 */
	@ViewInject(R.id.iv_add)
	private ImageView mIvAdd;
	/**
	 * 显示空白的图片
	 */
	
	@ViewInject(R.id.iv_empty)
	private ImageView mIvEmpty;
	/**
	 * 数据库的操作类
	 */
	private DraftDao mDao;
	/**
	 * 存储数据库查询的信息的数据集合
	 */
	private List<DraftInfo> mList ;
	/**
	 * 存储数据库查询的信息的数据集合的逆序列
	 * 也就是展示在listView的数据
	 */
	private List<DraftInfo> mListShow ;
	/**
	 * listView所关联的adapter
	 * 
	 */
	private DraftAdapter mDraftAdaptr;
	/**
	 * 来确定获取图片大小的屏幕宽高
	 */
	private int 				 screenWidth;
	private int     			 screenHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        /**
		 * 获取得到屏幕的信息
		 */
		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		mList = new ArrayList<DraftInfo>();
		mListShow = new ArrayList<DraftInfo>();
		mDao = new DraftDao(this);
		mList = mDao.findAll();
		 /**
		 * 如果没有数据,显示空白的图
		*/
			
		if(mList.size() == 0){
			mIvEmpty.setVisibility(View.VISIBLE);
		}else{
			for(int i = mList.size()-1;i>=0;i--){
			 mListShow.add(mList.get(i));
			}
		}
        /**
         * 点击添加新的帖子的时候跳转至帖子编辑activity
         */
        mIvAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ThreadEditActivity.class);
				startActivity(intent);
			}
		});
    
   
		/**
		 * 展示草稿数据的adapter
		 */
		mDraftAdaptr = new DraftAdapter();
		mListView.setAdapter(mDraftAdaptr);
    }
   
    /**
     * 展示富文本的adapter
     * @author lijianchang
     *
     */
    private  class  DraftAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListShow.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mListShow.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 *  对单个条目进行布局
			 */
			ViewHolder_Draft mHolder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_draft, null);
				mHolder = new ViewHolder_Draft(convertView);
			}else{
				mHolder = (ViewHolder_Draft) convertView.getTag();
			}
			final DraftInfo mDraftBean = mListShow.get(position);
			final int position1 = position;
			mHolder.tv_times.setText(mDraftBean.time);
			mHolder.tv_title.setText(mDraftBean.title);
			/**
			 * 对mDraftBean.text进行处理获得内容,除却文字信息就是图片信息
			 */
			String content = "";
			String[] mContent = mDraftBean.text.split("\n");
			for (String string : mContent) {
				
				if(!string.contains("￼")){
					content +=string;
			} 
			}
			mHolder.tv_content.setText(content);
			/**
			 * 通过对html的文件解析出图片的路径，之后显示在item上面
			 */
			/**
			 * 做两个开始和结束的标识符
			 */
			int startIndex;
			int endIndex;
			String content_eb = mDraftBean.html;
			List<String> mPicUri = new ArrayList<String>();
			String [] mContentEb = content_eb.split("\n");
			for (String string : mContentEb) {
				if(string.contains("src=")){
					try {
					LogUtils.d(string);
					startIndex = string.indexOf("src=")+5;
					endIndex   = string.lastIndexOf("jpg")+3;
					String url = string.substring(startIndex, endIndex);
					LogUtils.d("11111获得的图片路径"+url);
					mPicUri.add(url);
					} catch (Exception e) {
						
						
					}
				}
				
			}
			mHolder.iv_image1.setVisibility(View.VISIBLE);
			mHolder.iv_image2.setVisibility(View.VISIBLE);
			mHolder.iv_image3.setVisibility(View.VISIBLE);
			mHolder.iv_image1.setScaleType(ScaleType.FIT_XY);
			mHolder.iv_image2.setScaleType(ScaleType.FIT_XY);
			mHolder.iv_image3.setScaleType(ScaleType.FIT_XY);
			if(mPicUri.size() == 0){
				mHolder.iv_image1.setVisibility(View.GONE);
				mHolder.iv_image2.setVisibility(View.GONE);
				mHolder.iv_image3.setVisibility(View.GONE);
			}else if(mPicUri.size() == 1){
				mHolder.iv_image2.setVisibility(View.INVISIBLE);
				mHolder.iv_image3.setVisibility(View.INVISIBLE);
				Bitmap bitmap = PhotoUtilChange.getImage(mPicUri.get(0), MainActivity.this);
				Drawable bp1 = getMyDrawable(bitmap, screenWidth/3-20);
				mHolder.iv_image1.setImageDrawable(bp1);
			
				
			}else if(mPicUri.size() == 2){
				Bitmap bitmap = PhotoUtilChange.getImage(mPicUri.get(0), MainActivity.this);
				Drawable bp1 = getMyDrawable(bitmap, screenWidth/3-20);
				mHolder.iv_image1.setImageDrawable(bp1);
				Bitmap bitmap1 = PhotoUtilChange.getImage(mPicUri.get(1), MainActivity.this);
				Drawable bp2 = getMyDrawable(bitmap1, screenWidth/3-20);
				mHolder.iv_image2.setImageDrawable(bp2);
				mHolder.iv_image3.setVisibility(View.INVISIBLE);
			}else{
				Bitmap bitmap = PhotoUtilChange.getImage(mPicUri.get(0), MainActivity.this);
				Drawable bp1 = getMyDrawable(bitmap, screenWidth/3-20);
				mHolder.iv_image1.setImageDrawable(bp1);
				Bitmap bitmap1 = PhotoUtilChange.getImage(mPicUri.get(1), MainActivity.this);
				Drawable bp2 = getMyDrawable(bitmap1, screenWidth/3-20);
				mHolder.iv_image2.setImageDrawable(bp2);
				Bitmap bitmap2 = PhotoUtilChange.getImage(mPicUri.get(2), MainActivity.this);
				Drawable bp3 = getMyDrawable(bitmap2, screenWidth/3-20);
				mHolder.iv_image3.setImageDrawable(bp3);
			}
			/**
			 * 删除图标的点击事件
			 */
			mHolder.zone_click.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				int id	= mDraftBean.id;
				boolean b= mDao.delete(id);
				LogUtils.d("删除成功"+b);
				mList.remove(position1);
				mListShow.remove(position1);
				if(mList.size() == 0){
					/**
					 * 当数据全部删除时，listView显示，显示空白的图片出现
					 */
					mListView.setVisibility(View.GONE);
					mIvEmpty.setVisibility(View.VISIBLE);
				}
				notifyDataSetChanged();
				}
			});
			/**
			 * 条目的点击事件，跳转至postMessageActivity
			 * 传递的参数是
			 * 1，草稿箱的item显示的所有信息
			 * 2，来自于草稿箱的标记
			 */
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,ThreadEditActivity.class);
					intent.putExtra("draft_bean",mDraftBean );
					intent.putExtra("draft_origin", true);
					startActivity(intent);
//					finish();
				}
			});
			return convertView;
		}
    	
    }
    
    private class ViewHolder_Draft
	{
		

			View convertView;
			@ViewInject(R.id.tv_common_times)
			TextView tv_times;

			@ViewInject(R.id.tv_common_title)
			TextView tv_title;

			@ViewInject(R.id.iv_image1)
			ImageView iv_image1;

			@ViewInject(R.id.iv_image2)
			ImageView iv_image2;

			@ViewInject(R.id.iv_image3)
			ImageView iv_image3;

			@ViewInject(R.id.tv_common_content)
			TextView tv_content;

			@ViewInject(R.id.tv_from)
			TextView tv_from;

			@ViewInject(R.id.zone_click)
			View zone_click;

			public ViewHolder_Draft(View convertView) {
				//
				this.convertView = convertView;
				ViewUtils.inject(this, convertView);
				// 把当前的hodler设置到标签中
				convertView.setTag(this);
			
		  }
	}
    
    /**
	 * 获得指定大小的bitmap
	 * @param bitmap
	 * @return
	 */
	private static Drawable getMyDrawable(Bitmap bitmap,int width) {
		Drawable drawable = new BitmapDrawable(bitmap);
		int imgWidth = width-20 ;
		int imgHeight = 210;
		drawable.setBounds(0, 0, imgWidth, imgHeight);
		return drawable;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(mDao != null){
			mList = mDao.findAll();
			mListShow.clear();
			 /**
			 * 如果没有数据,显示空白的图
			*/
				
			if(mList.size() == 0){
				mIvEmpty.setVisibility(View.VISIBLE);
			}else{
				for(int i = mList.size()-1;i>=0;i--){
				 mListShow.add(mList.get(i));
				 mIvEmpty.setVisibility(View.GONE);
				}
		}
			mDraftAdaptr.notifyDataSetChanged();
		}
		super.onResume();
	}
}
