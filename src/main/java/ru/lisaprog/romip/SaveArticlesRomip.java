package ru.lisaprog.romip;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.Common;
import ru.lisaprog.TF;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Юлиан on 28.03.15.
 */
public class SaveArticlesRomip {

	private final File dir = new File("A:\\romip\\legal2007_new_dictionary_2\\");
	private final String dict = "src/main/resources/romip/dictionary.txt";
	private final String extension = "src/main/resources/romip/ngramms.txt";
	private Object2IntOpenHashMap<String> terms;
	private TF tf;

	public SaveArticlesRomip(){

		try{
			HashSet<String> dictionary = new HashSet<>();
			File file = new File(dict);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				dictionary.add(line);
			}

			file = new File(extension);
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				dictionary.add(line.split("\t")[0]);
			}

			tf = new TF(dictionary);

			terms = new Object2IntOpenHashMap<>();
			for(String term : dictionary){
				terms.put(term, terms.size() + 1);
			}

		}catch (Exception ex){
			Common.createLog(ex);
		}
	}

	public void start(){
		try{
			int num = 0;
			for(File file : dir.listFiles()){
				System.out.println(num++);
				HashMap<String, Vector> documents = new HashMap<>();
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					Object2IntOpenHashMap<String> map = tf.mapOfTheString(line.split("\t")[1]);
					Int2DoubleOpenHashMap vector = new Int2DoubleOpenHashMap();
					for(String i : map.keySet()){
						vector.put(terms.getInt(i), map.getInt(i));
					}
					documents.put(line.split("\t")[0], new Vector(vector));
				}
				SQLQuery.saveArticlesRomip(documents);
			}
		}catch (Exception ex){
			Common.createLog(ex);
		}
	}


}
