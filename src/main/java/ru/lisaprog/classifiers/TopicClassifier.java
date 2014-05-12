package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import ru.lisaprog.articles.Article;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.objects.Term;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Юлиан on 19.04.14.
 */
public class TopicClassifier {

	public static void test() throws Exception{
//		Article article = new Article("A:\\Examples\\example9.pdf");
		TopicClassifier tc = new TopicClassifier();
/*		ArrayList<ArticleYandex> ay = SQLQuery.getArticlesYandex(" udc like '51%'");
		for(ArticleYandex articleYandex : ay){
			System.out.println(articleYandex.udc + " " + articleYandex.rank);
			tc.addVector(articleYandex.udc, articleYandex.vector);
			if(articleYandex.rank.equals("15")){
				try{
					SQLQuery.disconnect();
				}catch(Exception e){

				}
			}
		}
		System.out.println("Build classifier");
		tc.buildClassifier();*/
		tc.readProbabilitiesFromFile(new File("A:\\probabilities.txt"));
		Article article = new Article("A:\\Examples\\example9.pdf");
		System.out.println("Classify\n\n");
		tc.classify1(Lemmer.lemmer(article.text));
//		System.out.println("Classify\n\n");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("Classify\n\n");
//		tc.classify(Lemmer.lemmer(article.text));
//		System.out.println("Classify\n\n");
//		tc.classify(Lemmer.lemmer(article.text));
	}



	private HashMap<String, HashMap<String, Double>> terms2Topics;
	private ArrayList<String> allTopics;
	private HashMap<String, Double> countOfTopic = new HashMap<>();

	public TopicClassifier(){
		terms2Topics = new HashMap<>();
		allTopics = new ArrayList<>();
	}

	public void addVector(String udc, ru.lisaprog.objects.Vector vector){
		for(int key : vector.keySet()){
			Term term = SQLQuery.getWordData(key);
//			if(term.getUnits() < 20000){
				String word = term.getWord();
				int frequency = (int)Math.round(vector.get(key)*vector.getNorm());
				if(!terms2Topics.containsKey(word))
					terms2Topics.put(word, new HashMap<String, Double>());
				if(!terms2Topics.get(word).containsKey(udc))
					terms2Topics.get(word).put(udc, 0.0);
				terms2Topics.get(word).put(udc, terms2Topics.get(word).get(udc) + frequency);
//				terms2Topics.get(word).put(udc, terms2Topics.get(word).get(udc) + 1);
				if(!allTopics.contains(udc))
					allTopics.add(udc);
			if(!countOfTopic.containsKey(udc))
				countOfTopic.put(udc, 0.0);
			countOfTopic.put(udc, countOfTopic.get(udc) + 1);
//			}
		}
	}

	public void buildClassifier(){
		HashSet<String> removeWords = new HashSet<>();
		for(String word : terms2Topics.keySet()){
			int sum = 0;
//			HashSet<String> remove = new HashSet<>();
//			for(String topic : terms2Topics.get(word).keySet()){
//				if(terms2Topics.get(word).get(topic) < 5)
//					remove.add(topic);
//			}
//			for(String topic : remove){
//				terms2Topics.get(word).remove(topic);
//			}

			for(String topic : terms2Topics.get(word).keySet()){
				sum += terms2Topics.get(word).get(topic);
			}
			if(sum == 0){
				removeWords.add(word);
				continue;
			}
			for(String topic : terms2Topics.get(word).keySet()){
				terms2Topics.get(word).put(topic, 1.0 * terms2Topics.get(word).get(topic) / sum);
			}
		}
		for(String word : removeWords)
			terms2Topics.remove(word);


		double sum = 0;
		for(String key : countOfTopic.keySet())
			sum += countOfTopic.get(key);
		for(String key : countOfTopic.keySet())
			countOfTopic.put(key, -1*Math.log(countOfTopic.get(key)/sum));
	}

	/*public void classify(String[] document){ // раскидывает уникальные слова

		System.out.println("Doc size = " + document.length);


		HashMap<String, String> topics = new HashMap<>();
		for(int i = 0; i < 1; i++){
			for(String term : document){
				if(terms2Topics.containsKey(term))
					topics.put(term, selectTopicForTerm(term));
			}
		}
		System.out.println("Topics size = " + topics.size());
		ArrayList<String> alOfTopics = new ArrayList<>(topics.values());
		for(String topic : new HashSet<>(alOfTopics)){
			System.out.println(topic + "   ===   " + Collections.frequency(alOfTopics, topic));
		}
	}*/

	/*public void classify(String[] document){ // раскидывает все слова

		System.out.println("Doc size = " + document.length);


		ArrayList<String> topics = new ArrayList<>();
		for(int i = 0; i < 1; i++){
			for(String term : document){
				if(terms2Topics.containsKey(term))
					topics.add(selectTopicForTerm(term));
			}
		}
		System.out.println("Topics size = " + topics.size());
		for(String topic : new HashSet<>(topics)){
			System.out.println(topic + "   ===   " + Collections.frequency(topics, topic));
		}
	}*/

	public void classify(String[] document){ // считаем мат. ожидания
		System.out.println("888 = " + terms2Topics.keySet().size());
		HashMap <String, Double> topics = new HashMap<>();
		for(int i = 0; i < 1; i++){
			for(String term : document){
				if(terms2Topics.containsKey(term))
					for(String topic : terms2Topics.get(term).keySet()){
						if(!topics.containsKey(topic))
							topics.put(topic, 0.0);
						topics.put(topic, topics.get(topic) + terms2Topics.get(term).get(topic));
					}
			}
		}


		DoubleArrayList list = new DoubleArrayList(topics.values());

		Collections.sort(list);

		for(int i = 0; i < 3; i++){
			for(String topic : topics.keySet()){
				if(topics.get(topic).equals(list.get(list.size() - 1 - i)))
					System.out.println(topic + "   ===   " + topics.get(topic));
			}
		}

		System.out.println("________________________");

		list.clear();
		for(String topic : topics.keySet())
			if(countOfTopic.containsKey(topic))
				list.add(topics.get(topic)*countOfTopic.get(topic));

		Collections.sort(list);

		for(int i = 0; i < 3; i++){
			for(String topic : topics.keySet()){
				if(topics.get(topic)*countOfTopic.get(topic) == list.get(list.size() - 1 - i))
					System.out.println(topic + "   ===   " + topics.get(topic)*countOfTopic.get(topic));
			}
		}

		System.out.println("Topics size = " + topics.size());

		System.out.println("Size of count = " + countOfTopic.keySet().size());

	}

	private void readProbabilitiesFromFile(File file) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while(!((line = reader.readLine()) == null)){
			String[] args = line.split("\t");
			if(!terms2Topics.containsKey(args[0]))
				terms2Topics.put(args[0], new HashMap<String, Double>());
			terms2Topics.get(args[0]).put(args[1], Double.parseDouble(args[2]));
			if(!allTopics.contains(args[1]))
				allTopics.add(args[1]);
			if(!countOfTopic.containsKey(args[1]))
				countOfTopic.put(args[1], 0.0);
			countOfTopic.put(args[1], countOfTopic.get(args[1]) + 1);
		}
		double sum = 0;
		for(String key : countOfTopic.keySet())
			sum += countOfTopic.get(key);
		for(String key : countOfTopic.keySet())
			countOfTopic.put(key, -1*Math.log(countOfTopic.get(key)/sum));
	}


	public void classify1(String[] document){ // Оппа Марков стайл!
		try{
	/*	FileWriter fw = new FileWriter("A:\\probabilities.txt");

		fw.write("term\t");

		for(String topic : allTopics)
			fw.write(topic + "\t");

		fw.write("\n");


		for(String term : terms2Topics.keySet()){
			for(String topic : terms2Topics.get(term).keySet())
				fw.write(term + "\t" + topic + "\t" + terms2Topics.get(term).get(topic) + "\n");
		}

		fw.close();

		System.exit(100500);*/

		document = removeUnknown(document);

		}catch(Exception e){}


		double alpha = 1.0;
		String[] topics = new String[document.length];

		for(int i = 0; i < 1000; i++){
//			System.out.println("Iter = " + i);
			for(int termId = 0; termId < document.length; termId++){
				String term = document[termId];
				double[] probabilities = new double[allTopics.size()];
				for(String topic : terms2Topics.get(term).keySet()){
					probabilities[allTopics.indexOf(topic)] = terms2Topics.get(term).get(topic);
				}
				if(termId > 0 && i > 0)
					probabilities[allTopics.indexOf(topics[termId-1])]+=alpha;
				if(termId > 1 && i > 0)
					probabilities[allTopics.indexOf(topics[termId-2])]+=alpha;
				if(termId > 2 && i > 0)
					probabilities[allTopics.indexOf(topics[termId-3])]+=alpha;
				if(termId < document.length-1 && i > 0)
					probabilities[allTopics.indexOf(topics[termId+1])]+=alpha;
				if(termId < document.length-2  && i > 0)
					probabilities[allTopics.indexOf(topics[termId+2])]+=alpha;
				if(termId < document.length-3  && i > 0)
					probabilities[allTopics.indexOf(topics[termId+3])]+=alpha;

				topics[termId] = allTopics.get(selectTopic(normalize(probabilities)));


			}
		}

		ArrayList<String> alTopics = new ArrayList<>(Arrays.asList(topics));
		for(String topic : new HashSet<>(alTopics)){
			System.out.println(topic + "   ===   " + Collections.frequency(alTopics, topic));
		}
		System.out.println("______________________");
		HashMap<String, HashMap<String, Integer>> terms = new HashMap<>();
		for(int i = 0; i < document.length; i++){
			String term = document[i];
			if(!terms2Topics.containsKey(term))
				continue;
			if(!terms.containsKey(term))
				terms.put(term, new HashMap<String, Integer>());
			if(!terms.get(term).containsKey(topics[i]))
				terms.get(term).put(topics[i], 0);
			terms.get(term).put(topics[i], terms.get(term).get(topics[i]) + 1);
		}

		HashMap<String, Integer> topicsCounts = new HashMap<>();
		for(String i : allTopics){
			topicsCounts.put(i, 0);
		}

		for(String term : terms.keySet()){
			String topic = "";
			int max = -1;
			for(String themes : terms.get(term).keySet())
				if(terms.get(term).get(themes) > max){
					topic = themes;
					max = terms.get(term).get(themes);
				}
			topicsCounts.put(topic, topicsCounts.get(topic) + 1);
//			System.out.println(term + "   ===   " + topic);
		}

		for(String topic : topicsCounts.keySet())
			System.out.println(topic + "   ===   " + topicsCounts.get(topic)*countOfTopic.get(topic));


		/*

				HashMap<String, HashSet<String>> terms = new HashMap<>();
		for(String topic : new HashSet<>(alTopics)){
			terms.put(topic, new HashSet<String>());
			for(int i = 0; i < document.length; i++){
				if(topics[i].equals(topic))
					terms.get(topic).add(document[i]);
			}
		}
		for(String topic : terms.keySet()){
			System.out.println(topic + " = " + terms.get(topic));
		}*/





	}




	private String selectTopicForTerm(String term){
		HashMap<Integer, String> map = new HashMap<>();
		double[] probabilities = new double[terms2Topics.get(term).size()];
		for(String topic : terms2Topics.get(term).keySet()){
			map.put(map.size(), topic);
			probabilities[map.size() - 1] = terms2Topics.get(term).get(topic);
		}
		return map.get(selectTopic(probabilities));
	}


	private int selectTopic(double[] probabilities){
		double sum = probabilities[0];
		int topic = 0;
		double random = Math.random();
		while(random > sum)
			sum += probabilities[++topic];
		return topic;
	}


	private double[] normalize(double[] massive){
		double sum = 0;
		for(double d : massive)
			sum+=d;
		for(int i = 0; i < massive.length; i++)
			massive[i]/=sum;
		return massive;
	}

	private String[] removeUnknown(String[] document){
		String str = "";
		for(String i : document)
			if(terms2Topics.containsKey(i))
				str+=i+";";
		return str.split(";");
	}

}
