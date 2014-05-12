package ru.lisaprog.datamining;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;
import ru.lisaprog.articles.yandex.ArticleYandex;

import java.util.HashMap;

/**
 * Created by Юлиан on 14.04.14.
 * CREATE TABLE lisa.mutual_information(udc char(32), term_id int, MI double);
 */
public class MutualInformation {

	private double log2Value = Math.log(2);

	private HashMap<String, Vector> vectors;
	private HashMap<String, Integer> sizesOfClasses;
	private HashMap<String, Int2DoubleOpenHashMap> mutualInformations;
	double N = 0;

	public MutualInformation(){
		vectors = new HashMap<>();
		sizesOfClasses = new HashMap<>();
	}

	public void addVector(String udc, Vector vector){
		N++;
		if(!vectors.containsKey(udc))
			vectors.put(udc, new Vector());
		for(int key : vector.keySet()){
			if(!vectors.get(udc).containsKey(key))
				vectors.get(udc).put(key, 0);
			vectors.get(udc).put(key, vectors.get(udc).get(key) + 1);
		}
		if(!sizesOfClasses.containsKey(udc))
			sizesOfClasses.put(udc, 0);
		sizesOfClasses.put(udc, sizesOfClasses.get(udc) + 1);
	}

	public void calculateMI(){
		mutualInformations = new HashMap<>();
		IntOpenHashSet terms = new IntOpenHashSet();
		for(String i : vectors.keySet()){
			terms.addAll(vectors.get(i).keySet());
		}


		for(int i : terms){
			int to = calculateTermOccurrences(i);
			for(String udc : vectors.keySet()){
				if(!mutualInformations.containsKey(udc))
					mutualInformations.put(udc, new Int2DoubleOpenHashMap());
				double N11 = vectors.get(udc).get(i);
				double N10 = to - N11;
				double N01 = sizesOfClasses.get(udc) - N11;
				double N00 = N - N01 - N10 - N11;
				double N1_ = N11 + N10;
				double N0_ = N01 + N00;
				double N_1 = N11 + N01;
				double N_0 = N00 + N10;
				double MI = 0;
				if(N11 > 0)
					MI += N11/N*log2((N * N11) / (N1_ * N_1));
				if(N01 > 0)
					MI += N01/N*log2((N * N01) / (N0_ * N_1));
				if(N10 > 0)
					MI += N10/N*log2((N * N10) / (N1_ * N_0));
				if(N00 > 0)
					MI += N00/N*log2((N * N00) / (N0_ * N_0)); //Зато понятно :)
				mutualInformations.get(udc).put(i, MI);
			}
		}
	}

	public void saveMutualInformation(){
		SQLQuery.saveMutualInformation(mutualInformations);
	}

	private int calculateTermOccurrences(int term){
		int result = 0;
		for(String i : vectors.keySet()){
			result += vectors.get(i).get(term);
		}
		return result;
	}

	private double log2(double d){
		return Math.log(d)/log2Value;
	}



}
