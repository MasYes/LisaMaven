package ru.lisaprog.articles.yandex;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by Юлиан on 12.04.14.
 */
public class SaveArticlesYandex { // в идеале - распоточить, но от греха подальше пока не буду :)

	private static Object2IntOpenHashMap<String> terms;


	public static void saveArticles() throws Exception{
		terms = SQLQuery.getWords();
		File dir = new File("A:\\articles\\YANDEX");
		for(File folder : dir.listFiles()){
			if(!folder.getName().equals("519.83"))
				continue;
			System.out.println(folder.getName());

			long time = System.currentTimeMillis();
			if(folder.isDirectory()){
				ArrayList<ArticleYandex> articleYandexes = new ArrayList<>();
				BufferedReader readerUDC = new BufferedReader(new FileReader(folder + "\\udc.txt"));
				String udc = readerUDC.readLine();
				readerUDC.close();
				for(File article : folder.listFiles()){
//					long time2 = System.currentTimeMillis();
						if(article.getName().endsWith("_lemm_oneline.txt")){
							BufferedReader reader = new BufferedReader(new FileReader(article));
							String line = reader.readLine();
							reader.close();
//							System.out.println(1.0*(System.currentTimeMillis() - time2)/1000);
							if(line == null)
								continue;
							Vector vector = new Vector();
							for(String term : line.split(";")){
								if(!terms.containsKey(term))
									continue;
								int id = terms.get(term);
								vector.put(id, vector.get(id) + 1);
							}
							vector.normalize();
							String rank = article.getName().substring(0, article.getName().indexOf("_"));
//							System.out.println(1.0*(System.currentTimeMillis() - time2)/1000);
							articleYandexes.add(new ArticleYandex(udc, rank, vector));
//							System.out.println(1.0*(System.currentTimeMillis() - time2)/1000);
						}
				}
				SQLQuery.saveArticlesYandex(articleYandexes);
				break;
			}
			System.out.println(1.0*(System.currentTimeMillis() - time)/1000);
//			System.exit(1000);
		}


	}



}
