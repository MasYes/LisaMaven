package ru.lisaprog.classifiers;

import ru.lisaprog.articles.Article;
import ru.lisaprog.objects.Vector;

import java.util.HashSet;

/**
 * Created by Юлиан on 11.05.14.
 */
public class RandomForest implements Classifier{
	private weka.classifiers.trees.RandomForest rf = new weka.classifiers.trees.RandomForest();

	private WekaInstance wekaInstance;

	public RandomForest(WekaInstance wekaInstance){
		this.wekaInstance = wekaInstance;
	}

	public String findUDC(Article article, HashSet<String> UDCs){return "";}

	public String classify(String[] document) throws Exception{
		return classify(Vector.toVector(document));
	}

	public void buildClassifier() throws Exception{
		if(wekaInstance.instances == null)
			wekaInstance.createInstance();
		rf.buildClassifier(wekaInstance.instances);
	}

	public String classify(Vector vector) throws Exception{
		return wekaInstance.labels.get((int) rf.classifyInstance(wekaInstance.prepareForClassification(vector).firstInstance()));
	}
}
