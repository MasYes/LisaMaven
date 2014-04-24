package ru.lisaprog.datamining;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Юлиан on 13.04.14.
 */
public class LDA {

	private ArrayList<ArrayList<String>> documents;
	private ArrayList<Int2IntOpenHashMap> topics;
	private double a = 1;
	private double b = 1;
	private int k;
	private int numberOfTerms = 0;
	private int numberOfIterations = 1;

	private Object2IntOpenHashMap<String> terms;

	private double[][] phi;
	private double[][] theta;
	//При построении модели, в целях ускорения процесса, в них лежат значения
	//соответствующие количествам раз, когда тема появляется в документе/слово с темой.

	public LDA(double alpha, double beta, int numberOfTopics){
		a = alpha;
		b = beta;
		k = numberOfTopics;
		documents = new ArrayList<>();
	}

	protected void addDocument(String[] document){
		ArrayList<String> documentAL = new ArrayList<>();
		Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();
		for(String i : document){
			if(!map.containsKey(i))
				map.put(i, 0);
			map.put(i, map.get(i) + 1);
		}
		IntArrayList values = new IntArrayList(map.values());
		Collections.sort(values);
		int min = 0;
		if(values.size() > 100)
			min = values.getInt(values.size() - 100);
		for(String i : document)
			if(map.get(i) >= min && i.length() > 0)
				documentAL.add(i);


		documents.add(documentAL);
	}

	protected void buildModel(){
//		System.out.println(documents.size());
		calculateNumberOfTerms();
		phi = new double[k][numberOfTerms];
		theta = new double[documents.size()][k];
		initTopics();
		for(int i = 0; i < numberOfIterations; i++){
//			System.out.println("Iteration: " + i);
			iteration();
		}
		calculatePhiTheta();
	}

	private void calculateNumberOfTerms(){
		terms = new Object2IntOpenHashMap<>();
		for(ArrayList<String> document : documents){
			for(String term : document){
				if(!terms.containsKey(term))
					terms.put(term, terms.size());
			}
		}
		numberOfTerms = terms.size();
	}

	private void initTopics(){
		topics = new ArrayList<>();
		for(int docIndex = 0; docIndex < documents.size(); docIndex++){
			topics.add(new Int2IntOpenHashMap());
			for(int termIndex = 0; termIndex < documents.get(docIndex).size(); termIndex++){
				int topic = selectTopic();
				topics.get(docIndex).put(termIndex, topic);
				theta[docIndex][topic]++;
				phi[topic][terms.getInt(documents.get(docIndex).get(termIndex))]++;
			}
		}
	}

	private int selectTopic(){
		double[] probabilities = new double[k];
		for(int i = 0; i < k; i++){
			probabilities[i] = 1.0/k;
		}
		return selectTopic(probabilities);
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

	private void iteration(){
		for(int docIndex = 0; docIndex < documents.size(); docIndex++){
//			System.out.println("Doc index " + docIndex);
//			System.out.println("Doc size " + documents.get(docIndex).size());
			for(int termIndex = 0; termIndex < documents.get(docIndex).size(); termIndex++){
//				System.out.println("Term " + termIndex);
				double[] probabilities = new double[k];
				for(int i = 0; i < k; i++){
					int termId = terms.get(documents.get(docIndex).get(termIndex));
					if(i != topics.get(docIndex).get(termId))
						probabilities[i] = 1.0*(phi[i][termId] + b)/(numberOfTopicOccurrencesInTerms(i) + b*numberOfTerms)*(theta[docIndex][i] + a*k);
					else
						probabilities[i] = 1.0*(phi[i][termId] + b - 1)/(numberOfTopicOccurrencesInTerms(i) + b*numberOfTerms - 1)*(theta[docIndex][i] + a*k - 1);
				}
				changeTopic(docIndex, termIndex, selectTopic(normalize(probabilities)));
			}
		}
	}

	private void changeTopic(int docId, int termIndex, int newTopic){
		int oldTopic = topics.get(docId).get(termIndex);
		if(oldTopic != newTopic){
			int termId = terms.get(documents.get(docId).get(termIndex));
			phi[oldTopic][termId]--;
			theta[docId][oldTopic]--;
			phi[newTopic][termId]++;
			theta[docId][newTopic]++;
			topics.get(docId).put(termIndex, newTopic);
		}
	}

	private int numberOfTopicOccurrencesInTerms(int topic){
		int number = 0;
		for(int term = 0; term < numberOfTerms; term++)
			number += phi[topic][term];
		return number;
	}

	private int numberOfTopicsInDocument(int document){
		int number = 0;
		for(int topic = 0; topic < k; topic++)
			number += theta[document][topic];
		return number;
	}


	private void calculatePhiTheta(){
		for(int term = 0; term < numberOfTerms; term++){
			for(int topic = 0; topic < k; topic++){
				phi[topic][term] = (phi[topic][term] + b)/(numberOfTopicOccurrencesInTerms(topic) + b*numberOfTerms);
			}
		}
		for(int topic = 0; topic < k; topic++){
			for(int document = 0; document < documents.size(); document++){
				theta[document][topic] = (theta[document][topic] + a)/(numberOfTopicsInDocument(document) + a*k);
			}
		}
	}

	public double perplexity(String[] document){


		int docSize = document.length;
		double valuePerplexity = 0;
		double[] queryTheta = new double[k];
		int[] queryTopics = new int[document.length];
		int[] termOccurrencesInDocument = new int[numberOfTerms];

		for(String term : document){
			if(terms.containsKey(term))
				termOccurrencesInDocument[terms.get(term)]++;
		}

		for(int term = 0; term < document.length; term++){
			queryTopics[term] = selectTopic();
			queryTheta[queryTopics[term]]++;
		}
		for(int iteration = 0; iteration < numberOfIterations; iteration++){
			for(int term = 0; term < document.length; term++){
				if(!terms.containsKey(document[term]))
					continue;
				double[] probabilities = new double[k];
				for(int topic = 0; topic < k; topic++){
					if(queryTopics[term] != topic)
						probabilities[topic] = phi[topic][terms.get(document[term])]*(queryTheta[topic] + a);
					else
						probabilities[topic] = phi[topic][terms.get(document[term])]*(queryTheta[topic] - 1 + a);
				}
				int selectedTopic = selectTopic(normalize(probabilities));
				queryTheta[queryTopics[term]]--;
				queryTheta[selectedTopic]++;
				queryTopics[term] = selectedTopic;
			}
		}

		for(int topic = 0; topic < k; topic++)
			queryTheta[topic] = (queryTheta[topic] + a)/(document.length + a*k);

		for(int term = 0; term < numberOfTerms; term++){
			if(termOccurrencesInDocument[term] > 0){
				double sum = 0;
				for(int topic = 0; topic < k; topic++){
					sum += phi[topic][term]*queryTheta[topic];
				}
				valuePerplexity += 1.0*termOccurrencesInDocument[term]/docSize*Math.log(sum);
			}
		}
//		System.out.println(valuePerplexity);
		return valuePerplexity;
	}


}


