package ru.lisaprog.classifiers;

import ru.lisaprog.objects.Vector;

/**
 * Created by Юлиан on 11.05.14.
 */
public class RandomForest {
	private weka.classifiers.trees.RandomForest rf = new weka.classifiers.trees.RandomForest();

	private WekaInstance wekaInstance;

	public RandomForest(WekaInstance wekaInstance){
		this.wekaInstance = wekaInstance;
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
