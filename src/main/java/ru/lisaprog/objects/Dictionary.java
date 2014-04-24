package ru.lisaprog.objects;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 18.07.13
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 *
 *CREATE TABLE lisa.dict(id int AUTO_INCREMENT primary key, word char(32) unique, units int, freq int, meas double);
 *
 * НЕ ЗАБЫВАТЬ МЕНЯТЬ Ё НА Е
 */

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.Common;
import ru.lisaprog.articles.yandex.ReadArticleYandex;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.sql.SQLQuery;

import java.io.*;
import java.util.*;


public class Dictionary {
	protected static Int2ObjectOpenHashMap<Term> dict = new Int2ObjectOpenHashMap<>();
	public static Object2IntOpenHashMap<String> indexes= new Object2IntOpenHashMap<>();
	protected static ArrayList<ArticleYandex> articles = new ArrayList<>();
	private static String rank;
	private static String udc;

//	private static HashSet<String> dict = SQLQuery.getWordsFromDict("dict_urls");

	private static void addArticle(ru.lisaprog.objects.Vector vector){
//		System.out.println("Articles size = " + articles.size());
		vector.normalize();
		articles.add(new ArticleYandex(udc, rank, vector));
		if(articles.size() >= 100){
			SQLQuery.saveArticlesYandex(articles);
			articles.clear();
		}
	}

	private static void addToDictionary(String[] str){
		ArrayList<String> array = new ArrayList<>();
		array.addAll(Arrays.asList(str)); //еще не определился, как лучше - так, или в конатрукторе сразу привести к такому виду.
		HashSet<String> set = new HashSet<>(array);
		set.remove("");
	//	Vector vector = new Vector();
		for(String i : set){
			if(indexes.containsKey(i)){
				int frequency = Collections.frequency(array, i);
				int id = indexes.get(i);
	//			vector.put(id, frequency);
				dict.get(id).incrementUnits();
				dict.get(id).addToFrequency(frequency);
			}
			else{
				int frequency = Collections.frequency(array, i);
				indexes.put(i, indexes.size() + 1);
				dict.put(indexes.get(i), new Term(i, Collections.frequency(array, i) ));
	//			vector.put(indexes.get(i), frequency);
			}
		}
	//	addArticle(vector);
	}

	/*private static void addToDictionary(String[] str){
		for(String i : str){
			if(i.matches("[А-Яа-я]*"))
				if(dict.add(i))
					System.out.println(i);
		}
	}*/

	public static void saveDictionary(){
		ArrayList<Term> terms = new ArrayList<>();
		for(int key: dict.keySet()){
			Term term = dict.get(key);
			if(term.getWord().length() > 2 && term.getWord().length() < 32 && term.getFrequency() >= 5){
				terms.add(term);
			}
			if(terms.size() >= 5000){
				SQLQuery.saveIntoDict(terms);
				terms = new ArrayList<>();
			}
		}
		SQLQuery.saveIntoDict(terms);
		SQLQuery.saveArticlesYandex(articles);
	}

	/*public static void saveDictionary(){
		ArrayList<Term> terms = new ArrayList<>();
		for(String term: dict){
			if(term.length() > 2 && term.length() < 32){
				terms.add(new Term(term));
			}
			if(terms.size() >= 5000){
				SQLQuery.saveIntoDict(terms);
				terms = new ArrayList<>();
			}
		}
		SQLQuery.saveIntoDict(terms);
	}*/

	public static void createDict(){
		for(String path : new String[]{"A:\\articles\\CPS","A:\\articles\\CYBERLENINKA"}){
			File dir = new File(path);
			File [] files = dir.listFiles();
			for(File i : files){
				if (i.toString().contains(".txt"))
				try{
					System.gc();
					Scanner file = new Scanner(i);
					String str = "";
					String curr;
					while(file.hasNext()){
						curr = file.nextLine();
						if(curr.length() > 6 && curr.length() - curr.lastIndexOf("-") < 3){
							str += curr.substring(0, curr.lastIndexOf("-"));
						}
						else
							str += curr + "\n";
					}
					System.out.println(i);
					str = str.replaceAll("Ё", "Е");
					str = str.replaceAll("ё", "е");
					Dictionary.addToDictionary(Lemmer.lemmer(str));
					file.close();
				} catch (Exception e){
					Common.createLog(e);
				}
			}
		}
		Dictionary.saveDictionary();
	}


	public static void createDictFromURLS(){
		for(int i = 1; i <= SQLQuery.getURLCount(); i++){
			System.out.println(i);
			try{
				String str = SQLQuery.getURLText(i);
				if(str != null){
					str = str.replaceAll("Ё", "Е");
					str = str.replaceAll("ё", "е");
					Dictionary.addToDictionary(Lemmer.lemmer(str));
				}
			} catch (Exception e){
				Common.createLog(e);
			}
		}
		Dictionary.saveDictionary();
	}

	public static void createDictFromYandex(){
		try{
		File dir = new File("A:\\articles\\YANDEX");
		for(File folder : dir.listFiles()){
			long time = System.currentTimeMillis();
			if(folder.isDirectory()){
				ArrayList<ReadArticleYandex> readers = new ArrayList<>();
				ArrayList<Thread> threads = new ArrayList<>();
				System.out.println(folder.getAbsolutePath());
				for(File article : folder.listFiles()){
					try{
						if(article.getName().endsWith("_lemm.txt")){
							readers.add(new ReadArticleYandex(article));
							threads.add(new Thread(readers.get(readers.size() - 1)));
							threads.get(threads.size() - 1).setDaemon(true);
							threads.get(readers.size() - 1).run();
						}
						if(article.getName().equals("udc.txt")){
							BufferedReader reader = new BufferedReader(new FileReader(article));
							udc = reader.readLine();
							reader.close();
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
//				System.out.println(readers.size());
				for(int i = 0; i < threads.size(); i++){
					while(threads.get(i).isAlive()){

					}
					String filename = readers.get(i).file.getAbsolutePath();
					filename = filename.substring(0, filename.length() - 4) + "_oneline.txt";
					FileWriter fw = new FileWriter(filename);
					String result = readers.get(i).result;
					fw.write(result);
					fw.close();
				}
				System.out.println((System.currentTimeMillis() - time)/1000.0 + "\n" + articles.size());
			}
		}
	//	Dictionary.saveDictionary();
	}catch(Throwable t){
	//		Dictionary.saveDictionary();
		}
	}
}

