package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import ru.lisaprog.Common;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.objects.UDC;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.articles.Article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

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
		System.out.println("% of correct" + (1 - 1.0*errors/ numberOfDocuments));
		return 1 - 1.0*errors/ numberOfDocuments;
	}

	public static double testClassifier(Classifier classifier, String[] udcs){
		int nsb = 0;
		HashMap<String, Integer> u = new HashMap<>();
		HashMap<String, Integer> v = new HashMap<>();
		HashMap<String, Integer> uANDv = new HashMap<>();
		for(String i : udcs){
			u.put(i, 0);
			uANDv.put(i, 0);
			v.put(i, 0);
		}
		int errors = 0;
		int problems = 0;
		int numberOfDocuments = 0;
		ArrayList<String> labels = new ArrayList<>();
//		labels.addAll(Arrays.asList(udcs));
		IntArrayList articles = SQLQuery.getArticlesForTests(udcs);
		HashMap<String, HashMap<String, Integer>> fails = new HashMap<>();

		for(String i : udcs)
			for(String c : SQLQuery.getUDCByParent(i)){
				fails.put(c, new HashMap<String, Integer>());
				labels.add(c);
			}


		for(int id : articles){
			try{
				String info = SQLQuery.getArticleInfo(id);
				String path = "A:\\articles\\CYBERLENINKA\\" + info.substring(info.lastIndexOf(" ") + 1) + ".txt";
//				System.out.println(path);
				Article art = new Article(path);
				String res = classifier.classify(Lemmer.lemmer(art.text));
				String udc = SQLQuery.getArticleUDC(id).trim();
				boolean fail = true;

				for(String i : udcs)
					if(udc.startsWith(i))
						v.put(i, v.get(i) + 1);

				for(String udcRes : res.split(";")){
					if(udc.startsWith(udcRes) || udc.startsWith(" " + udcRes))
						fail = false;
				}
				if(fail){
					if(notSoBad(udc, res))
						nsb++;
					for(String i : udcs)
						if(res.contains(i))
							u.put(i, u.get(i) + 1);

					errors++;
					System.out.println("Error: " + res + "   ===   " + udc +  "  ===   " + art.vector.size());
					res = res.substring(2);
					while(udc.length() > 2){
						if(labels.contains(udc)){
							if(!fails.get(udc).containsKey(res))
								fails.get(udc).put(res, 0);
							fails.get(udc).put(res, fails.get(udc).get(res) + 1);
						}
						udc = udc.substring(0, udc.length() - 1);
					}

				}
				else{
					for(String i : udcs)
						if(res.contains(i)){
							u.put(i, u.get(i) + 1);
							uANDv.put(i, uANDv.get(i) + 1);
						}
						System.out.println("Success: " + res + "   ===   " + udc +  "  ===   " + art.vector.size());
				}
				numberOfDocuments++;
			}
			catch(Exception e){
				Common.createLog(e);
				problems++;
			}
		}
		ArrayList<Integer> list = new ArrayList<>();
		for(String udc : fails.keySet())
			for(String fail : fails.get(udc).keySet())
				if(!list.contains(fails.get(udc).get(fail)))
					list.add(fails.get(udc).get(fail));

		Collections.sort(list);


//		for(int i = 0; i < list.size() - 3; i++)
//			for(String udc : fails.keySet())
//				for(String fail : fails.get(udc).keySet())
//					if(fails.get(udc).get(fail).equals(list.get(list.size() - 1 - i)))
//						System.out.println(udc + "  ==  " + fail + "  ==  " + fails.get(udc).get(fail));

		double fm = 0;
		for(String i : udcs)
			fm += fMeasure(u.get(i), v.get(i), uANDv.get(i));

		System.out.println("Errors = " + errors);
		System.out.println("NotSoBad = " + nsb);
		System.out.println("Problems = " + problems);
		System.out.println("nod = " + numberOfDocuments);
		System.out.println("F-Measure = " + fm/udcs.length);
		return 1 - 1.0*errors/ numberOfDocuments;
	}


	private static double fMeasure(double u, double v, double uANDv){
		if(u*v*uANDv == 0)
			return 0;
		double p = uANDv/u;
		double r = uANDv/v;
		return 2*p*r/(p+r);

	}


	private static boolean notSoBad(String realUdc, String result){
		for(String res : result.split(";")){
			if(res.equals("4"))
				continue;
			if(res.equals("51") && (realUdc.startsWith("004") || realUdc.startsWith(" 004") ))
				return true;
			if(res.equals("55") && (realUdc.startsWith("622") || realUdc.startsWith(" 622") ))
				return true;
			if(res.equals("53") && (realUdc.startsWith("621") || realUdc.startsWith(" 621") ))
				return true;
			if(res.equals("33") && (realUdc.startsWith("658") || realUdc.startsWith(" 658") ))
				return true;
			if(res.equals("81") && (realUdc.startsWith("801") || realUdc.startsWith(" 801") ))
				return true;
			if(res.equals("51") && (realUdc.startsWith("681") || realUdc.startsWith(" 681") ))
				return true;
			if(res.equals("33") && (realUdc.startsWith("378") || realUdc.startsWith(" 378") ))
				return true;
			if(res.equals("37") && (realUdc.startsWith("159.9") || realUdc.startsWith(" 159.9") ))
				return true;
		}
		return false;
	}

}
