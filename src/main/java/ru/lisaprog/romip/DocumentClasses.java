package ru.lisaprog.romip;

import ru.lisaprog.Common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Юлиан on 28.03.15.
 */
public class DocumentClasses {

	private static HashMap<String, HashSet<String>> classesOfDocs;
	private static HashSet<String> classes;

	private DocumentClasses(){}

	public static void setClassesOfDocs(){
		try{
			classesOfDocs = new HashMap<>();

			File file = new File("src/main/resources/romip/topicset.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String cls = "c2";
			classes = new HashSet<>();
			classes.add(cls);//поскольку там фигня какая-то в начале файла
			while ((line = reader.readLine()) != null) {
				if(line.startsWith("<topic")){
					cls = line.split(" ")[1].substring(4, line.split(" ")[1].length() - 1);
					classes.add(cls);
					continue;
				}
				if(line.startsWith("<document")){
					String doc = line.split(" ")[1].substring(4, line.split(" ")[1].length() - 3);
					if(!classesOfDocs.containsKey(doc))
						classesOfDocs.put(doc, new HashSet<String>());
					classesOfDocs.get(doc).add(cls);
				}
			}
		}catch (IOException ex){
			Common.createLog(ex);
		}
	}

	public static HashSet<String> getClassOfDoc(String docId){
		if(classesOfDocs == null)
			setClassesOfDocs();
		return classesOfDocs.get(docId);
	}

	public static Set<String> getKeySet(){
		if(classesOfDocs == null)
			setClassesOfDocs();
		return classesOfDocs.keySet();
	}

	public static Set<String> getClasses(){
		if(classesOfDocs == null)
			setClassesOfDocs();
		return classes;
	}

}
