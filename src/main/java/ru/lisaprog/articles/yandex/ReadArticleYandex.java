package ru.lisaprog.articles.yandex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by Юлиан on 12.04.14.
 */
public class ReadArticleYandex implements Runnable{

	public String result = "";
	public File file;

	public ReadArticleYandex(File file){
		this.file = file;
	}

	public void run(){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				result += line + ";";
			}
			reader.close();
		}catch(Exception e){
			result = "Error";
		}


	}

}
