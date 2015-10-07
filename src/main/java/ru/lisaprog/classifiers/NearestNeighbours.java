package ru.lisaprog.classifiers;

import it.unimi.dsi.fastutil.ints.Int2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.Common;
import ru.lisaprog.TF;
import ru.lisaprog.objects.Vector;

import java.util.*;

/**
 * Created by Юлиан on 07.08.2015.
 */
public class NearestNeighbours {

    private final int k;
    private final TF tf;

    private final ArrayList<Vector> documents;
    private final ArrayList<int[]> labels;

    private final Object2IntOpenHashMap<String> indexes;

    public NearestNeighbours(){
        this(5, new TF());
    }

    public NearestNeighbours(int k){
        this(k, new TF());
    }

    public NearestNeighbours(TF tf){
        this(5, tf);
    }

    public NearestNeighbours(int k, TF tf){
        this.k = k;
        this.tf = tf;
        documents = new ArrayList<>();
        labels = new ArrayList<>();
        indexes = new Object2IntOpenHashMap<>();
    }

    public void setDictionary(HashSet<String> dictionary){
        indexes.clear();
        for(String i : dictionary)
            indexes.put(i, indexes.size() + 1);
    }

    public int[] classify(String document){
        IntArrayList result = new IntArrayList();

        Vector vector = toVector(tf.mapOfTheString(document.toLowerCase()));
        Int2DoubleOpenHashMap distances = new Int2DoubleOpenHashMap();
        for(int i = 0; i < documents.size(); i++)
            distances.put(i, documents.get(i).angle(vector));
        Map<Integer, Double> sorted = Common.sortByValues(distances, true);
        Map<Integer, Double> nns = new Int2DoubleLinkedOpenHashMap();
        Iterator<Integer> iter = sorted.keySet().iterator();
        for(int i = 0; i < k; i++){
            int next = iter.next();
            nns.put(next, sorted.get(next));
        }
        Int2DoubleOpenHashMap weights = new Int2DoubleOpenHashMap();
        for(int i : nns.keySet()){
            for(int cls : labels.get(i)){
                weights.addTo(cls, nns.get(i));
            }
        }
        Map<Integer, Double> sortedWeights = Common.sortByValues(weights, true);
        double sum = Common.sumOfElements(sortedWeights.values());
        double currsum = 0;
        for(int i : sortedWeights.keySet()){
            result.add(i);
            currsum += sortedWeights.get(i);
            if(currsum/sum > 0.6 || result.size() > 3)
                return result.toIntArray();
        }
        return result.toIntArray();
    }

    public void addDocument(String document, int[] labels){
        Vector doc = toVector(tf.mapOfTheString(document.toLowerCase()));
        documents.add(doc);
        this.labels.add(labels);
    }

    private Vector toVector(Map<String, Integer> mapOfDoc){
        for(String i : mapOfDoc.keySet())
            if(!indexes.containsKey(i))
                indexes.put(i, indexes.size() + 1);
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        for(String key : mapOfDoc.keySet()){
            map.put(indexes.getInt(key), mapOfDoc.get(key));
        }
        return new Vector(map);
    }

}
