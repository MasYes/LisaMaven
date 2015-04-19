package ru.lisaprog;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Юлиан on 28.03.15.
 */
public class TF {

	private final Object2IntOpenHashMap<String> tf;
	private final HashMap<String, HashSet<String>> sequences;

	public TF(){
		tf = new Object2IntOpenHashMap<>();
		sequences = new HashMap<>();
	}

	public TF(Set<String> dictionary){
		tf = new Object2IntOpenHashMap<>(dictionary.size());
		sequences = new HashMap<>();
		for(String i : dictionary)
			tf.put(i, 0);
		createSequences(dictionary);
	}

	private void createSequences(Set<String> dictionary){
		for(String term : dictionary){
			String[] seq = term.split(" ");
			if(seq.length > 1){
				String key = seq[0];
				if(!sequences.containsKey(key))
					sequences.put(key, new HashSet<String>());
				StringBuilder ngramm = new StringBuilder().append(key);
				for(int i = 1; i < seq.length; i++){
					ngramm.append(" ").append(seq[i]);
					sequences.get(key).add(ngramm.toString());
				}
			}
		}
	}

	public Object2IntOpenHashMap<String> mapOfTheString(String str){
		return processString(str, new Object2IntOpenHashMap<String>());
	}

	public void addString(String str){
		processString(str, this.tf);
	}

	private Object2IntOpenHashMap<String> processString(String str, Object2IntOpenHashMap<String> tf){
		String[] text = str.split(" ");
		for(int i = 0; i < text.length;){
			int index = findSequence(i, text);
			StringBuilder term = new StringBuilder().append(text[i]);
			for(i++; i < index; i++)
				term.append(" ").append(text[i]);
			tf.addTo(term.toString(), 1);
		}
		return tf;
	}

	private int findSequence(int index, String[] text){
		int lastIndex = index + 1;
		if(sequences.containsKey(text[index])){
			String key = text[index];
			StringBuilder term = new StringBuilder().append(key);
			for(int i = index + 1; i < text.length; i++){
				term.append(" ").append(text[i]);
				if(tf.containsKey(term.toString()))
					lastIndex = i + 1;
				if(!sequences.get(key).contains(term.toString()))
					break;
			}
		}
		return lastIndex;
	}

	public Object2IntOpenHashMap<String> getTf() {
		return tf;
	}
}
