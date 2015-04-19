package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.articles.Article;
import ru.lisaprog.objects.Term;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Юлиан on 29.03.14.
 *
 * CREATE TABLE lisa.rocchio_terms(id int, udc text, parent text, tf int, idf float, weight float);
 *
 * CREATE TABLE lisa.rocchio_terms(udc text, weights longtext);
 *
 */
public class Rocchio implements Classifier {

	private static HashMap<String, Vector> documents = new HashMap<>();

	private static HashMap<String, Vector> conceptsWeights = new HashMap<>();

	private static Int2IntOpenHashMap tf = new Int2IntOpenHashMap();
	private static Int2IntOpenHashMap df = new Int2IntOpenHashMap();

	private int N = 0;

	public String findUDC(Article a, HashSet<String> s){
		return "";
	}

	public String classify(String[] document) throws Exception{
		return classify(Vector.toVector(document));
	}

	public void addVector(String udc, Vector vector){
		if(!documents.containsKey(udc))
			documents.put(udc, new Vector());
		documents.get(udc).add(vector);
		for(int i : vector.keySet()){
			if(!df.containsKey(i)){
				df.put(i, 0);
				tf.put(i, 0);
			}
			df.put(i, df.get(i) + 1);
			tf.put(i, tf.get(i) + (int)Math.round(vector.get(i)*vector.getNorm()));
		}
		N++;
	}


	public void buildClassifier(){
		for(String key : documents.keySet()){
			Vector concept = new Vector();
			Vector vector = documents.get(key);
			for(int i : vector.keySet()){
				concept.put(i, Math.log(vector.get(i)*vector.getNorm() + 1)*Math.log(1.0 * N / df.get(i)));
			}
			double sum = 0;
			for(int i : vector.keySet()){
				sum += concept.get(i)*concept.get(i);
			}
			sum = Math.sqrt(sum);
			for(int i : vector.keySet()){
				concept.put(i, concept.get(i)*concept.getNorm()/sum);
			}
			concept.normalize();
			conceptsWeights.put(key, concept);
		}
	}






	public String classify(Vector vector){
		String result = "";
		Vector newVector = new Vector();
		for(int i : vector.keySet()){
			if(df.get(i) > 0)
				newVector.put(i, Math.log(vector.get(i)*vector.getNorm() + 1)*Math.log(1.0 * N / df.get(i)));
		}
		newVector.normalize();
		DoubleArrayList list = new DoubleArrayList();
		for(String udc : conceptsWeights.keySet()){
//			System.out.println("Angle = " + newVector.angle(conceptsWeights.get(udc)));
			list.add(newVector.angle(conceptsWeights.get(udc)));
		}
		Collections.sort(list);
		for(int i = 0; i < 1; i++){
			for(String udc : conceptsWeights.keySet()){
				if(newVector.angle(conceptsWeights.get(udc)) == list.get(i)){
					result += udc;
				}
			}
		}

		return result;
	}





	/*
	private static void getParents(){
		parents = SQLQuery.getDistinctParents();
	}

	public static void evaluateTFIDF(){
		getParents();
//		parents.add("NULL");
//		parents.add("5");
//		parents.add("51");

		for(String i : parents){
			HashMap<String, String> udc2url = SQLQuery.getUDCURLByParent(i);
			df = new Int2IntOpenHashMap();
			idf = new Int2DoubleOpenHashMap();
			calculations(udc2url, i);
		}

	}

	private static void calculations(HashMap<String, String> udc2url, String parent){

		int documentNumber = 0;
		HashMap<String, Vector> concepts = new HashMap<>();
		HashMap<String, Vector> weights = new HashMap<>();
		HashMap<String, Int2IntOpenHashMap> dfc = new HashMap<>();
		Object2IntOpenHashMap<String> conceptSize = new Object2IntOpenHashMap<>();
		for(String udc : udc2url.keySet()){
			dfc.put(udc, new Int2IntOpenHashMap());
			conceptSize.put(udc, 0);
			System.out.println(udc);
			Vector concept = new Vector();
			for(String url : udc2url.get(udc).split(";")){
				if(conceptSize.get(udc) > 500)
					break;
				conceptSize.put(udc, conceptSize.get(udc) + 1);
				documentNumber++;
				Vector vect = SQLQuery.getURLVector(url);
				concept.add(vect);
				for(int term : vect.keySet()){
					if(!df.containsKey(term))
						df.put(term, 0);
					df.put(term, df.get(term) + 1);
					if(!dfc.get(udc).containsKey(term))
						dfc.get(udc).put(term, 0);
					dfc.get(udc).put(term, dfc.get(udc).get(term) + 1);
				}
			}
			concepts.put(udc, concept);
		}

		for(int i : df.keySet()){
			idf.put(i, Math.log(documentNumber/df.get(i)));
		}

		for(String udc : concepts.keySet()){
			Vector concept = concepts.get(udc);
			weights.put(udc, new Vector());
			for(int key : concept.keySet()){
				weights.get(udc).put(key, Math.log(1.0*concept.get(key)*concept.getNorm() + 1)*Math.log(1.0*documentNumber/(0.1 + df.get(key) - dfc.get(udc).get(key))) * Math.log(1 + (1.0*dfc.get(udc).get(key)/conceptSize.get(udc)))*Math.log(2.0));
			}
			weights.get(udc).normalize();
		}
		for(String udc : weights.keySet()){
			SQLQuery.rocchioSaveUDCWeights(udc, weights.get(udc));
		}
	}

	public static void test(Article article){
		String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"};
		//String str = "510;511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;";
		String str = "502/504;51;52;53;54;55;56;57;58;59;";
		Vector art = toWeights(article.vector);
		//main = str.split(";");
		for(String i : main){
			Vector vect = SQLQuery.rocchioGetUDCWeights(i);
			System.out.println(i + "  ==  " + Vector.radToGrad(vect.angle(art)));
		}
		Vector vect = SQLQuery.rocchioGetUDCWeights("519.8");
		IntOpenHashSet set = new IntOpenHashSet(vect.keySet());
		set.retainAll(article.vector.keySet());
		System.out.println("size = " + vect.size());
		System.out.println("size art = " + article.vector.size());
		int number = 0;

		for(int i : vect.keySet()){
			if(vect.get(i) == 0.0)
				number++;
		}

		double sum = 0;
		for(int key : article.vector.keySet()){
			sum += vect.get(key);
		}

		double fullsum = 0;
		for(int key : vect.keySet()){
			fullsum += vect.get(key);
		}


		//System.out.println(sum);
		//System.out.println(fullsum);

	}

	public String findUDC(Article article, HashSet<String> UDCs){
		String result = "";
		double min = 1.6;
		Vector articleWeights = toWeights(article.vector);
		for(String udc : UDCs){
			Vector vector = SQLQuery.rocchioGetUDCWeights(udc);
			double value = vector.angle(articleWeights);
			if(min > value){
				min = value;
				result = udc;
			}
		}
		return result;
	}

	private static Vector toWeights(Vector vector){
		Vector weights = new Vector();
		for(int term : vector.keySet()){
			Term word = SQLQuery.getWordData(term);
			weights.put(term, Math.log(vector.get(term)*vector.getNorm() + 1)*Math.log(1.0 * 12500 / word.getUnits()));
		}
		weights.normalize();
		return weights;
	}*/

}


//в принципе ок
//weights.get(udc).put(key, Math.log(1.0*concept.get(key)*concept.getNorm() + 1)*Math.log(1.0*documentNumber/(0.1 + df.get(key) - dfc.get(udc).get(key))) * (1.0*dfc.get(udc).get(key)/conceptSize.get(udc)));
//weights.get(udc).put(key, Math.log(1.0*concept.get(key)*concept.getNorm() + 1)*Math.log(1.0*documentNumber/(0.1 + df.get(key) - dfc.get(udc).get(key))) * Math.log(1 + (1.0*dfc.get(udc).get(key)/conceptSize.get(udc)))*Math.log(2.0));
//weights.get(udc).put(key, Math.log(1.0*concept.get(key)*concept.getNorm() + 1)*Math.log(1.0*documentNumber/(0.1 + df.get(key) - dfc.get(udc).get(key))) * Math.log(1 + (1.0*dfc.get(udc).get(key)/conceptSize.get(udc)))*Math.log(2.0));









