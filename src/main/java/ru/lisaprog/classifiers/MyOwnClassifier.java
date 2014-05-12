package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import ru.lisaprog.articles.Article;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Юлиан on 04.04.14.
 */
public class MyOwnClassifier implements Classifier {

	public String findUDC(Article article, HashSet<String> UDCs){
		double value = 1.6;

		Vector artVector = article.vector;

		DoubleArrayList al = new DoubleArrayList(artVector.values());

		Collections.sort(al);

		double limit = al.get(al.size() - 50);
		IntOpenHashSet set = new IntOpenHashSet();

		for(int i : artVector.keySet())
			if(artVector.get(i) > limit)
				set.add(i);

		for(int i : set){
			artVector.remove(i);
		}

		artVector.add(new Vector());//обновляет норму)

		HashMap<String, String> udc2url = SQLQuery.getUDCURLByParent("51");

		String best = "";
		for(String udc : UDCs){

			int numberOfUrls = 0;

			Vector vector = new Vector();
			Int2IntOpenHashMap map = new Int2IntOpenHashMap();
			for(String url : udc2url.get(udc).split(";")){
				numberOfUrls++;
				Vector urlVector = SQLQuery.getURLVector(url);
				vector.add(urlVector);



				for(int i : vector.keySet()){
					if(!map.containsKey(i))
						map.put(i, 0);
					map.put(i, map.get(i) + 1);
				}
			}

			if(udc.equals("519.1"))
				System.out.println("OLOLO   " + vector.get(115535)*vector.getNorm());

			for(int i : map.keySet()){
				//1.0*map.get(i)/numberOfUrls < 0.5 ||
				if(SQLQuery.getWordData(i).getUnits() > 1000 || vector.getNorm()*vector.get(i)/numberOfUrls < 1)
					vector.remove(i);
			}

			vector.add(new Vector());

			for(int i : vector.keySet()){

				System.out.println(udc + "  " + SQLQuery.getWordData(i).getWord() + "  " + 1.0*map.get(i)/numberOfUrls + "  " + numberOfUrls);
			}

			System.out.println("Next udc");
			if(vector.angle(artVector) < value){
				value = vector.angle(artVector);
				best = udc;
			}
		}
		return best;
	}
}
