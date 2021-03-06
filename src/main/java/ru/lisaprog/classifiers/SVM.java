package ru.lisaprog.classifiers;

//http://weka.sourceforge.net/doc.dev/
//http://weka.sourceforge.net/doc.stable/weka/classifiers/functions/LibSVM.html

import ru.lisaprog.articles.Article;
import ru.lisaprog.objects.Vector;
import weka.classifiers.functions.LibSVM;

import java.util.HashSet;

public class SVM implements Classifier{

	private LibSVM svm = new LibSVM();

	private WekaInstance wekaInstance;

	public String findUDC(Article article, HashSet<String> UDCs){return "";}

	public String classify(String[] document) throws Exception{
		return classify(Vector.toVector(document));
	}

	public SVM(WekaInstance wekaInstance){
		this.wekaInstance = wekaInstance;
	}

	public void buildClassifier() throws Exception{
		if(wekaInstance.instances == null)
			wekaInstance.createInstance();
		svm.buildClassifier(wekaInstance.instances);
	}

	public String classify(Vector vector) throws Exception{
		return wekaInstance.labels.get((int) svm.classifyInstance(wekaInstance.prepareForClassification(vector).firstInstance()));
	}
}


