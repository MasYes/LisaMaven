
/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 12.07.13
 * Time: 19:50
 * To change this template use File | Settings | File Templates.
 */
package ru.lisaprog.articles.cps;


import ru.lisaprog.Language;
import ru.lisaprog.TemplateStyle;
import ru.lisaprog.articles.ArticleAbstract;

public class ArticleCPS extends ArticleAbstract {

	private int section;
	private String university;
	private String references;

	private void setReferences(String str){
		references = str;
	}

	public String getReferences(){
		return references;
	}

	public ArticleCPS(String str){
		section = Integer.parseInt(str.substring(str.indexOf("<section>") + 9, str.indexOf("</section>")));
		setAuthor(str.substring(str.indexOf("<author>") + 8, str.indexOf("</author>")));
		university = str.substring(str.indexOf("<university>") + 12, str.indexOf("</university>"));
		setTitle(str.substring(str.indexOf("<title>") + 7, str.indexOf("</title>")));
		setBody(str.substring(str.indexOf("<body>") + 6, str.indexOf("</body>")));
		setReferences(str.substring(str.indexOf("<references>") + 12, str.indexOf("</references>")));
		setTemplate(TemplateStyle.CPS);
		setVector();
		setPublication("Процессы управления и устойчивость");
		setUdc("");
		setLink("http://www.apmath.spbu.ru/ru/research/conference/pm/archive/");
		setMark(-1);
		setLanguage(Language.RU);
	}


	public String getInfo(){
		return "Университет:" + this.university + "; секция: " + this.section;
	}



	@Deprecated
	public ArticleCPS(String[] str){ //С этим парсером не работает; лучше пользоваться Питоновским для таких случаев.
		int i = 0;
		while(str[i].equals(""))
			i++;
		setAuthor(str[i++]);
		while(!str[i].equals("")){
			university+=str[i++] + " ";
		}
		while(str[i].equals(""))
			i++;
		String title = "";
		while(!str[i].equals("")){
			title+=str[i++] + " ";
		}
		String lit = "Литература";
		String body = "";
		String references = "";
		while(!str[i].contains(lit) && i < str.length){
			body += str[i++] + " ";
		}
		while(i < str.length){
			references+=str[i++] + " ";
		}
		setBody(body);
		setTitle(title);
		setReferences(references);
	}

}
