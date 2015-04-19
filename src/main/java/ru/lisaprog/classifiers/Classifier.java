package ru.lisaprog.classifiers;

import ru.lisaprog.articles.Article;
import ru.lisaprog.objects.Vector;

import java.util.HashSet;

/**
 * Created by Юлиан on 30.03.14.
 */
public interface Classifier {
	public String findUDC(Article article, HashSet<String> UDCs);

	public String classify(String[] document) throws Exception;

//	public void addDocument(String udc, Vector vector);

	public String classify(Vector vector) throws Exception;

	public void buildClassifier() throws Exception;
}
