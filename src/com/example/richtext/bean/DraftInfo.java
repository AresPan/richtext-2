package com.example.richtext.bean;

import java.io.Serializable;

/**
 *  @项目名: ZEALER
 *  @包名: com.zealer.app.bean
 *  @类名: DraftInfo 
 *  @创建者: 李建昌
 *  @创建时间: 2016年2月25日 下午5:37:18
 *  @描述: 草稿箱帖子信息的bean类* 
 *  @git版本: $Rev$ 
 *  @更新人: $Author$ 
 * @更新时间: $Date$  
 * @更新描述: TODO *
 */
public class DraftInfo implements Serializable {
	
	public int     id;
	public String  time;
	public String  text;
	public String  html;
	public String  title;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	
}
