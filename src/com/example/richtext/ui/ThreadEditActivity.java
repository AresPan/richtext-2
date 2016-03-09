package com.example.richtext.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.richtext.R;
import com.example.richtext.bean.DraftInfo;
import com.example.richtext.dao.DraftDao;
import com.example.richtext.util.LogUtils;
import com.example.richtext.util.PhotoUtilChange;
import com.example.richtext.view.DraftPopupWindow;
import com.example.richtext.view.SelectPhotoPopupWindow;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class ThreadEditActivity extends ActionBarActivity implements OnClickListener {
	/**
	 * 内容的edittext
	 */
	@ViewInject(R.id.editContent)
	private EditText etContent;
	/**
	 * 图片选择的按钮，点击时唤起系统的相册
	 */
	@ViewInject(R.id.pic_select)
	private ImageView ivPicSelect;
	/**
	 * 点击时打开小键盘
	 */
	@ViewInject(R.id.keyboard_select)
	private ImageView ivKeyBoard;
	/**
	 * 帖子的核心布局，用于侦听小键盘的开启来选择对应的布局
	 */
	@ViewInject(R.id.editor_base_content)
	private LinearLayout baseContent;
	/**
	 * 标题的edittext
	 */
	@ViewInject(R.id.et_title_post)
	private EditText  etTitle;
	/**
	 * 发表帖子页面的蒙层。蒙层要做到的效果是这样的
	 * 1，点击图片选择按钮或者退出按钮弹出对画框时显示蒙层。
	 * 2，当对话框显示是点击蒙层对话框消失，蒙层也消失 。
	 * 3，当对对话框的相关按钮点击之后对画框消失，蒙层也消失。
	 */
	@ViewInject(R.id.viewMask)
	private View mViewMask;
	/**
	 * 两个弹出的popWindow
	 */
	private SelectPhotoPopupWindow selectPhotoPopupWindow;
	private DraftPopupWindow       mDraftPopupWindow;
	/**
	 * 包裹内容编辑区域的ScrollView
	 */
	@ViewInject(R.id.post_scrollview)
	private ScrollView mScrollView;
	/**
	 * 开辟一个操作UI的线程
	 */
	private Handler mHandler;
	/**
	 * 申请危险权限是用的一个常量
	 */
	private static final int EXTERNAL_STORAGE_REQ_CODE = 200 ;
	/**
	 * 获得存储图片的路径
	 */
	private String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String APK_PATH = Environment.getExternalStorageDirectory()+"/RICHTEXT/";
	/**
	 * 打开相机和相册是的回调标示
	 */
	private final int INSERTIMG_CODE = 502;
	private final int PictureButtonCode = 504;
	/**
	 * 保存照相机形成的图片的路径的变量
	 */
	private String mCameraPath;
	/**
	 * 记录屏幕的宽高的两个变量
	 */
	private int screeHeight;
	private static int screeWidth;
	/**
	 * 来自展示富文本的listView的item的点击事件
	 */
	private boolean   mDraftOrigin = false;
	/**
	 * 存储富文本的数据库
	 */
	private DraftDao mDraftDao;
	/**
	 * 接受有草稿箱传过来的信息
	 * 1，标记来自于草稿箱
	 * 2，记录草稿箱的信息
	 */
	private DraftInfo mDraftInfo;
	private ActionBar				mActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_threadedit);
		ViewUtils.inject(this);
		mHandler = new Handler();
		/** DisplayMetrics获取屏幕信息 */
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		screeWidth = displayMetrics.widthPixels;
		screeHeight = displayMetrics.heightPixels;
		mDraftDao = new DraftDao(this);
		initListener();
		initActionBar();
		/**
		 * 如果是安卓6.0一些权限需要我们自己在运行的时候申请
		 */
		if(Build.VERSION.SDK_INT >= 23){
			requestPermission();
		}
		File apkFile = new File(APK_PATH);
		if (!apkFile .exists()) {
		LogUtils.d("创建文件夹");
		apkFile.mkdirs();
		 }
		Editable eb = etContent.getEditableText();
		
		mDraftOrigin = getIntent().getBooleanExtra("draft_origin", false);
		if(mDraftOrigin){
			mDraftInfo = (DraftInfo) getIntent().getSerializableExtra("draft_bean");
			/**
			 * 获得标题，并且把标题显示在标题栏
			 */
			etTitle.setText(mDraftInfo.title);
			String content  =  mDraftInfo.text;
			LogUtils.d("获得的text信息"+content);
			String[] mContent = content.split("\n");
			int startPosition;
			/**
			 * 获得html中的图片的URI，存储在容器中mPicUri
			 */
			int startIndex;
			int endIndex;
			List<String> mPicUri = new ArrayList<String>();
			String [] mContentEb = mDraftInfo.html.split("\n");
			for (String string : mContentEb) {
				if(string.contains("src=")){
					LogUtils.d(string);
					startIndex = string.indexOf("src=")+5;
					endIndex   = string.lastIndexOf("jpg")+3;
					String uri = string.substring(startIndex, endIndex);
					LogUtils.d("11111获得的图片路径"+uri);
					mPicUri.add(uri);
					
				}
				
			}
			int i = 0;
			/**
			 * 获得草稿的图片和文字信息，并把它展示出来
			 */
			for (String string : mContent) {
				//LogUtils.d(string);
				if(string.contains("￼")){
					/**
					 * 如果是图片
					 * 1,先判断其来自与照相机还是图库
					 * 2，图库用ImageGetter显示出来
					 * 3，照片用ImageGetter1显示出来
					 */
					startPosition = etContent.getSelectionStart();
					String mPicPath  =  mPicUri.get(i);
					startPosition = etContent.getSelectionStart();
					if(mPicPath.contains("content")){
						eb.insert(
								startPosition,
								Html.fromHtml("<br/><img src=" + mPicPath
												+ "><br/>", imageGetter, null));
					}else{
						eb.insert(
								startPosition,
								Html.fromHtml("<br/><img src=" + mPicPath 
												+ "><br/>", imageGetter, null));
					}
					i++;
					/**
					 * 碰到图片下面直接有文字的情况
					 */
					if(!string.equals("￼")){
					String contentPic	= string.substring(string.indexOf("￼")+1);
					startPosition = etContent.getSelectionStart();
					eb.insert(startPosition, contentPic);
					}
				}else{
					/**
					 * 展示文字
					 */
					startPosition = etContent.getSelectionStart();
					eb.insert(startPosition, string);
				}
			}
			
		}
	}
	
	 private void initActionBar() {
		// 对导航栏进行操作
		mActionBar = getSupportActionBar();
		
		
	}

	@Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.threadedit, menu);
	        return true;
	    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 导航栏的点击事件
		switch (item.getItemId()) {
		case R.id.action_save:
			saveDraft();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void requestPermission() {
		// 申请文件的读取权限

		// TODO Auto-generated method stub
		  //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"please give me the permission",Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"please give me the permission",Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            }
        }
	
	}
	/**
	 * 给布局添加侦听事件
	 */
	private void initListener() {
		ivPicSelect.setOnClickListener(this);
		ivKeyBoard.setOnClickListener(this);
		mViewMask.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		/**
		 * 图片选择的按钮点击事件
		 */
		case R.id.pic_select:
			//强制关闭
			InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//2.调用hideSoftInputFromWindow方法隐藏软键盘
			imm1.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
			mViewMask.setVisibility(View.VISIBLE);
			
			gallery();	
			
			break;
		case R.id.keyboard_select:
			//1.得到InputMethodManager对象
			InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			//2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
			imm2.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			//在键盘出现的时候内容编辑框滑动之最低端
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
				
			});
			break;
		case R.id.btn_upload_pic_popupwindows_cancel:
			selectPhotoPopupWindow.dismiss();
			/**
			 * 对话框消失蒙层也消失
			 */
			mViewMask.setVisibility(View.GONE);
			break;
		case R.id.btn_upload_pic_popupwindows_camera:
			/**
			 * 弹出框中的相机按钮点击事件
			 */
			LogUtils.d("第一步发起拍照请求");
			File file = new File(APK_PATH + System.currentTimeMillis()+".jpg");
			//把拍照的图片路径赋值给mCameraPath
			mCameraPath = APK_PATH + System.currentTimeMillis()+".jpg";
			PhotoUtilChange.takePhoto(this, PictureButtonCode, file);
			selectPhotoPopupWindow.dismiss();
			/**
			 * 对话框消失蒙层也消失
			 */
			mViewMask.setVisibility(View.GONE);
			break;
		case R.id.btn_upload_pic_popupwindows_photo:
			/**
			 * 弹出框中的相册按钮点击事件
			 */
			LogUtils.d("第一步发起打开本地相册的请求");
			Intent intent1 = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent1, INSERTIMG_CODE);
			selectPhotoPopupWindow.dismiss();
			/**
			 * 对话框消失蒙层也消失
			 */
			mViewMask.setVisibility(View.GONE);
			break;
		case R.id.viewMask:
			/**
			 * 当对话框显示是点击蒙层对话框消失，蒙层也消失 。
			 */
			if(mDraftPopupWindow != null && mDraftPopupWindow.isShowing()){
				mDraftPopupWindow.dismiss();
			}
			if(selectPhotoPopupWindow != null && selectPhotoPopupWindow.isShowing()){
				selectPhotoPopupWindow.dismiss();
			}
			mViewMask.setVisibility(View.GONE);
			break;
		case R.id.btn_give_up:
			/**
			 * 放弃编辑点击事件的处理就是finish掉当前的activity，回退到上一个界面。
			 */
			finish();
			break;
		case R.id.btn_cancel_draft:
			/**
			 * 取消的点击事件的处理就是让弹框消失，重新回到编辑页面
			 */
			mDraftPopupWindow.dismiss();
			/**
			 * 对话框消失蒙层也消失
			 */
			mViewMask.setVisibility(View.GONE);
			break;
		case R.id.btn_save_draft:
			saveDraft();
			break;
		default:
			break;
		}
	}

	private void saveDraft() {
		/**
		 * 在保存草稿的时候要判断这次草稿的编辑页面是不是来自于草稿箱的item
		 * 的点击
		 */
		if(mDraftOrigin){
			String time = DateUtils.formatDateTime(getApplicationContext(),System.currentTimeMillis(),
					DateUtils.FORMAT_SHOW_DATE);
			Editable eb= etContent.getEditableText();
			String html = Html.toHtml(eb);
			String text = etContent.getText().toString();
			String title  = etTitle.getText().toString();
			boolean b = mDraftDao.add(time, html, text, title);
			LogUtils.d("数据插入成功"+b);
			boolean b1= mDraftDao.delete(mDraftInfo.id);
			LogUtils.d("数据删除成功"+b1);
		}else{
			
			String time = DateUtils.formatDateTime(getApplicationContext(),System.currentTimeMillis(),
					DateUtils.FORMAT_SHOW_DATE);
			Editable eb= etContent.getEditableText();
			String html = Html.toHtml(eb);
			String text = etContent.getText().toString();
			String title  = etTitle.getText().toString();
			Boolean b = mDraftDao.add(time, html, text,title);
			LogUtils.d("数据插入成功"+b);
		}
		/**
		 * 存储完之后弹框消失，回退到上一个界面。
		 */
		if(mDraftPopupWindow != null){
			
			mDraftPopupWindow.dismiss();
		}
		finish();
	}
	private void gallery() {
		// TODO Auto-generated method stub
		 selectPhotoPopupWindow = PhotoUtilChange.getPicPopupWindow(this, this,baseContent);
		 mViewMask.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		// 插入图片
		if (resultCode == RESULT_OK && requestCode == INSERTIMG_CODE) {
			final Uri uri = data.getData();
			final String url = getRealPathFromURI(uri);
			Editable eb = etContent.getEditableText();
			LogUtils.d("获得的文件绝对路径是"+url);
			// 获得光标所在位置
			int startPosition = etContent.getSelectionStart();
			eb.insert(
					startPosition,
					Html.fromHtml("<br/><img src='" + url
							+ "'/><br/>", imageGetter, null));
			/**
			 * 图片插入之后，scrillView下滑至最底部
			 */
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
				
			});
		}if (resultCode == RESULT_OK && requestCode == PictureButtonCode) {
			File file =new File(mCameraPath); 
			/**
			 * 如果照相机产生的图片过大对他进行压缩
			 */
			Bitmap btp = getLocalImage(file, 500, 500);
			compressImage(btp, file, 30);
			Editable eb = etContent.getEditableText();
			int startPosition = etContent.getSelectionStart();
			eb.insert(
					startPosition,
					Html.fromHtml("<br/><img src='" + mCameraPath 
									+ "'/><br/>", imageGetter, null));
			mHandler.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
				
			});
		}
		
	}
	/**
	 * 获得指定大小的文件
	 * @param file
	 * @param swidth
	 * @param sheight
	 * @return
	 */
	private Bitmap getLocalImage(File f, int swidth, int sheight) {

		File file = f;
		if (file.exists()) {
			try {
				file.setLastModified(System.currentTimeMillis());
				FileInputStream in = new FileInputStream(file);

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(in, null, options);
				int sWidth = swidth;
				int sHeight = sheight;
				int mWidth = options.outWidth;
				int mHeight = options.outHeight;
				int scale = 1;
				if (mWidth > mHeight && mWidth > sWidth) {
					scale = (int) (mWidth / sWidth);
				} else if (mHeight > mWidth && mHeight > sHeight) {
					scale = (int) (mHeight / sHeight);
				}
				//newOpts.inSampleSize = scale;// 设置缩放比例
				options = new BitmapFactory.Options();
				LogUtils.d("压缩率"+scale);
				/**
				 * 为了兼顾压缩之后的图片的质量我们在对获得的scale进行了一些操作
				 * 让他小一点
				 */
				options.inSampleSize = (int) Math.sqrt(scale);
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				options.inPurgeable = true;
				options.inInputShareable = true;
				try {
					// 4. inNativeAlloc 属性设置为true，可以不把使用的内存算到VM里
					BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(options, true);
				} catch (Exception e) {
				}
				in.close();
				// 再次获取
				in = new FileInputStream(file);
				Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
				in.close();
				return bitmap;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				System.gc();
				return null;
			}
		}
		return null;
	
	}
	
	/**
	 * 图片压缩 上传图片时建议compress为30
	 * 
	 * @param bm
	 * @param f
	 */
	public static void compressImage(Bitmap bm, File f, int compress) {
		if (bm == null)
			return;
		File file = f;
		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			OutputStream outStream = new FileOutputStream(file);
			bm.compress(android.graphics.Bitmap.CompressFormat.JPEG, compress, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 从URI中获得文件路径
	 * @param contentUri
	 * @return
	 */
	private String getRealPathFromURI(Uri contentUri) {
		// 从获得的URI 中获得绝对路径
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(getApplicationContext(),
				contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	/**
	 * 把图片转成为bitmap，并且在富文本中显示的一个类
	 */
	private ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			try {
				Bitmap bitmap = getImage(source);
				return getMyDrawable(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		
	};
	/**
	 * 获得指定大小的bitmap
	 * @param bitmap
	 * @return
	 */
	private static Drawable getMyDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		int imgWidth = screeWidth-20 ;
		int imgHeight = (int) (imgWidth /drawable.getIntrinsicWidth()*drawable.getIntrinsicHeight()+0.5f);
		drawable.setBounds(0, 0, imgWidth, imgHeight);
		return drawable;
	}
	
	/**
	 * 通过这个方法对图片进行一定程度的压缩
	 * @param source
	 * @return
	 */
	private Bitmap getImage(String source) {

		try {
			Bitmap bitmap = null;
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			newOpts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(source), null, newOpts);
			newOpts.inJustDecodeBounds = false;
			int imgWidth = newOpts.outWidth;
			int imgHeight = newOpts.outHeight;
			// 缩放比,1表示不缩放
			int scale = 1;

			if (imgWidth > imgHeight && imgWidth > screeWidth) {
				scale = (int) (imgWidth / screeWidth);
			} else if (imgHeight > imgWidth && imgHeight > screeHeight) {
				scale = (int) (imgHeight / screeHeight);
			}
			newOpts.inSampleSize = scale*2;// 设置缩放比例
			bitmap = BitmapFactory.decodeStream(new FileInputStream(source), null,
					newOpts);
			return bitmap;
		} catch (Exception e) {
			System.out.println("文件不存在");
			return null;
		}
	
	}
	/**
	 * 当点击实体回退键时，对这个点击事件作出的处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			/**
			 * 当点击实体的回退键的时候，首先判断对话框是否弹出，如弹出让对话框消失
			 * 如果没弹出弹出对话框
			 */
			if(selectPhotoPopupWindow != null && selectPhotoPopupWindow.isShowing()){
				selectPhotoPopupWindow.dismiss();
				mViewMask.setVisibility(View.GONE);
			}else{
				
				if(mDraftPopupWindow != null && mDraftPopupWindow.isShowing()){
					mDraftPopupWindow.dismiss();
					mViewMask.setVisibility(View.GONE);
				}else{
					if(TextUtils.isEmpty(etContent.getText())){
						finish();
					}else{
						showAlertDraftDialog();
					}
//					showAlertDraftDialog();
				}
			}
			/**
			 * 必须得加上return true来消费这个点击事件
			 */
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	private void showAlertDraftDialog() {
		/*
		 * 弹出保存草稿的对话框
		 */
	  mDraftPopupWindow = PhotoUtilChange.getDraftWindow(this, this,baseContent);
	  /**
	   * 弹框出现，蒙层也出现
	   */
	  mViewMask.setVisibility(View.VISIBLE);
	}
}
