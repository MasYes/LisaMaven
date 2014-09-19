package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import ru.lisaprog.Common;
import weka.core.*;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ru.lisaprog.objects.Vector;

/**
 * Created by Юлиан on 11.05.14.
 */
public class WekaInstance {

	protected Instances instances;
	protected ArrayList<String> labels;
	private HashMap<String, ArrayList<Vector>> vectors;
	private ArrayList<Attribute> attributes;
	private Int2IntOpenHashMap old2newTermId;

	public WekaInstance(){
		vectors = new HashMap<>();
		attributes = new ArrayList<>();
		old2newTermId = new Int2IntOpenHashMap();
	}

	public void addVector(String udc, Vector vector){
		if(!vectors.containsKey(udc))
			vectors.put(udc, new ArrayList<Vector>());
//		for(Vector subVector : splitIntoVectors(vector))
		vectors.get(udc).add(vector);
		for(int i : vector.keySet()){
			if(!old2newTermId.containsKey(i))
				old2newTermId.put(i, old2newTermId.size());
		}
	}

	public void createInstance(){
		for(int i = 0; i <= old2newTermId.size(); i++)
			attributes.add(new Attribute("term_" + i));
		labels = new ArrayList<>(vectors.keySet());
		attributes.add(new Attribute("class", labels));
		instances = new Instances("Data", attributes, 0);
		instances.setClassIndex(attributes.size() - 1);
		for(String udc : vectors.keySet()){
			for(Vector vector : vectors.get(udc)){
				Couple couple = getMassives(vector);
				couple.indexes[couple.indexes.length - 1] = attributes.size() - 1;
				couple.values[couple.indexes.length - 1] = labels.indexOf(udc);
				instances.add(new SparseInstance(1, couple.values, couple.indexes, attributes.size()));
			}
		}
	}

	protected Instances prepareForClassification(Vector vector){
		Couple couple = getMassives(vector);
		Instances result = new Instances("UnknownArticle", attributes, 0);
		result.add(new SparseInstance(1, couple.values, couple.indexes, attributes.size()));
		result.setClassIndex(result.numAttributes() - 1);
		return result;
	}

	private Couple getMassives(Vector vector){
		Couple result = new Couple();
		vector = toNewTermIds(vector);
		IntArrayList list = new IntArrayList(vector.keySet());
		Collections.sort(list);
		result.indexes = new int[vector.keySet().size() + 1];
		result.values = new double[vector.keySet().size() + 1];
		int sum = 0;
		for(int i : vector.keySet()){
			sum += Math.round(vector.get(i)*vector.getNorm());
		}
		int num = 0;
		for(int i : list){
			result.indexes[num] = i;
			result.values[num++] = vector.get(i)*vector.getNorm()/sum;
		}
		return result;
	}

	private Vector toNewTermIds(Vector vector){
		Vector result = new Vector();
		for(int i : vector.keySet()){
			if(old2newTermId.containsKey(i))
				result.put(old2newTermId.get(i), vector.get(i)*vector.getNorm());
		}
		result.normalize();
		return result;
	}

/*	private ArrayList<Vector> splitIntoVectors(Vector vector){
		int partitionSize = 10050000;
		Vector subVector = new Vector();
		ArrayList<Vector> result = new ArrayList<>();
		int count = 0;
		for(int i : vector.keySet()){
			count++;
			subVector.put(i, vector.get(i)*vector.getNorm());
			if(count == partitionSize){
				result.add(subVector);
				subVector.normalize();
				subVector = new Vector();
				count = 0;
			}
		}
		if(count > 0){
			subVector.normalize();
			result.add(subVector);
		}
		return result;
	}*/

	protected void saveInstanceInFile(String fileName){
		try{
			FileWriter fw = new FileWriter("A:\\SVMTests\\" + fileName + ".arff");
			fw.write(this.instances.toString());
			fw.close();
		}catch(Exception e){
			Common.createLog(e);
		}
	}

	private static class Couple{
		int[] indexes;
		double[] values;
	}

}
