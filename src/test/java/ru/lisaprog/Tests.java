package ru.lisaprog;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import junitparams.JUnitParamsRunner;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import ru.lisaprog.articles.Article;
import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.classifiers.*;
import ru.lisaprog.datamining.LDA;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.datamining.MutualInformation;
import ru.lisaprog.articles.yandex.Common;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by Юлиан on 25.04.14.
 */

@RunWith(JUnitParamsRunner.class)

public class Tests {

	TopicClassifier tc;


	@Test
	@Ignore
	public void mutualInformation(){
		MutualInformation mi = new MutualInformation();
/*		String[] udcs = ("00;01;02;030;050;06;070;08;09;" +
				"101;11;122/129;13;14;159.9;16;17;" +
				"2-1/-9;21/29;" +
				"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
				"502/504;51;52;53;54;55;56;57;58;59;" +
				"60;61;62;63;64;65;66;67;68;69;" +
				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
				"80;81;82;" +
				"902/908;91;92;93/94;").split(";");*/
		String[] udcs = "510.2;510.3;510.6;511;512;514.7;515.1;517.9;519.1;519.2;519.6;519.7;519.83;519.85;".split(";");
		for(String udc : udcs){
			System.out.println(udc + "  " + new Date(System.currentTimeMillis()));
			for(ArticleYandex articleYandex : SQLQuery.getArticlesYandex("udc = '" + udc + "'")){
				mi.addVector(udc, articleYandex.vector);
			}
		}
		mi.calculateMI();
		mi.saveMutualInformation();
	}


	@Test
	@Ignore
	public void testSVM() throws Exception{
		double miLimit = 0.01;
		ArrayList<String> classes = new ArrayList<>();

		String udcs = "51;81;56;" +
	//			"00;01;02;030;050;06;070;08;09;" +
	//			"101;11;122/129;13;14;159.9;16;17;" +
	//			"2-1/-9;21/29;" +
	//	 		"304;305;308;311;314/316;32;33;34;35;36;37;39;" + //303
	//			"502/504;51;52;53;54;55;57;58;59;" + //56
	//			"60;61;62;63;64;65;66;67;68;69;" +
	//			"7.01/.09;71;72;73;74;75;76;77;78;79;" +
	//			"80;81;82;" +
	//			"902/908;91;92;93/94;" +
				"";
		WekaInstance wekaInstance = new WekaInstance();

		IntOpenHashSet terms = new IntOpenHashSet();
		for(String i : udcs.split(";")){
			classes.add(i);
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(i, miLimit));
		}

		Vector test = new Vector();
		Vector test2 = new Vector();

		HashMap<String, Integer> map = new HashMap<>();
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			for(ArticleYandex article : ay){
//				System.out.println(new Date());
				Vector vector = new Vector();
				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
						vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				wekaInstance.addVector(article.udc, vector);
				if(udc.equals("51"))
					test = vector;
				if(udc.equals("81"))
					test2 = vector;
			}
		}
		SVM svm = new SVM(wekaInstance);
		svm.buildClassifier();
//		svm.getModelFromFile("A:\\train.model");
		System.out.println("Test, udc = 51");
		System.out.println(svm.classify(test));
		System.out.println("Test2, udc = 81");
		System.out.println(svm.classify(test2));

		System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		System.out.println(svm.classify(article.vector));

/*		System.out.println("test12, udc = 51");
		for(int i : test.keySet())
			test.put(i, test.get(i)*test.getNorm()-0.01);
		System.out.println(svm.classify(test));
		System.out.println("test22, udc = 81");
		for(int i : test2.keySet())
			test2.put(i, test2.get(i)*test2.getNorm()-0.01);
		System.out.println(svm.classify(test2));*/

	}

	@Test
	@Ignore
	public void testSVM2() throws Exception{

		ArrayList<String> classes = new ArrayList<>();

/*		String udcs =
				"00;01;02;030;050;06;070;08;09;" +
				"101;11;122/129;13;14;159.9;16;17;" +
				"2-1/-9;21/29;" +
		 		"304;305;308;311;314/316;32;33;34;35;36;37;39;" + //303
				"502/504;51;52;53;54;55;57;58;59;" + //56
				"60;61;62;63;64;65;66;67;68;69;" +
				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
				"80;81;82;" +
				"902/908;91;92;93/94;" +
				"";*/
		WekaInstance wekaInstance = new WekaInstance();

		IntOpenHashSet terms = new IntOpenHashSet();


		Vector test = new Vector();
		Vector test2 = new Vector();

		HashMap<String, Integer> map = new HashMap<>();
		String udc = "=51";
		ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + 51 + "';");
		for(ArticleYandex article : ay){
//				System.out.println(new Date());
			Vector vector = new Vector();
			for(int term : article.vector.keySet()){
				if(terms.contains(term))
					vector.put(term, article.vector.get(term)*article.vector.getNorm());
			}
			vector.normalize();
			wekaInstance.addVector(udc, vector);
			if(udc.equals("51"))
				test = vector;
			if(udc.equals("81"))
				test2 = vector;
		}
		udc = "!51";
		ay = SQLQuery.getArticlesYandex("udc = '81' OR udc = '56';");
		for(ArticleYandex article : ay){
//				System.out.println(new Date());
			Vector vector = new Vector();
			for(int term : article.vector.keySet()){
				if(terms.contains(term))
					vector.put(term, article.vector.get(term)*article.vector.getNorm());
			}
			vector.normalize();
			wekaInstance.addVector(udc, vector);
			if(udc.equals("51"))
				test = vector;
			if(udc.equals("!51"))
				test2 = vector;
		}
		SVM svm = new SVM(wekaInstance);
		svm.buildClassifier();
//		svm.getModelFromFile("A:\\train.model");
		System.out.println("Test, udc = 51");
		System.out.println(svm.classify(test));
		System.out.println("Test2, udc = 81");
		System.out.println(svm.classify(test2));

		System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		System.out.println(svm.classify(article.vector));

/*		System.out.println("test12, udc = 51");
		for(int i : test.keySet())
			test.put(i, test.get(i)*test.getNorm()-0.01);
		System.out.println(svm.classify(test));
		System.out.println("test22, udc = 81");
		for(int i : test2.keySet())
			test2.put(i, test2.get(i)*test2.getNorm()-0.01);
		System.out.println(svm.classify(test2));*/

	}

	@Test
	@Ignore
	public void testMSVM() throws Exception{


		String udcs = "51;81;" +
//				"00;01;02;030;050;06;070;08;09;" +
//				"101;11;122/129;13;14;159.9;16;17;" +
//				"2-1/-9;21/29;" +
//		 		"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" + //303
//				"51;52;54;56;57;58;59;" + //56;53;
//				"60;61;62;63;64;65;66;67;68;69;" +
//				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
//				"80;81;82;" +
//				"902/908;91;92;93/94;" +
				"";

		ArrayList<String> labels = new ArrayList<>();

		for(String i : udcs.split(";")){
			labels.add(i);
		}


		MSVM svm = new MSVM(labels);

		svm.buildClassifiers();
//		svm.getModelFromFile("A:\\train.model");
//		System.out.println("Test, udc = 51");
//		System.out.println(svm.classify(test));
//		System.out.println("Test2, udc = 81");
//		System.out.println(svm.classify(test2));

		System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		System.out.println(svm.classify(article.vector));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		System.out.println(svm.classify(article.vector));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		System.out.println(svm.classify(article.vector));

/*		System.out.println("test12, udc = 51");
		for(int i : test.keySet())
			test.put(i, test.get(i)*test.getNorm()-0.01);
		System.out.println(svm.classify(test));
		System.out.println("test22, udc = 81");
		for(int i : test2.keySet())
			test2.put(i, test2.get(i)*test2.getNorm()-0.01);
		System.out.println(svm.classify(test2));*/

	}


	@Test
	@Ignore
	public void testMRF() throws Exception{


		String udcs =
//				"00;01;02;050;06;070;08;09;" + //030
//				"101;11;122/129;13;14;159.9;16;17;" +
//				"2-1/-9;21/29;" +
//		 		"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" + //303
				"51;52;54;56;57;58;59;" + //56;53;
//				"60;61;62;63;64;65;66;67;68;69;" +
//				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
//				"902/908;91;92;93/94;" +
						"";

		ArrayList<String> labels = new ArrayList<>();

		for(String i : udcs.split(";")){
			labels.add(i);
		}


		MRF rf = new MRF(labels);

		rf.buildClassifiers();
//		svm.getModelFromFile("A:\\train.model");
//		System.out.println("Test, udc = 51");
//		System.out.println(svm.classify(test));
//		System.out.println("Test2, udc = 81");
//		System.out.println(svm.classify(test2));

		System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		System.out.println(rf.classify(article.vector));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		System.out.println(rf.classify(article.vector));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		System.out.println(rf.classify(article.vector));

/*		System.out.println("test12, udc = 51");
		for(int i : test.keySet())
			test.put(i, test.get(i)*test.getNorm()-0.01);
		System.out.println(svm.classify(test));
		System.out.println("test22, udc = 81");
		for(int i : test2.keySet())
			test2.put(i, test2.get(i)*test2.getNorm()-0.01);
		System.out.println(svm.classify(test2));*/

	}

	@Test
	@Ignore
	public void testRF() throws Exception{
		double miLimit = 0.0;
		ArrayList<String> classes = new ArrayList<>();


		String udcs =
//				"00;01;02;030;050;06;070;08;09;" +
//				"101;11;122/129;13;14;159.9;16;17;" +
//				"2-1/-9;21/29;" +
//				"304;305;308;311;314/316;32;33;34;35;36;37;39;" + //303
				"80;81;82;" +
				"502/504;51;52;53;54;55;57;58;59;" + //56
//				"60;61;62;63;64;65;66;67;68;69;" +
//				"7.01/.09;71;72;73;74;75;76;77;78;79;" +

//				"902/908;91;92;93/94;" +
				"";

		WekaInstance wekaInstance = new WekaInstance();

		IntOpenHashSet terms = new IntOpenHashSet();
		for(String i : udcs.split(";")){
			classes.add(i);
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(i, miLimit));
		}

		Vector test = new Vector();
		Vector test2 = new Vector();

		HashMap<String, Integer> map = new HashMap<>();
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			for(ArticleYandex article : ay){
//				System.out.println(new Date());
				Vector vector = new Vector();
				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
						vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				wekaInstance.addVector(article.udc, vector);
				if(udc.equals("51"))
					test = vector;
				if(udc.equals("81"))
					test2 = vector;
			}
		}
		RandomForest rf = new RandomForest(wekaInstance);
		rf.buildClassifier();
//		svm.getModelFromFile("A:\\train.model");
		System.out.println("Test, udc = 51");
		System.out.println(rf.classify(test));
		System.out.println("Test2, udc = 81");
		System.out.println(rf.classify(test2));

		System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		System.out.println(rf.classify(article.vector));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		System.out.println(rf.classify(article.vector));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		System.out.println(rf.classify(article.vector));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		System.out.println(rf.classify(article.vector));

/*		System.out.println("test12, udc = 51");
		for(int i : test.keySet())
			test.put(i, test.get(i)*test.getNorm()-0.01);
		System.out.println(svm.classify(test));
		System.out.println("test22, udc = 81");
		for(int i : test2.keySet())
			test2.put(i, test2.get(i)*test2.getNorm()-0.01);
		System.out.println(svm.classify(test2));*/

	}


	@Test
	@Ignore
	public void testTopicClassifier() throws Exception{

		double miLimit = 0;
//		IntOpenHashSet terms = SQLQuery.getMutualInformationWithLimitMI("0", miLimit);
//		terms.addAll()
		TopicClassifier tc = new TopicClassifier();
//		String udcs = "51;81;56;";//
//		String udcs = "502/504;51;52;53;54;55;57;58;59;";

		String udcs = "00;01;02;030;050;06;070;08;09;" +
				"101;11;122/129;13;14;159.9;16;17;" +
				"2-1/-9;21/29;" +
				"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
				"502/504;51;52;53;54;55;56;57;58;59;" +
				"60;61;62;63;64;65;66;67;68;69;" +
				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
				"80;81;82;" +
				"902/908;91;92;93/94;" +
				"";

		System.out.println(SQLQuery.getArticlesForTests(udcs.split(";")).size());

		IntOpenHashSet terms = new IntOpenHashSet();
		for(String udc : udcs.split(";"))
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(udc, miLimit));

		System.out.println("Size = " + terms.size());
		System.out.println("111 " + new Date(System.currentTimeMillis()));
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			System.out.println(udc + " - " + new Date(System.currentTimeMillis()));


			for(ArticleYandex article : ay){

//				System.out.println(new Date());
				Vector vector = new Vector();

				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
						vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				tc.addVector(article.udc, vector);
			}
		}




		System.out.println("Build classifier");
		tc.buildClassifier();

		System.out.println("example004.pdf, udc = 004.93");
		Article article = new Article("A:\\Examples\\example004.pdf");
		System.out.println("Result = " + tc.classify(Lemmer.lemmer(article.text)));

		System.out.println("example_wr.pdf, udc = 51");
		article = new Article("A:\\Examples\\example_wr.pdf");
		System.out.println("Result = " + tc.classify(Lemmer.lemmer(article.text)));

		System.out.println("example_wr.txt, udc = 00");
		article = new Article("A:\\Examples\\example_wr.txt");
		System.out.println("Result = " + tc.classify(Lemmer.lemmer(article.text)));

		/*System.out.println("example.pdf, udc = 51");
		Article article = new Article("A:\\Examples\\example.pdf");
		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example8.pdf, udc = 172");
//		article = new Article("A:\\Examples\\example8.pdf");
//		tc.classify(Lemmer.lemmer(article.text));
		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		tc.classify(Lemmer.lemmer(article.text));
		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("example514.pdf, udc = 515");
//		article = new Article("A:\\Examples\\example514.pdf");
//		tc.classify(Lemmer.lemmer(article.text));


		System.out.println("example.pdf, udc = 51");
		article = new Article("A:\\Examples\\example.pdf");
		tc.classify(Lemmer.lemmer(article.text));

//		System.out.println("example1.pdf, udc = 00");
//		article = new Article("A:\\Examples\\example1.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example2.pdf, udc = 62");
//		article = new Article("A:\\Examples\\example2.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example4.pdf, udc = 51");
		article = new Article("A:\\Examples\\example4.pdf");
		tc.classify(Lemmer.lemmer(article.text));

//		System.out.println("example5.pdf, udc = 63");
//		article = new Article("A:\\Examples\\example5.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example6.pdf, udc = 378?");
//		article = new Article("A:\\Examples\\example6.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example7.pdf, udc = 316");
//		article = new Article("A:\\Examples\\example7.pdf");
//		System.out.println(svm.classify(article.vector));

//		System.out.println("example8.pdf, udc = 172");
//		svm.currClass = "17";
//		article = new Article("A:\\Examples\\example8.pdf");
//		System.out.println(svm.classify(article.vector));

		System.out.println("example9.pdf, udc = 51");
		article = new Article("A:\\Examples\\example9.pdf");
		tc.classify(Lemmer.lemmer(article.text));

		System.out.println("example10.pdf, udc = 811");
		article = new Article("A:\\Examples\\example10.pdf");
		tc.classify(Lemmer.lemmer(article.text));

		System.out.println("example514.pdf, udc = 514");
		article = new Article("A:\\Examples\\example514.pdf");
		tc.classify(Lemmer.lemmer(article.text));

		System.out.println("example81.pdf, udc = 811");
		article = new Article("A:\\Examples\\example81.pdf");
		tc.classify(Lemmer.lemmer(article.text));

		System.out.println("example811.pdf, udc = 811");
		article = new Article("A:\\Examples\\example811.pdf");
		tc.classify(Lemmer.lemmer(article.text));*/




	}

	@Test
//	@Ignore
	public void testClassifier() throws Exception{



		double miLimit = 0.05;
		double perplexityLimit = 1.9;//16
		TopicClassifier tc = new TopicClassifier();
//		Rocchio tc = new Rocchio();
		String udcs = "004.01;004.02;004.03;004.04;004.05;004.07;004.08;" +

//				"00;01;02;030;050;06;070;08;09;" +
//				"101;11;122/129;13;14;159.9;16;17;" +
//				"2-1/-9;21/29;" +
//				"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
//				"502/504;51;52;53;54;55;56;57;58;59;" +
//				"60;61;62;63;64;65;66;67;68;69;" +
//				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
//				"80;81;82;" +
//				"902/908;91;92;93/94;" +
				"";

		System.out.println("Number of udc = " + udcs.split(";").length);



		IntOpenHashSet terms = new IntOpenHashSet();
		ArrayList<String> labels = new ArrayList<>();
		for(String udc : udcs.split(";")){
			labels.add(udc);
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(udc, miLimit));
		}


//		MSVM svm = new MSVM(labels);

//		System.out.println("Size = " + terms.size());
//		System.out.println("111 " + new Date(System.currentTimeMillis()));
		long train = System.currentTimeMillis();
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			System.out.println(udc + " - " + new Date(System.currentTimeMillis()));


			for(ArticleYandex article : ay){
				if(article.perplexity >= perplexityLimit)
					continue;

//				System.out.println(new Date());
				Vector vector = new Vector();

				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
						vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				tc.addVector(article.udc, vector);
			}
		}
		System.out.println("Build classifier");
		tc.buildClassifier();
		train = System.currentTimeMillis() - train;
		long classifing = System.currentTimeMillis();
		Article article = new Article("A:\\Dplm.pdf");
		System.out.println("Result =" +  tc.classify(Lemmer.lemmer(article.text)));
//		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
		System.out.println("Time of training = " + 1.0*train/1000);
		System.out.println("Time of classifing = " + 1.0*(System.currentTimeMillis() - classifing)/1000);
	}



	@Test
	@Ignore
	public void testClassifierAlpha05() throws Exception{
		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
						"101;11;122/129;13;14;159.9;16;17;" +
						"2-1/-9;21/29;" +
						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
						"502/504;51;52;53;54;55;56;57;58;59;" +
						"60;61;62;63;64;65;66;67;68;69;" +
						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
						"902/908;91;92;93/94;" +
						"";

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 0.5;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierAlpha1() throws Exception{



		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = "511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
//				"00;01;02;030;050;06;070;08;09;" +
//						"101;11;122/129;13;14;159.9;16;17;" +
//						"2-1/-9;21/29;" +
//						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
//						"502/504;51;52;53;54;55;56;57;58;59;" +
//						"60;61;62;63;64;65;66;67;68;69;" +
//						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
//						"80;81;82;" +
//						"902/908;91;92;93/94;" +
						"";

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 1.0;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierAlpha5() throws Exception{
		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
						"101;11;122/129;13;14;159.9;16;17;" +
						"2-1/-9;21/29;" +
						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
						"502/504;51;52;53;54;55;56;57;58;59;" +
						"60;61;62;63;64;65;66;67;68;69;" +
						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
						"902/908;91;92;93/94;" +
						"";

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 5.0;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierAlpha10() throws Exception{
		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
						"101;11;122/129;13;14;159.9;16;17;" +
						"2-1/-9;21/29;" +
						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
						"502/504;51;52;53;54;55;56;57;58;59;" +
						"60;61;62;63;64;65;66;67;68;69;" +
						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
						"902/908;91;92;93/94;" +
						"";

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 10.0;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierAlpha20() throws Exception{
		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
						"101;11;122/129;13;14;159.9;16;17;" +
						"2-1/-9;21/29;" +
						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
						"502/504;51;52;53;54;55;56;57;58;59;" +
						"60;61;62;63;64;65;66;67;68;69;" +
						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
						"902/908;91;92;93/94;" +
						"";

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 20.0;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierAlpha100() throws Exception{

		double miLimit = 0.05;
		double perplexityLimit = 2.0;
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
						"101;11;122/129;13;14;159.9;16;17;" +
						"2-1/-9;21/29;" +
						"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
						"502/504;51;52;53;54;55;56;57;58;59;" +
						"60;61;62;63;64;65;66;67;68;69;" +
						"7.01/.09;71;72;73;74;75;76;77;78;79;" +
						"80;81;82;" +
						"902/908;91;92;93/94;" +
						"";


		tc = new TopicClassifier();

		System.out.println("Number of udc = " + udcs.split(";").length);



		IntOpenHashSet terms = new IntOpenHashSet();
		ArrayList<String> labels = new ArrayList<>();
		for(String udc : udcs.split(";")){
			labels.add(udc);
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(udc, miLimit));
		}


//		MSVM svm = new MSVM(labels);

//		System.out.println("Size = " + terms.size());
//		System.out.println("111 " + new Date(System.currentTimeMillis()));
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			System.out.println(udc + " - " + new Date(System.currentTimeMillis()));


			for(ArticleYandex article : ay){
				if(article.perplexity >= perplexityLimit)
					continue;

//				System.out.println(new Date());
				Vector vector = new Vector();

				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
					vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				tc.addVector(article.udc, vector);
			}
		}
		System.out.println("Build classifier");
		tc.buildClassifier();

		System.out.println("Number of udc = " + udcs.split(";").length);
		tc.alpha = 100.0;
		ru.lisaprog.classifiers.Test.testClassifier(tc, udcs.split(";"));
	}

	@Test
	@Ignore
	public void testClassifierWithWeka() throws Exception{
		long train = System.currentTimeMillis();
		WekaInstance wekaInstance = new WekaInstance();

		double miLimit = 0.05;
		double perplexityLimit = 0.75;
//		TopicClassifier tc = new TopicClassifier();
		String udcs = //"511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;" +
				"00;01;02;030;050;06;070;08;09;" +
				"101;11;122/129;13;14;159.9;16;17;" +
				"2-1/-9;21/29;" +
				"303;304;305;308;311;314/316;32;33;34;35;36;37;39;" +
				"502/504;51;52;53;54;55;56;57;58;59;" +
				"60;61;62;63;64;65;66;67;68;69;" +
				"7.01/.09;71;72;73;74;75;76;77;78;79;" +
				"80;81;82;" +
				"902/908;91;92;93/94;" +
				"";

		System.out.println("Number of udc = " + udcs.split(";").length);



		IntOpenHashSet terms = new IntOpenHashSet();
		ArrayList<String> labels = new ArrayList<>();
		for(String udc : udcs.split(";")){
			labels.add(udc);
			terms.addAll(SQLQuery.getMutualInformationWithLimitMI(udc, miLimit));
		}


//		MSVM svm = new MSVM(labels);

//		System.out.println("Size = " + terms.size());
//		System.out.println("111 " + new Date(System.currentTimeMillis()));
		for(String udc : udcs.split(";")){
			ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex("udc = '" + udc + "';");
			System.out.println(udc + " - " + new Date(System.currentTimeMillis()));


			for(ArticleYandex article : ay){
				if(article.perplexity >= perplexityLimit)
					continue;

//				System.out.println(new Date());
				Vector vector = new Vector();

				for(int term : article.vector.keySet()){
//					if(terms.contains(term))
						vector.put(term, article.vector.get(term)*article.vector.getNorm());
				}
				vector.normalize();
				wekaInstance.addVector(article.udc, vector);
			}
		}

		SVM svm = new SVM(wekaInstance);
//		RandomForest svm = new RandomForest(wekaInstance);
		System.out.println("Build classifier");
		svm.buildClassifier();
		train = System.currentTimeMillis() - train;
		long classify = System.currentTimeMillis();
		ru.lisaprog.classifiers.Test.testClassifier(svm, udcs.split(";"));
		System.out.println("Time of training = " + 1.0*train/1000);
		System.out.println("Time of classifing = " + 1.0*(System.currentTimeMillis() - classify)/1000);
	}

	@Test
	@Ignore
	public void testPerplexity() throws Exception{
		File folder = new File("A:\\articles\\YANDEX");
		for(File dir : folder.listFiles() ){
			String udc = new Scanner(new FileInputStream(dir.getAbsoluteFile() + "\\udc.txt")).nextLine();
			System.out.println(dir.getName());
			for(File file : dir.listFiles()){
				if(!file.getName().contains("_oneline") || file.length() < 16)
					continue;
				String rank = file.getName().substring(0, file.getName().indexOf("_"));
				LDA lda = new LDA(1.0/50, 0.01, 50);

				for(File article : dir.listFiles()){
					if(article.getName().contains("_oneline") && ! article.getName().startsWith(rank) && article.length() > 16){
						Scanner scan = new Scanner(article);
						lda.addDocument(Lemmer.lemmer(scan.nextLine()));
					}
				}
				lda.buildModel();
				File article = new File(dir.getAbsoluteFile() + "\\" + rank + "_lemm_oneline.txt");
				Scanner scan = new Scanner(article);
				SQLQuery.savePerplexity(udc, rank, lda.perplexity(Lemmer.lemmer(scan.nextLine())));
			}
		}
	}

	@Test
	@Ignore
	public void testTypicalArticles() throws Exception{
		long time = System.currentTimeMillis();
		Common.calculatePerplexity();
		System.out.println((System.currentTimeMillis() - time)/1000.0);
	}



}
