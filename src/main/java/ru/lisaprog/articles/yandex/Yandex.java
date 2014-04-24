package ru.lisaprog.articles.yandex;

import ru.lisaprog.parser.ExtractText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * Created by Юлиан on 05.04.14.
 *
 * Тут самый костыльный код :)
 *
 *
 */
public class Yandex {

	private static String classValue = "<input class=\"b-form-checkbox__checkbox\" name=\"holdreq\" value=\"";
	private static String href = "<a class=\"b-serp-sitelinks__sitelinks-link b-link\" href=\"";

	public static void parseResults() throws Exception{
		String output = "A:\\articles\\YANDEX";

		String input = "A:\\YandexSaved";

		File firstFile = new File(input);

		boolean pass = true;

		for(String linkFolder : firstFile.list()){
			File secondFile = new File(input + "\\" + linkFolder);
			for(String folder : secondFile.list()){
				Scanner scan = new Scanner(new FileInputStream(secondFile.getAbsoluteFile() + "\\" + folder + "\\index.html"));
				String page = "";
				while(scan.hasNext()){
					page+=scan.nextLine();
				}

	//			page = page.toLowerCase();

				int beginning = page.indexOf(classValue) + classValue.length();

				String title = page.substring(beginning, page.indexOf("\" tabindex=\"1\"", beginning));

				String udc = "";
				for(int i = 0; i < title.split(" ").length; i++){
					if(title.split(" ")[i].equals("УДК")){
						udc = title.split(" ")[i+1];
						break;
					}
				}
				if(pass && !udc.equals("598.279"))
					continue;
				pass = false;
				System.out.println(udc);
				if(udc.length() == 0){
					FileWriter fw = new FileWriter("A:\\articles\\YANDEX\\emptyudc.txt", true);
					fw.write("АХТУНГ! ПУСТОЙ УДК! " + secondFile.getAbsoluteFile() + "\\" + folder + "\n");
					fw.close();
					continue;
				}

				String newUdc = udc.replaceAll("[\\\\/:?*\"|]", "");

				File outputFolder = new File(output + "\\" + newUdc);
				outputFolder.mkdirs();

				FileWriter fw = new FileWriter(outputFolder.getAbsoluteFile() + "\\udc.txt");
				fw.write(udc);
				fw.close();
				boolean bool = true;
				int rank = 0;
				ArrayList<YandexThread> fileOutputStreams = new ArrayList<>();
				while(page.contains(href)){

					page = page.substring(page.indexOf(href) + href.length());

					if(!page.contains("&amp;keyno="))
						continue;

					String url = page.substring(0, page.indexOf("&amp;keyno="));

					url = url.substring(url.indexOf("url=") + 4);
					url = url.substring(0, url.indexOf("&amp"));

	//				url = url.replaceAll("%3A", ":");
	//				url = url.replaceAll("%2F", "/");
					if(bool){
						try{
							bool = false;
							url = URLDecoder.decode(url);
							rank++;

							System.out.println(rank);

							String format = ".unk";

							if(url.endsWith(".pdf"))
								format = ".pdf";
							else if(url.endsWith(".doc"))
								format = ".doc";
							else if(url.endsWith(".docx"))
								format = ".docx";
							else if(url.contains(".pdf"))
								format = ".pdf";
							else if(url.contains(".docx"))
								format = ".docx";
							else if(url.contains(".doc"))
								format = ".doc";

							fileOutputStreams.add(new YandexThread(new FileOutputStream(outputFolder + "\\" + rank + format), url));
							Thread thr = new Thread(fileOutputStreams.get(fileOutputStreams.size() - 1));
							long time = System.currentTimeMillis();
							thr.setDaemon(true);
							thr.start();
							while(System.currentTimeMillis() - time < 2*60*1000 && thr.isAlive());
							if(thr.isAlive() || !fileOutputStreams.get(fileOutputStreams.size() - 1).result.equals("ok")){
								thr.interrupt();
								throw new Exception();
							}
	//						fos.close();



						}catch(Exception e){
							FileWriter problems = new FileWriter(outputFolder + "\\problems.txt", true);
							problems.write(url + "\n");
							problems.close();
						}
					}
					else
						bool = true;


				}


			}
		}







	}

	public static void parseFiles() throws Exception{
		boolean pass = true;
		String dir = "A:\\articles\\YANDEX";
		File dirFile = new File(dir);
		for(File folder : dirFile.listFiles()){
			if(pass && !folder.getName().equals("623.8"))
				continue;
			pass = false;
			System.out.println(folder.getAbsolutePath());
			for(File article : folder.listFiles()){
				if(article.getName().endsWith(".txt"))
					continue;
				String result = "";
				if(article.length() > 0){
					if(article.getAbsolutePath().endsWith(".pdf") || article.getAbsolutePath().endsWith(".doc") || article.getAbsolutePath().endsWith(".docx")){
						result = ExtractText.parse(article.getAbsolutePath());
					}
				}

				if(article.length() > 0 && result.length() == 0){
					result = ExtractText.parsePDF(article.getAbsolutePath());
					if(result.length() == 0)
						result = ExtractText.parseDOC(article.getAbsolutePath());
					if(result.length() == 0)
						result = ExtractText.parseDOCX(article.getAbsolutePath());
				}

				if(result.length() > 0){
					FileWriter fw = new FileWriter(folder.getAbsolutePath() + "\\" + article.getName().split("\\.")[0] + ".txt");
					fw.write(result);
					fw.close();
				}
				else{
					FileWriter fw = new FileWriter("A:\\articles\\problemsYandex.txt", true);
					fw.write(article.getAbsolutePath() + "\n");
					fw.close();
				}
			}
		}

	}

}
