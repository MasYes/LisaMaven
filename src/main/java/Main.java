import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import ru.lisaprog.articles.*;
import ru.lisaprog.classifiers.*;
import ru.lisaprog.articles.yandex.*;

import java.io.FileInputStream;
import java.lang.* ;
import java.util.*;

import javolution.text.TypeFormat;
import ru.lisaprog.objects.Term;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;
import weka.classifiers.functions.LibSVM;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 13.07.13
 * Time: 1:05
 * To change this template use File | Settings | File Templates.
 * \\p{Punct}\n]
 *
 * В целом, как я и говорил, вся проблема с парсером... Углы очень большие.
 * Да, между похожими они меньше, но всё равно... И с леммером надо разобраться.
 * Но так, если в целом, то всё даже работает....
 */
public class Main {



	public static void main(String[] args)throws Exception{
		Article art = new Article("A:\\Dplm.pdf");
		for(int i : art.findClose()){
			System.out.println(i);
		}
		System.exit(10);


		HashMap<String, Double> words = new HashMap<>();
		HashMap<String, Integer> ids = new HashMap<>();
		DoubleArrayList list = new DoubleArrayList();
		Article article = new Article("A:\\dplm.pdf");
		for(int i : article.vector.keySet()){
			Term term = SQLQuery.getWordData(i);
			ids.put(term.getWord(), i);
			term.computeMeasure();
			double d = term.getMeasure();
			list.add(d);
			words.put(term.getWord(), d);
		}
		Collections.sort(list);
		HashSet<String> keywords = new HashSet<>();
		for(int i = 0; i < 45; i++){
			for(String word : words.keySet()){
//				System.out.println(article.vector.get(ids.get(word))*article.vector.getNorm());
				if(list.get(i).equals(words.get(word)) && article.vector.get(ids.get(word))*article.vector.getNorm() > 2)
					keywords.add(word);
			}
		}

		for(String i : keywords)
			System.out.println(i);

	}




	private static void sss() throws Exception{

				int[] assigments = new int[]{};

				double[] data={0.23,0.16,0.23,0.36,0.33,0.20,0.16,0.1,0.33,0.43,0.52,0.9,0.27,0.41,0.70};
				ArrayList<String> labels = new ArrayList<String>();
				labels.add("1");
				ArrayList<Attribute> attributes = new ArrayList<Attribute>(1);
				attributes.add(new Attribute("value"));
//				attributes.add(new Attribute("class", labels));
				Instances instances = new Instances("Data", attributes, 0);
//				instances.setClassIndex(attributes.size() - 1);
				for (double d:data){
					double[] values=new double[2];
					int[] indexes=new int[2];
					values[0]=d;
					values[1]=0;
					indexes[0]=0;
					indexes[1]=1;
					instances.add(new SparseInstance(1, values, indexes, attributes.size()));
				}
				SimpleKMeans kmeansClusterer = new SimpleKMeans();
				kmeansClusterer.setNumClusters(5);
				kmeansClusterer.setPreserveInstancesOrder(true);
				kmeansClusterer.buildClusterer(instances);
				assigments = kmeansClusterer.getAssignments();

				for(int i = 0; i <= assigments.length; i++)
					System.out.println(assigments[i]);

		}







	}






