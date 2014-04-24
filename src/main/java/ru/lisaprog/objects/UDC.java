package ru.lisaprog.objects;

/**
 * Created with IntelliJ IDEA.
 * User: Юлиан
 * Date: 17.08.13
 * Time: 14:11
 * To change this template use File | Settings | File Templates.
 */
import ru.lisaprog.sql.SQLQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class UDC {
	private String id;
	private String description;
	private String parent;
	private String children;



	public UDC(){
		id = "";
		description = "";
		parent = "";
		children = "";
	}

	public UDC(String id, String descr, String par, String childr){
		this.id = id;
		this.description = descr;
		this.parent = par;
		this.children = childr;
	}

	private boolean exist(String str){
		UDC udc = SQLQuery.getUDC(str);
		if(udc == null)
			return false;
		this.id = udc.id;
		this.description = udc.description;
		this.parent = udc.parent;
		this.children = udc.children;
		return true;
	}

	private static HashSet<String> normalize(String code){
		try{
		//ВНИМАНИЕ! Заработало как надо с 1 раза.
		//Этот кусок и все сопутствующие функции
		//надо внимательно проверить!!!!
		//Вызывает аут оф мемори, если " не парные

		/*Переводим сложное УДК к набору более простых.
		Основные моменты:
		1). Убираем всё, что заключено в ""
		2). /. -> во что-то нормальное :)
		3). :: -> +
		4). : -> +
		5). [ и ] -> +
		6). ( и ) -> +
		7). Делаем сплит по +,
		помещаем в результат, проверяя,
		что данные УДК имеются в базе.
		 */

		while(code.contains("\"")){ // МБ потом напишу это через регулярки.
			code = code.substring(0, code.indexOf("\"")) + "+" +
					code.substring(code.indexOf("\"", code.indexOf("\"") + 1) + 1);
		}

		while(code.contains("/.")){ // 3.1/.2 -> 3.1 + 3.2
			code = code.substring(0, code.indexOf("/.")) + "+" + code.substring(0, code.lastIndexOf(".", code.indexOf("/."))) +
					code.substring(code.indexOf("/.") + 1);
		}
		code = code.replaceAll("[\\[:\\]();=*\\?_\\s\\*]", "+");
		while(code.contains("++"))
			code = code.replaceAll("\\+\\+", "+");
		code = code.replaceAll(" ", "+");
		HashSet<String> set = new HashSet<>();
		UDC udc = new UDC();
		for(String i : code.split("\\+")){
			while(i.length() > 0){
				if(udc.exist(i)){
					set.add(i);
					i = udc.parent;
					while(udc.exist(i)){
						set.add(i);
						i = udc.parent;
					}
					break;
				}
				i = i.substring(0, i.length() - 1);
			}
		}
		return set;
		}catch(Exception e){
			return new HashSet<String>();
		}
	}

	public static void computeUDCVectors(){
		HashMap<String, Integer> counts = new HashMap<>();
		HashMap<String, Vector> udc = new HashMap<>();
		int count = SQLQuery.getCountOfArticles();
		for(int i = 1; i <= count; i++){
			System.out.println(i);
			if(i%1000 == 0){
				try{
					SQLQuery.disconnect();
				}catch(Exception e){}
			}
			for(String str : normalize(SQLQuery.getArticleUDC(i))){
				if(udc.containsKey(str)){
					udc.put(str, udc.get(str).add(SQLQuery.getArticleVector(i)));
					counts.put(str, counts.get(str) + 1);
				}
				else{
					udc.put(str, SQLQuery.getArticleVector(i));
					counts.put(str, 1);
				}
			}
		}
		for(String str : udc.keySet()){
			SQLQuery.setUDCVector(str, udc.get(str));
			SQLQuery.setUDCCount(str, counts.get(str));
		}
	}

	public static void computeUDCVectorsFromURL(){
		HashMap<String, Integer> counts = new HashMap<>();
		HashMap<String, Vector> udc = new HashMap<>();
		int count = SQLQuery.getURLCount();
		for(int i = 1; i <= count; i++){
			System.out.println(i);
			if(i%1000 == 0){
				try{
					SQLQuery.disconnect();
				}catch(Exception e){}
			}
			for(String str : normalize(SQLQuery.getArticleUDC(i))){
				if(udc.containsKey(str)){
					udc.put(str, udc.get(str).add(SQLQuery.getArticleVector(i)));
					counts.put(str, counts.get(str) + 1);
				}
				else{
					udc.put(str, SQLQuery.getArticleVector(i));
					counts.put(str, 1);
				}
			}
		}
		for(String str : udc.keySet()){
			SQLQuery.setUDCVector(str, udc.get(str));
			SQLQuery.setUDCCount(str, counts.get(str));
		}
	}

	public static void computeUDCTerms(){
		computeUDCTerms(new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"});
	}

	private static void computeUDCTerms(String[] main){
		Vector[] vold = new Vector[main.length];
		Vector[] vnew = new Vector[main.length];
		int count = 0;
		for(String i : main){
			vold[count] = SQLQuery.getUDCVector(i);
//			for(Integer k : vold[count].keySet())
//				vold[count].put(k, vold[count].get(k)/SQLQuery.getUDCCount(i));
			vnew[count] = SQLQuery.getUDCVector(i);
			count++;
		}

		Vector sum = new Vector();

		for(int i = 0; i < main.length; i++){
			for(int k: vold[i].keySet()){
				sum.put(k, sum.at(k) + vold[i].at(k)*vold[i].getNorm());
			}
		}

		for(int i = 0; i < main.length; i++){
			for(Integer k: vold[i].keySet()){
				if(sum.get(k)*0.5 > vold[i].at(k)*vold[i].getNorm() || vold[i].at(k)*vold[i].getNorm() < 10 ||
						1.0 * SQLQuery.getWordData(k).getUnits() / SQLQuery.getWordData(k).getFrequency() > 0.5||
						SQLQuery.getWordData(k).getFrequency() > 4700)
					vnew[i].remove(k);
			}
		}

		for(int i = 0; i < main.length; i++){
			SQLQuery.setUDCTerms(main[i], vnew[i]);
			if(!SQLQuery.getUDC(main[i]).children.equals("")){
				computeUDCTerms(SQLQuery.getUDC(main[i]).children.split(";"));
			}
		}
	}




	//-----------------------------------------------------------------------------------------------





	private static String findCloseUDCAngle(Vector vect){
		HashSet<Integer> ud = new HashSet<>(vect.keySet());
		String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"};
		String min = "002";
		String ololo = "001.1;001.32;001.8;001.9;";
		double value = SQLQuery.getUDCVector("002").angle(vect);
		for(String i : ololo.split(";")){
			if(value > SQLQuery.getUDCVector(i).angle(vect)){
				value = SQLQuery.getUDCVector(i).angle(vect);
				min = i;
			}
		}
		return min;
	}


	public static String findCloseUDCTerm(Vector vect){
		HashSet<Code> codes = new HashSet<>();
		String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"};
		double sum2 = 0;
		for(String i : main){
			sum2 += vect.angle(SQLQuery.getUDCVector(i));
		}
		//System.out.println(sum2/9 + "\n");
		for(String i : main){
		//	System.out.println(i + "  ===  " + vect.angle(SQLQuery.getUDCVector(i)));
			if(vect.angle(SQLQuery.getUDCVector(i)) < sum2/9.0){
		//		System.out.println("Passed " + i);
				String max = "";
				double value;
				UDC udc = SQLQuery.getUDC(i);
				LinkedHashSet<Integer> set = new LinkedHashSet<>();
				set.add(SQLQuery.getUDCTerms(i).crossingSize(vect, 2));
				while(!udc.children.equals("")){
					value = -1;
					for(String str : udc.children.split(";")){
						Vector terms = SQLQuery.getUDCTerms(str);
						double curr = 0;
						if(terms.size() > 0 && SQLQuery.getUDCCount(str) > 2){
							curr = 1.0*Math.log(vect.crossingSize(terms, 2)) / Math.log(terms.size());
							//curr = 1.0*vect.crossingSize(terms, 2);
						}
						if(curr >= value){
							if(curr > value || vect.angle(terms) > vect.angle(SQLQuery.getUDCTerms(max))){
								max = str;
								value = curr;
							}
						}
					}
					set.add(vect.crossingSize(SQLQuery.getUDCTerms(max), 2));
					udc = SQLQuery.getUDC(max);
				}
				int sum = 0;
				for(int key : set)
					sum+=key;
				sum = sum / set.size();
//				System.out.println(i + "  ===  " + set + "  " + sum);
				codes.add(new Code(max, computeProbability(set)));
			}
		}
		String res = "";
		double aw_an = 0;
		double aw_pr = 0;
		for(Code c : codes){
//			System.out.println("\n" + SQLQuery.getUDC(c.udc).description + "\n" +c.udc + "  ===  " + vect.distanse(SQLQuery.getUDCVector(c.udc), c.udc) + "  ===  " + vect.angle(SQLQuery.getUDCVector(c.udc)) + "  ===  " + vect.crossingSize(SQLQuery.getUDCVector(c.udc)) + "  ===  " + c.likelihood);
			aw_an += vect.angle(SQLQuery.getUDCVector(c.udc));
			aw_pr += c.likelihood;
		}
		aw_an = aw_an/codes.size();
		aw_pr = aw_pr/9;
//		System.out.println(aw_an);
//		System.out.println(aw_pr);
		for(Code c : codes)
				if(c.likelihood >=aw_pr && vect.angle(SQLQuery.getUDCVector(c.udc)) < aw_an)
			//if(vect.angle(SQLQuery.getUDCVector(c.udc)) < vect.angle(SQLQuery.getUDCVector(c.udc.substring(0, 1))))
				res += c.udc + "  " + SQLQuery.getUDC(c.udc).description + "\n";

		return res;
	}

	@Deprecated
	private static String findCloseUDCTermOLD(Vector vect){
		//String ololo = "60;61;62;63;64;65;66;67;68;69;"; //6
		//String ololo = "62-1/-9;620;621;622;623;624;625;626/627;628;629;"; // 62
		//String ololo = "621.1;621.22;621.3;621.4;621.5;621.6;621.7;621.8;621.9;"; //621
		//String ololo = "502/504;51;52;53;54;55;56;57;58;59;"; //5
		//String ololo = "510;511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;"; //51
		//String ololo = "519.83;519.85;";
		//String ololo = "510;511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;"; //51
		//String ololo = "62;621;60;61;63;64;65;66;67;68;69;510;511;512;514;515.1;517;519.1;519.2;519.6;519.7;519.8;631;519;604;";
		//String ololo = "519.8;511;512;514;515.1;517;519.1;519.6;519.7;519.2";
		String ololo = "681.1;681.2;681.5;681.6;681.7;681.8;"; //68
		//String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"};
		String[] main = ololo.split(";");
		Vector[] vold = new Vector[main.length];
		Vector[] vnew = new Vector[main.length];
		int count = 0;

		for(String i : main){
			vold[count] = SQLQuery.getUDCVector(i);
			vnew[count] = SQLQuery.getUDCVector(i);
			count++;
		}

		Vector sum = new Vector();


		for(int i = 0; i < main.length; i++){
			for(int k: vold[i].keySet()){
				sum.put(k, sum.at(k) + vold[i].at(k)*vold[i].getNorm());
			}
		}

		for(int i = 0; i < main.length; i++){
			for(Integer k: vold[i].keySet()){
				if(sum.get(k)*0.9 > vold[i].at(k)*vold[i].getNorm())
					vnew[i].remove(k);
			}
		}
		return "";
	}

	private static String findCloseUDCDistance(Vector vect){
		HashSet<Integer> ud = new HashSet<>(vect.keySet());
		String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8", "9"};
		String min = "0";
		double value = SQLQuery.getUDCVector("0").distanse(vect);
		for(String i : main){
			Vector vect1 = SQLQuery.getUDCVector(i);
			for(int num : SQLQuery.getUDCVector(i).keySet())
				vect1.put(num, vect1.get(num)/SQLQuery.getUDCCount(i));
			if(value > vect.distanse(vect1)){
				value = vect.distanse(vect1);
				min = i;
			}
		}
		return min;
	}

	public static String findCloseUDC(Vector vect){
		return findCloseUDCTerm(vect);
	}


	@Deprecated
	public static String findCloseUDCOLD(Vector vect){

		HashSet<Integer> ud = new HashSet<>(vect.keySet());
		String[] main = new String[]{"0", "1", "2", "3", "5", "6", "7", "8"};
		String min_a = "1";
		String min_d = "1";
		String max_r = "1";
		double value_a = SQLQuery.getUDCVector("1").angle(vect);
		double value_d = SQLQuery.getUDCVector("1").distanse(vect);
		ud.retainAll(SQLQuery.getUDCVector("1").keySet());
		int value_r = ud.size();
		for(String i : main){
			if(value_a > SQLQuery.getUDCVector(i).angle(vect)){
				value_a = SQLQuery.getUDCVector(i).angle(vect);
				min_a = i;
			}
			HashSet<Integer> ud2 = new HashSet<>(vect.keySet());
			ud2.retainAll(SQLQuery.getUDCVector(i).keySet());
			if(value_r < ud2.size()){
				max_r = i;
				value_r = ud2.size();
			}

			Vector vect1 = SQLQuery.getUDCVector(i);
			for(int num : SQLQuery.getUDCVector(i).keySet())
				vect1.put(num, vect1.get(num)/SQLQuery.getUDCCount(i));
			if(value_d > vect.distanse(vect1)){
				value_d = vect.distanse(vect1);
				min_d = i;
			}
		}

		System.out.println("Angle: " + min_a + "   		with: " + value_a);
		System.out.println("Distance: " + min_d + "   	with: " + value_d);
		System.out.println("Retain: " + max_r + "   	with: " + value_r);
		return "";
	}

	private static double computeProbability(LinkedHashSet<Integer> set){
		double res = 0;
		int last = 0;
		for(Integer i : set){
			res += i;
		}
		return res/set.size();
	}


	public static void addURLs(){
		HashSet<String> set = new HashSet<>();
		File dir = new File("A:\\GoogleResults");
		File [] files = dir.listFiles();
		for(File i : files){
			if(i.toString().contains("_res.txt")){
				String url = "";
				try{
					Scanner scan = new Scanner(new FileInputStream(i));
					while(scan.hasNext()){
						url += scan.nextLine() + ";";
					}
					scan.close();

					scan = new Scanner(new FileInputStream("A:\\GoogleResultsWithUDC\\" +
							i.toString().substring(i.toString().lastIndexOf("\\") + 1)));
					while(scan.hasNext()){
						url += scan.nextLine() + ";";
					}
					scan.close();
					for(String str : url.split(";")){
						set.add(str);
					}
					SQLQuery.setUDCURL(i.toString().substring(17, i.toString().length() - 8), url);
				}
				catch(FileNotFoundException e){
						System.out.println("Всё совсем плохо :(");
					}
			}
		}
		SQLQuery.saveURL(set);
	}



	private static class Code{
		String udc;
		double likelihood;
		protected Code(String str, double prob){
			udc = str;
			likelihood = prob;
		}
	}


}


