package com.example.richtext.dao;


import com.example.richtext.util.LogUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DraftListDBHelper extends SQLiteOpenHelper {

	public DraftListDBHelper(Context context) {
		
		super(context, DraftListDB.DB_NAME, null, DraftListDB.VERSION);
	}
	/**
	 *  @项目名: ZEALER
	 *  @包名: com.zealer.app.dao
	 *  @类名: DraftListDBHelper 
	 *  @创建者: 李建昌
	 *  @创建时间: 2016年2月25日 下午2:19:41
	 *  @描述: TODO * 
	 *  @git版本: $Rev$ 
	 *  @更新人: $Author$ 
	 * @更新时间: $Date$  
	 * @更新描述: TODO *
	 */

	@Override
	public void onCreate(SQLiteDatabase db) {
		/**
		 * 创建数据库
		 */
		String sql = DraftListDB.DraftList.SQL_CREATE_TABLE;
		db.execSQL(sql);
		LogUtils.d("SQL的语句是"+sql);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//数据库升级语句暂时不写
		
	}
}
