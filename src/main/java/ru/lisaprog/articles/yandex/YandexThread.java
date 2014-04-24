package ru.lisaprog.articles.yandex;

import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by Юлиан on 05.04.14.
 */
public class YandexThread implements Runnable {

	FileOutputStream fos;
	ReadableByteChannel channel;
	public String result = "";
	private String url;

	public YandexThread(FileOutputStream stream, String url){
		fos = stream;
		this.url = url;
	}

	public void run(){
		try{

			java.net.URL website = new java.net.URL(url);
			channel = Channels.newChannel(website.openStream());
			fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
			result = "ok";
			fos.close();

		}catch(Exception e){
			result = "error";
		}

	}
}
