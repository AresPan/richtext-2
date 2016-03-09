package com.example.richtext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.example.richtext.view.DraftPopupWindow;
import com.example.richtext.view.SelectPhotoPopupWindow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/***
 * 修改版
 * 头像上传工具类 调用 getPhoto 在onactivityResult 调用
 * onPhotoFromCamera
 * onPhotoFromPick
 */
public class PhotoUtilChange {
	private static String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ZEALER/";
	/**
	 * 因为处理不同
	 * 
	 * @param takePhotoCode
	 *            Uri originalUri = data.getData();
	 *            image=ImageUtil.getBitmapFromUrl(originalUri.toString());
	 ********************************************************************************** 
	 * @param pickPhotoCode
	 *            Bundle extras = data.getExtras(); image = (Bitmap)
	 *            extras.get("data");
	 * @param tempFile
	 *            拍照时的临时文件 需要zoom时
	 * **/
	public static boolean getPhotoDialog(final Activity activity, final int takePhotoCode, final int pickPhotoCode,
			final File tempFile) {
		final CharSequence[] items = { "相册", "拍照" };
		AlertDialog dlg = new AlertDialog.Builder(activity).setTitle("选择图片")
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 1) {
							Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
							getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
							activity.startActivityForResult(getImageByCamera, takePhotoCode);
						} else {
							Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
							getImage.addCategory(Intent.CATEGORY_OPENABLE);
							getImage.setType("image/jpeg");
							activity.startActivityForResult(getImage, pickPhotoCode);
						}
					}
				}).create();
		dlg.show();
		return true;
	}

	public static SelectPhotoPopupWindow getPicPopupWindow(Context context, OnClickListener itemsOnClick,
			View viewAttach) {
		//实例化SelectPicPopupWindow
		SelectPhotoPopupWindow menuWindow = new SelectPhotoPopupWindow(context, itemsOnClick);
		//显示窗口
		try {
			
			menuWindow.showAtLocation(viewAttach, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
		} catch (Exception e) {
			// TODO: handle exception
			menuWindow.showAtLocation(viewAttach, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置

		}
		return menuWindow;
	}
	
	/**
	 * 
	 * @param context
	 * @param itemsOnClick
	 * @param viewAttach
	 * @return 退出发帖的时候选择是否保存草稿的popWindow
	 */
	public static DraftPopupWindow getDraftWindow(Context context, OnClickListener itemsOnClick,
			View viewAttach) {
		//实例化SelectPicPopupWindow
		DraftPopupWindow menuWindow = new DraftPopupWindow(context, itemsOnClick);
		//显示窗口
		try {
			
			menuWindow.showAtLocation(viewAttach, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
		} catch (Exception e) {
			// TODO: handle exception
			menuWindow.showAtLocation(viewAttach, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置

		}
		return menuWindow;
	}

	public static boolean takePhoto(Activity activity, int takePhotoCode, File tempFile) {
		Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
		getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
		LogUtils.d("第二步：打开摄像头，发起要求返回字节码takePhotoCode的请求,并把文件存储在tempFile1");
		activity.startActivityForResult(getImageByCamera, takePhotoCode);
		return true;
	}

	public static boolean pickPhoto(Activity activity, int imageCode, File tempFile) {
		//Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
		Intent getImage = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		getImage.addCategory(Intent.CATEGORY_OPENABLE);
//		getImage.setType("image/jpeg");
		LogUtils.d("第二步：打开图片库，获得图片，并要求返回字节码");
		activity.startActivityForResult(getImage, imageCode);
		
		return true;
	}

	/**
	 * 拍照获取图片的方式 用于切割的图片大小被限制在500,500
	 * 
	 * @param context
	 * @param zoomCode
	 * @param temppath
	 *            拍照前生成的临时路劲
	 * @param angle 
	 * @return 新的路劲
	 */
	public static String onPhotoFromCamera(final Activity context, final int zoomCode, final String temppath,
			final int aspectX, final int aspectY, int angle) {
		try {
			Bitmap btp = getLocalImage(new File(sdCardPath + "/" + "tempFile1.jpg"), 500, 500,angle);
			compressImage(btp, new File(temppath + "temp.jpg"), 30);
			LogUtils.d("第四步：把通过照相机摄像生成的图片压缩存在+"+temppath+" + temp.jpg中");
			photoZoom(context, Uri.fromFile(new File(temppath + "temp.jpg")), Uri.fromFile(new File(temppath)),
					zoomCode, aspectX, aspectY);
			
		} catch (Exception e) {
			Toast.makeText(context, "图片加载失败", 1000).show();
		}

		return temppath;
	}

	/**
	 * 图片切割完调用 如果还需要 Bitmap 调用getLocalImage
	 * 
	 * @param path
	 * @param rw
	 * @param rh
	 * @param compress
	 * @return
	 */
	public static File onPhotoZoom(String path, int rw, int rh, int compress) {

		File f = new File(path);
		Bitmap btp = PhotoUtilChange.getLocalImage(f, rw, rh,0);
		compressImage(btp, f, compress);
		return f;
	}

	/**
	 * 相册获取图片,用于切割的图片大小被限制在500,500
	 * 
	 * @param context
	 * @param zoomCode
	 * @param temppath
	 *            希望生成的路劲
	 * @param data
	 */
	public static void onPhotoFromPick(final Activity context, final int zoomCode, final String temppath,
			final Intent data, final int aspectX, final int aspectY) {
		try {
			LogUtils.d("第四步，获得从图库中的压缩出来的图片并存在指定路径中");
			Bitmap btp = checkImage(context, data);
			compressImage(btp, new File(temppath + "temp.jpg"), 30);
			PhotoUtilChange.photoZoom(context, Uri.fromFile(new File(temppath + "temp.jpg")),
					Uri.fromFile(new File(temppath)), zoomCode, aspectX, aspectY);
		} catch (Exception e) {
			Toast.makeText(context, "图片加载失败", 1000).show();
		}
	}

	/**
	 * data 中检出图片
	 * 
	 * @param activity
	 * @param data
	 * @return
	 */
	public static Bitmap checkImage(Activity activity, Intent data) {
		Bitmap bitmap = null;
		try {
			Uri originalUri = data.getData();
			int angle = getAngle(activity,originalUri);
			String path = getRealPathFromURI(activity, originalUri);
			File f = activity.getExternalCacheDir();
			String pp = f.getAbsolutePath();
			if (path.indexOf(pp) != -1) {
				path = path.substring(path.indexOf(pp), path.length());
			}
			bitmap = getLocalImage(new File(path), 500, 500,angle);
		} catch (Exception e) {
		} finally {
			return bitmap;
		}
	}

	public static int getAngle(Activity activity, Uri originalUri) {
		// TODO Auto-generated method stub
		ContentResolver cr = activity.getContentResolver(); 
		String orientation = null;
	    Cursor cursor = cr.query(originalUri, null, null, null, null);// 根据Uri从数据库中找  
	    if (cursor != null) {  
	        cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了  
	       // String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路  
	         orientation = cursor.getString(cursor.getColumnIndex("orientation"));// 获取旋转的角度  
	         LogUtils.d("获取旋转的角度orientation"+orientation);
	        cursor.close();  }
	    int angle = 0;  
        if (orientation != null && !"".equals(orientation)) {  
            angle = Integer.parseInt(orientation);  
        }  
		return angle;
	}

	/**
	 * 通过URI 获取真实路劲
	 * 
	 * @param activity
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Activity activity, Uri contentUri) {
		Cursor cursor = null;
		String result = contentUri.toString();
		String[] proj = { MediaStore.Images.Media.DATA };
		cursor = activity.managedQuery(contentUri, proj, null, null, null);
		if (cursor == null)
			throw new NullPointerException("reader file field");
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			result = cursor.getString(column_index);
			/*************************获得图片的路径是***********************/
			LogUtils.d("获得图片的路径是"+result);
//			/***************************获得图片旋转的角度***********************************/
//		    String orientation = cursor.getString(cursor.getColumnIndex("orientation"));
//		    LogUtils.d("获得的旋转角度是"+orientation);
			if (Integer.parseInt(Build.VERSION.SDK) < 14) {
				cursor.close();
			}
		}
		return result;
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
	 * 由本地文件获取希望大小的文件和图片本身的旋转角度
	 * 
	 * @param f
	 * @param angle2 
	 * @return
	 */
	public static Bitmap getLocalImage(File f, int swidth, int sheight, int angle2) {
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
				int s = 1;
				while ((mWidth / s > sWidth * 2) || (mHeight / s > sHeight * 2)) {
					s *= 2;
				}
				options = new BitmapFactory.Options();
				LogUtils.d("压缩率"+s);
				if(s>4){
				options.inSampleSize = s/4;
				}else{
					options.inSampleSize = s;
				}
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
				int angle = angle2;
				
				 Matrix m = new Matrix();  
	                int width = bitmap.getWidth();  
	                int height = bitmap.getHeight();  
	                LogUtils.d("或得到的宽度是"+width+"或得到的高度是"+height);
	                m.setRotate(angle); // 旋转angle度  
	                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 从新生成图片  
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
	 * aspectY Y对于X的比例 outputX X 的宽
	 * **/
	public static void photoZoom(Activity activity, Uri uri, Uri outUri, int photoResoultCode, int aspectX, int aspectY) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		if (aspectY > 0) {
			intent.putExtra("aspectX", aspectX);
			intent.putExtra("aspectY", aspectY);
		}
		intent.putExtra("scale", aspectX == aspectY);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
		intent.putExtra("noFaceDetection", true); 
		//LogUtils.d("666666666666666666666666666");
		activity.startActivityForResult(intent, photoResoultCode);
		LogUtils.d("第五步：发起裁剪的请求，并带上裁剪的返回码,并且把裁剪的图片存在temp中");
	}

	/**
	 * 保存zoom之后的图片
	 * @param data zoom后的intent
	 * @param context 上下文
	 * @return
	 */
	public static Bitmap getZoomBitMap(Intent data, Context context) {
		try {
			Bundle extras = data.getExtras();
			if (extras != null) {
				LogUtils.d("111111");
				Bitmap bitmap = extras.getParcelable("data");
				LogUtils.d("2222");
				File file = new File(sdCardPath+"/" + "tempFile.jpg");
				LogUtils.d("3333");
				compressImage(bitmap, file, 30);
				LogUtils.d("4444");
				return bitmap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//Toast.makeText(context, "wwwwwwww", Toast.LENGTH_SHORT).show();
		}
		return null;
	}
	
	/**
	 * 通过这个方法对图片进行一定程度的压缩
	 * @param source
	 * @return
	 */
	public static Bitmap getImage(String source,Context context) {

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
			DisplayMetrics displayMetrics = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			int screeWidth = displayMetrics.widthPixels;
			int screeHeight = displayMetrics.heightPixels;
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
	 * 获得指定大小的bitmap
	 * @param bitmap
	 * @return
	 */
	public static Drawable getMyDrawable(Bitmap bitmap,int screeWidth) {
		Drawable drawable = new BitmapDrawable(bitmap);
		int imgWidth = screeWidth-20 ;
		int imgHeight = (int) (imgWidth /drawable.getIntrinsicWidth()*drawable.getIntrinsicHeight()+0.5f);
		drawable.setBounds(0, 0, imgWidth, imgHeight);
		return drawable;
	}
}
