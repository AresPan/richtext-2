package com.example.richtext.dao;

public interface DraftListDB {
	/**
	 * 数据库的名称
	 */

	String DB_NAME = "draft.db";
	/**
	 * 当前数据库的版本
	 */
	int  VERSION = 1;
	/**
	 * 创建数据库的语言
	 */
	public interface DraftList{
		/**
		 * 表的名字
		 */
		String TABLE_NAME = "draft_list";
		/**
		 * 主键id
		 */
		String  COLUMN_ID  = "id";
		/**
		 * 帖子的标题
		 */
		String  COLUMN_TITLE = "title"; 
		/**
		 * 帖子的修改/创建时间
		 */
		String  COLUMN_TIME = "time";
		/**
		 * 富文本的HTML
		 */
		String COLUMN_HTML ="Html";
		/**
		 * 富文本的String格式
		 */
		String COLUMN_STRING = "String";
	
		/**
		 * 建表语句
		 */
		String SQL_CREATE_TABLE = "create table " + TABLE_NAME +"("
				+ COLUMN_ID  + " integer primary key autoincrement,"
				+ COLUMN_TIME + " text,"
				+ COLUMN_TITLE + " text,"
				+ COLUMN_HTML +" text,"
				+ COLUMN_STRING +" text"
				+")";
	}
}
