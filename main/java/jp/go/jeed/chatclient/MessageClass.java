package jp.go.jeed.chatclient;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MessageClass implements Serializable{

	String msg, name, date;

	public MessageClass(String msg, String name){
		this.msg = msg;
		this.name = name;

		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		this.date = sdf.format(d);
	}

	public MessageClass(String msg, String name, String date){
		this.msg = msg;
		this.name = name;
		this.date = date;
	}
}
