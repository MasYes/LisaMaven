package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import ru.lisaprog.articles.Article;
import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Юлиан on 12.05.14.
 */
public class MRF implements Classifier{

	private ArrayList<String> labels;
	private Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<RandomForest>> classifiers;
	private IntOpenHashSet terms;
	private double miLimit = 0.05;

	public String findUDC(Article article, HashSet<String> UDCs){return "";}

	public String classify(String[] document) throws Exception{
		return classify(Vector.toVector(document));
	}


	public MRF(ArrayList<String> labels){
		this.labels = labels;
		this.classifiers = new Int2ObjectOpenHashMap<>();
	}

	public MRF(ArrayList<String> labels, double miLimit){
		this.labels = labels;
		this.classifiers = new Int2ObjectOpenHashMap<>();
		this.miLimit = miLimit;
	}

	public void buildClassifiers() throws Exception{
		terms = new IntOpenHashSet();

		for(String udc : labels)
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(udc, miLimit));

		for(int i = 0; i < labels.size(); i++){
			String label1 = labels.get(i);
			ArrayList<ArticleYandex> label1Articles = getArticlesForUDC(label1);
			for(int j = i + 1; j < labels.size(); j++){
				String label2 = labels.get(j);
				ArrayList<ArticleYandex> label2Articles = getArticlesForUDC(label2);
				WekaInstance wekaInstance = new WekaInstance();
				for(ArticleYandex article : label1Articles){
					if(article.perplexity < 1.125)
						wekaInstance.addVector(label1, article.vector);
				}
				for(ArticleYandex article : label2Articles){
					if(article.perplexity < 1.125)
						wekaInstance.addVector(label2, article.vector);
				}
				wekaInstance.createInstance();

				RandomForest rf = new RandomForest(wekaInstance);
				rf.buildClassifier();

				if(!classifiers.containsKey(i))
					classifiers.put(i, new Int2ObjectOpenHashMap<RandomForest>());
				if(!classifiers.containsKey(j))
					classifiers.put(j, new Int2ObjectOpenHashMap<RandomForest>());

				classifiers.get(i).put(j, rf);
				classifiers.get(j).put(i, rf);

			}
		}
	}

	private ArrayList<ArticleYandex> getArticlesForUDC(String udc){
		ArrayList<ArticleYandex> articles = SQLQuery.getArticlesYandex("udc = '" + udc + "'");
		ArrayList<ArticleYandex> result = new ArrayList<>();
		for(ArticleYandex article : articles){
			Vector vector = new Vector();
			for(int i : article.vector.keySet())
				if(terms.contains(i))
					vector.put(i, article.vector.get(i)*article.vector.getNorm());
//					vector.put(i, 1.0);
			vector.normalize();
			if(vector.size() > 0)
				result.add(new ArticleYandex(article.udc, article.rank, vector, article.perplexity));
		}
		return result;
	}

	public String classify(Vector vector) throws Exception{
		Int2IntOpenHashMap results = new Int2IntOpenHashMap();

		for(int i = 0; i < labels.size(); i++){
			String udc = labels.get(i);
			for(RandomForest rf : classifiers.get(i).values())
				for(Vector subVector : splitIntoVectors(vector))
					if(rf.classify(subVector).equals(udc))
						results.put(i, results.get(i) + 1);
		}

		String result = "";
		int max = 0;

		for(int i : results.keySet()){
			if(results.get(i) > max){
				max = results.get(i);
				result = labels.get(i);
			}
			System.out.println(labels.get(i) + "   ===   " + results.get(i));
		}

		return result;
	}

	private ArrayList<Vector> splitIntoVectors(Vector vector){
		int partitionSize = 100500;
		Vector subVector = new Vector();
		ArrayList<Vector> result = new ArrayList<>();
		int count = 0;
		for(int i : vector.keySet()){
			count++;
			subVector.put(i, vector.get(i)*vector.getNorm());
			if(count == partitionSize){
				result.add(subVector);
				subVector.normalize();
				subVector = new Vector();
				count = 0;
			}
		}
		if(count > 0){
			subVector.normalize();
			result.add(subVector);
		}
		return result;
	}


}
