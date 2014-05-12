package ru.lisaprog.articles.yandex;

import ru.lisaprog.articles.ArticleInterface;
import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.objects.UDC;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.articles.ArticleAbstract;

import java.io.File;
import java.util.*;

/**
 * Created by Юлиан on 30.04.14.
 */
//39133
public class Common {

	public static void calculatePerplexity() throws Exception{

		ArrayList<String> UDCs = SQLQuery.getArticlesYandexUDCs();
		boolean pass = true;
		for(String udc : UDCs){
			if(!udc.equals("519.83"))
				continue;
			pass = false;
			System.out.println(udc);
			ArrayList<ArticleYandex> articles = SQLQuery.getArticlesYandex("udc = '" + udc + "'");
			for(int i = 0; i < articles.size(); i++){
				Vector vector = new Vector();
				for(int j = 0; j < articles.size(); j++){
					if(j == i)
						continue;
					for(int key : articles.get(j).vector.keySet()){
						vector.put(key, vector.get(key) + 1);
					}
				}
				vector.normalize();
				SQLQuery.savePerplexity(articles.get(i).udc, articles.get(i).rank, articles.get(i).vector.angle(vector));
			}
		}
	}

	public static boolean setArticlesToParents() throws Exception{
		boolean oneMore = false;
		ArrayList<String> UDCs = SQLQuery.getListOfUDCs();
		ArrayList<String> AYUDCs = SQLQuery.getArticlesYandexUDCs();
		for(String udcS : UDCs){
			if(!AYUDCs.contains(udcS)){
				boolean process = true;
				UDC udc = SQLQuery.getUDC(udcS);
				System.out.println(udc.id);
				for(String child : udc.getChildren().split(";")){
					if(!AYUDCs.contains(child)){
						process = false;
						break;
					}
				}

				if(process){
					System.out.println("Processing");
					oneMore = true;
					ArrayList<ArticleYandex> articles = new ArrayList<>();
					for(String child : udc.getChildren().split(";")){
						articles.addAll(SQLQuery.getArticlesYandex("udc = '" + child + "'"));
					}

					HashMap<ArticleYandex, Double> relevancy = articlesRelevancy(articles);
					ArrayList<Double> values = new ArrayList<>(relevancy.values());
					Collections.sort(values);
					ArrayList<ArticleYandex> saved = new ArrayList<>();
					double limit = 2;
					if(values.size() > 20)
						limit = values.get(20);
					for(ArticleYandex articleYandex : relevancy.keySet())
						if(relevancy.get(articleYandex) <= limit){
							articleYandex.perplexity = relevancy.get(articleYandex);
							articleYandex.udc = udc.id;
							saved.add(articleYandex);
						}
					SQLQuery.saveArticlesYandex(saved);
				}
			}
		}
		return oneMore;
	}

//	public static HashMap<? extends ArticleAbstract, Double> articlesRelevancy(ArrayList<? extends ArticleAbstract> articles){
//		HashMap<ArticleAbstract, Double> result = new HashMap<>();
	public static HashMap<ArticleYandex, Double> articlesRelevancy(ArrayList<ArticleYandex> articles){

		ArrayList<ArticleYandex> articlesFiltered = new ArrayList<>();
		HashSet<Vector> vectors = new HashSet<>();
		for(ArticleYandex articleYandex : articles)
			if(vectors.add(articleYandex.vector))
				articlesFiltered.add(articleYandex);


		HashMap<ArticleYandex, Double> result = new HashMap<>();
		for(int i = 0; i < articlesFiltered.size(); i++){
//			System.out.println(i + "/" + articlesFiltered.size());
			Vector vector = new Vector();
			for(int j = 0; j < articlesFiltered.size(); j++){
				if(j != i)
					vector.add(articlesFiltered.get(j).vector);
			}
			vector.normalize();
			result.put(articlesFiltered.get(i), vector.angle(articlesFiltered.get(i).vector));
		}
//		System.out.println("size = " + result.size());
		return result;
	}



}
