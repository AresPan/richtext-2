package com.example.richtext.dao;

import java.util.ArrayList;
import java.util.List;
import com.example.richtext.bean.DraftInfo;
import com.example.richtext.dao.DraftListDB.DraftList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 *  @项目名: ZEALER
 *  @包名: com.zealer.app.dao
 *  @类名: DraftDao 
 *  @创建者: 李建昌
 *  @创建时间: 2016年2月25日 下午2:34:16
 *  @描述: 数据库控制语句* 
 *  @git版本: $Rev$ 
 *  @更新人: $Author$ 
 * @更新时间: $Date$  
 * @更新描述: TODO *
 */
public class DraftDao {
	private DraftListDBHelper  mHelper;
	
	public DraftDao(Context context){
		mHelper = new DraftListDBHelper(context);
	}
	
	/**
	 * 增加一条记录
	 * 数据库应该保存的东西应该有
	 * 1，富文本的HTML 
	 * 2，富文本的toString得到的字符串 
	 * 3，发布帖子的一个网络请求参数groupId
	 * 4，存储URI和picid的容器
	 * 5，把当前的时间也存入
	 * 6，把小组的名称存入
	 * 7 用户的userId
	 */
	public boolean add(String time,String html,String toString,String title){
		
		SQLiteDatabase  db = mHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DraftListDB.DraftList.COLUMN_HTML, html);
		values.put(DraftListDB.DraftList.COLUMN_STRING, toString);
		values.put(DraftListDB.DraftList.COLUMN_TIME, time);
		values.put(DraftListDB.DraftList.COLUMN_TITLE, title);
		Long insert = db.insert(DraftListDB.DraftList.TABLE_NAME, null, values);
		db.close();
		return insert != -1;
		
	}
	
	/**
	 * 删除数据
	 * 根据COLUMN_ID来
	 */
	public boolean delete(int id){
		
		SQLiteDatabase  db = mHelper.getWritableDatabase();
		String whereClause = DraftList.COLUMN_ID + "=?";
		String[] whereArgs = new String[]{ id+"" }; 
		int delete  = db.delete(DraftList.TABLE_NAME, whereClause, whereArgs);
		db.close();
		
		return delete != 0;
		
	}

	
	/**
	 * 查询所有数据
	 */
	public List<DraftInfo> findAll(){
		
		SQLiteDatabase db = mHelper.getReadableDatabase();
		String sql  = "select " +DraftList.COLUMN_HTML + ","
				+DraftList.COLUMN_ID + ","
				+DraftList.COLUMN_TIME + ","
				+DraftList.COLUMN_STRING + ","
				+DraftList.COLUMN_TITLE + " from "
				+DraftList.TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		
		List<DraftInfo> mList = new ArrayList<DraftInfo>();
		if(cursor != null){
			while(cursor.moveToNext()){
				
				DraftInfo info = new DraftInfo();
				info.html    = cursor.getString(0);
				info.id = cursor.getInt(1);
				info.time = cursor.getString(2);
				info.text =  cursor.getString(3);
				info.title = cursor.getString(4);
				mList.add(info);
//				LogUtils.d("获得的text文本为"+info.text);
			}
			cursor.close();
		}
		db.close();
		
		return mList;
		
	}
	

	/**
	 * 更新一条数据的time，title，html，toString，hashmap
	 */
	public boolean update(int id,String time,String html,String text,String title){
		SQLiteDatabase db = mHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(DraftList.COLUMN_TIME, time);
		values.put(DraftList.COLUMN_HTML, html);
		values.put(DraftList.COLUMN_STRING, text);
		values.put(DraftList.COLUMN_TITLE, title);
		String whereClause = DraftList.COLUMN_ID + "=?";
		String[] whereArgs = new String[]{ id+"" };
		int update  = db.update(DraftList.TABLE_NAME, values, whereClause, whereArgs);
		return update!=0;
		
	}
}
