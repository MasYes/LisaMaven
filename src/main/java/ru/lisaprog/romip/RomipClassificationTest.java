package ru.lisaprog.romip;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.classifiers.Classifier;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.util.HashSet;

/**
 * Created by Юлиан on 29.03.15.
 */
public class RomipClassificationTest {

	private final HashSet<String> test;

	public RomipClassificationTest(){
		test = new HashSet<>();
		for(String doc : DocumentClasses.getKeySet()){
			if(Math.random() < 0.1)
				test.add(doc);
		}
	}


	public double test(Classifier classifier) throws Exception{
		classifier.buildClassifier();
		int correct = 0;
		int n = 0;
		int tested = 0;
		for(String doc : test){
			if(n++ % 100 == 0)
				System.out.print("\r" + 100.*n/test.size());
			Vector vector = SQLQuery.getArticleRomip(doc);
			if(vector == null)
				continue;
			String cls = classifier.classify(SQLQuery.getArticleRomip(doc));
			if(DocumentClasses.getClassOfDoc(doc).contains(cls)){
				correct++;
			}
			tested++;
		}
		System.out.print("\n");
		return 100.*correct/tested;
	}

	public boolean isForTest(String doc){
		return test.contains(doc);
	}

}
