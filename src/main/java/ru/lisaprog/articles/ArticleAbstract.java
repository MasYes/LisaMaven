/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 12.07.13
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 * IllegalArgumentException - неверные входящие данные
 */
package ru.lisaprog.articles;

import ru.lisaprog.keywords.Keywords;
import ru.lisaprog.Language;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.TemplateStyle;
import ru.lisaprog.objects.Vector;

import java.io.Serializable;

public abstract class ArticleAbstract implements Serializable, ArticleInterface{
	private int mark;
	private String link;
	private String author;
	private String publication;
	private String title;
	private String body;
	public String udc;
	private TemplateStyle template;
	private Language lang;
	public Vector vector;

	public ArticleAbstract(){
		mark = 0;
		link = "";
		author = "";
		publication = "";
		title = "";
		body = "";
		udc = "";
		template = TemplateStyle.NONE;
		lang = Language.RU;
	}

	String[] keywords(){
		return Keywords.getKeywords(vector);
	}


	protected void setMark(int i){
		this.mark = i;
	}

	protected void setLink(String str){
		this.link = str;
	}

	protected void setAuthor(String str){
		this.author = str;
	}

	protected void setPublication(String str){
		this.publication = str;
	}

	protected void setTitle(String str){
		this.title = str;
	}

	protected void setBody(String str){
		this.body = str;
	}

	protected void setVector(){
		String str = getSense();
		while(str.contains("  ")){
			str = str.replace("  ", " ");
		}
		this.vector = Vector.toVector(Lemmer.lemmer(str));
	}

	protected void setVector(Vector vect){
		this.vector = vect;
	}


	protected void setUdc(String udc){
		this.udc = udc;
	}


	protected void setTemplate(TemplateStyle tmpl){
		this.template = tmpl;
	}

	protected void setLanguage(Language lang){
		this.lang = lang;
	}

	public int getMark(){
		return mark;
	}

	public String getTitle(){
		return title;
	}

	public String getUdc(){
		return udc;
	}

	public Vector getVector(){
		return vector;
	}

	public String getAuthor(){
		return author;
	}

	public String getLink(){
		return link;
	}


	public String getBody() {
		return body;
	}

	public TemplateStyle getTemplate(){
		return template;
	}

	public Language getLanguage(){
		return lang;
	}

	public String getSense(){
		return title + " " + body;
	}

	public String getInfo(){
		return "";
	}

	public String getPublication(){
		return publication;
	}


	public boolean equals(ArticleAbstract article){
		return this.title.equals(article.title);
	}

}
