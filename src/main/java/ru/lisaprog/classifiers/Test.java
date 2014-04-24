package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.articles.Article;

/**
 * Created by Юлиан on 30.03.14.
 */
public class Test {

	public static double testClassifier(Classifier classifier, String parent){
		int errors = 0;
		int problems = 0;
		int numberOfDocuments = 0;
		IntArrayList articles = SQLQuery.getArticlesForTests(parent);
		System.out.println("Size = " + articles.size());
		for(int id : articles){
			try{
				String info = SQLQuery.getArticleInfo(id);
				String path = "A:\\articles\\CYBERLENINKA\\" + info.substring(info.lastIndexOf(" ") + 1) + ".pdf";
				Article art = new Article(path);
				String res = classifier.findUDC(art, SQLQuery.getUDCByParent(parent));
				String udc = SQLQuery.getArticleUDC(id).trim();
				if(!udc.startsWith(res) && !udc.startsWith(" " + res)){
					errors++;
					System.out.println(res + "   ===   " + udc +  "  ===   " + art.vector.size());
				}
				numberOfDocuments++;
			}
			catch(Exception e){
			}
		}
		System.out.println("Errors = " + errors);
		System.out.println("Problems = " + problems);
		System.out.println("nod = " + numberOfDocuments);
		return 1 - 1.0*errors/ numberOfDocuments;
	}



}
