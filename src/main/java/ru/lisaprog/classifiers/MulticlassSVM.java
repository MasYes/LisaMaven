package ru.lisaprog.classifiers;

//http://weka.sourceforge.net/doc.dev/
//http://weka.sourceforge.net/doc.stable/weka/classifiers/functions/LibSVM.html

import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.articles.Article;
import ru.lisaprog.sql.SQLQuery;
import weka.core.*;
import weka.classifiers.trees.RandomForest;

import java.util.*;

public class MulticlassSVM {

	static boolean bool = false;
	private RandomForest svm = new RandomForest();
//	private LibSVM svm = new LibSVM();
	private ArrayList<Attribute> attributes = new ArrayList<>();

	private ArrayList<String> labels;
	private Instances instances;

	public static void test() throws Exception{
		ArrayList<String> classes = new ArrayList<>();
		String udc = "510.2;510.3;510.6;511;512;514.7;515.1;517.9;519.1;519.2;519.6;519.7;519.83;519.85;";
		for(String i : udc.split(";"))
			classes.add(i);
		long time1 = System.currentTimeMillis();
		MulticlassSVM svm = new MulticlassSVM(classes);
		HashMap<String, Integer> map = new HashMap<>();
		ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = \'514.7\' OR udc = \'519.83\' ");
		for(ArticleYandex article : ay){
			if(!map.containsKey(article.udc))
				map.put(article.udc, 0);
			if(map.get(article.udc) < 5){
				svm.addDocument(article.vector, article.udc);
				map.put(article.udc, map.get(article.udc));
			}
		}
		System.out.println(((System.currentTimeMillis() - time1)/1000.0));
		System.out.println("нанчинаем строить кассифиер");
		svm.buildClassifier();
		System.out.println(((System.currentTimeMillis() - time1)/1000.0));
		System.out.println("Время " + (System.currentTimeMillis() - time1)/1000.0 + "\nНу что ж...");
		time1 = System.currentTimeMillis();
		Article article = new Article("A:\\Examples\\example4.pdf");
		ay = SQLQuery.getArticlesYandex(" id = 7030");
		System.out.println(svm.classify(ay.get(0).vector) + "\n" + ((System.currentTimeMillis() - time1)/1000.0));

	}

	public MulticlassSVM(ArrayList<String> classes){
		labels = classes;

		int numberOfTerms = SQLQuery.getCountOfWords();
		for(int i = 0; i < numberOfTerms; i++){
			attributes.add(new Attribute("term_" + i));
		}
		attributes.add(new Attribute("class", labels));

		instances = new Instances("Data", attributes, 0);

		instances.setClassIndex(attributes.size() - 1);



	}

	public void addDocument(ru.lisaprog.objects.Vector vector, String classOfDocument){

		Couple couple = getMassives(vector);
		couple.indexes[couple.indexes.length - 1] = attributes.size() - 1;
		couple.values[couple.values.length - 1] = labels.indexOf(classOfDocument);

		instances.add(new SparseInstance(0.2, couple.values, couple.indexes, attributes.size()));
	}

	public void buildClassifier() throws Exception{
		svm.buildClassifier(instances);
	}

	public String classify(ru.lisaprog.objects.Vector vector) throws Exception{

		Instances newIstanses = new Instances("Article", attributes, 0);
		Couple couple = getMassives(vector);
		couple.indexes[couple.indexes.length - 1] = attributes.size() - 1;
		newIstanses.add(new SparseInstance(1.0, couple.values, couple.indexes, attributes.size()));
		newIstanses.setClassIndex(newIstanses.numAttributes() - 1);
		System.out.println(svm.classifyInstance(newIstanses.firstInstance()));
		return labels.get((int) svm.classifyInstance(newIstanses.firstInstance()));

	}

	private static Couple getMassives(ru.lisaprog.objects.Vector vector){
		Couple result = new Couple();
		result.indexes = new int[vector.keySet().size() + 1];
		result.values = new double[vector.keySet().size() + 1];
		int num = 0;
		for(int i : vector.keySet()){
			result.indexes[num] = i;
			result.values[num++] = vector.get(i)*vector.getNorm();
		}
		return result;
	}

	private static class Couple{
		int[] indexes;
		double[] values;
	}





}


