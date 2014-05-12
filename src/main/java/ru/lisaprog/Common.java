package ru.lisaprog;

import ru.lisaprog.articles.cps.ArticleCPS;
import ru.lisaprog.articles.cyberleninka.ArticleCyberleninka;
import ru.lisaprog.articles.ArticleAbstract;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.objects.Term;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.sql.SQLQuery;

import java.io.*;
import java.util.Date;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 16.07.13
 * Time: 1:44
 * To change this template use File | Settings | File Templates.
 */
public class Common {
	private Common(){}
	public final static int COUNT_THREADS = 1;

	public static void createLog(Exception e){
		System.out.println(new Date() + "\nLOG\n");
		String log = "";
		for(StackTraceElement elem : e.getStackTrace()){
			log += elem.toString() + "\n";
		}
		createLog(e.toString() + log);
	}

	protected synchronized static void createLog(String str){
		try(FileWriter logs = new FileWriter("logs.txt", true);){
			logs.write("[" + new Date() + "]\n" + str + "\n\n");
		} catch (IOException e){
			System.out.println("Всё очень плохо...");
		}
	}

	public static void addArticles(){
		File dir = new File("A:\\articles");
		File [] folders = dir.listFiles();
		for(File folder : folders){
			TemplateStyle tmpl = TemplateStyle.valueOf(
					folder.toString().substring(
					folder.toString().lastIndexOf("\\") + 1));
			File [] files = folder.listFiles();
			for(File i : files){
				if (i.toString().endsWith(".txt")){
					try{
						Scanner file = new Scanner(i);
						String str = "";
						while(file.hasNext()){
							str += file.nextLine() + " ";
						}
						System.out.println(i);
						str = str.replaceAll("Ё", "Е");
						str = str.replaceAll("ё", "е");
						ArticleAbstract art;
						switch(tmpl){ // Возможно, я сделал что-то не так.
							//Я хочу, чтобы при отсутствующем элементе
							//он сразу выдавал ошибку, а ему норм ><
							//либо он дефолта требует
							case CPS:
								SQLQuery.saveArticle(new ArticleCPS(str));
								break;
							case CYBERLENINKA:
								SQLQuery.saveArticle(new ArticleCyberleninka(
										Integer.parseInt(
												i.toString().substring(i.toString().lastIndexOf("\\") + 1,
												i.toString().indexOf(".txt"))
												), str
								));
								break;
						}
						file.close();
					} catch (IOException e){
						Common.createLog(e);
					}
				}
			}
		}
	}

	public static void addURLs(){
		for(int i = 10900; i < SQLQuery.getURLCount(); i++){
			try{
				String str = SQLQuery.getURLText(i);
				System.out.println(i);
				if(str!=null){
					str = str.replaceAll("Ё", "Е");
					str = str.replaceAll("ё", "е");
					SQLQuery.setURLVector(i, Vector.toVector(Lemmer.lemmer(str)));
				}
			} catch (Exception e){
				Common.createLog(e);
			}
		}
	}


	@Deprecated //нужда в этом отпала
	protected static void computeMeasures(int id){
		int last = SQLQuery.getCountOfWords();
		for(int i = id; i <= last; i++){
			System.out.println(i);
			Term term = SQLQuery.getWordData(i);
			term.computeMeasure();
			SQLQuery.updateWord(term);
		}
	}

	@Deprecated
	public static void computeMeasures(){
		computeMeasures(1);
	}
}
