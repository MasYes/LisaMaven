package ru.lisaprog.articles;

import ru.lisaprog.parser.ExtractText;
import ru.lisaprog.keywords.Keywords;
import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.nearest.Nearest;
import ru.lisaprog.objects.UDC;
import ru.lisaprog.objects.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 29.07.13
 * Time: 18:09
 * To change this template use File | Settings | File Templates.
 *
 * По возможности стараться все методы lisa делать максимум protected,
 * а всё общение с "внешним" миром поместить сюды.
 *
 */
public class Article{
	public Vector vector;
	public String text;
	public String udc = "";

	public String[] keywords(){
		return Keywords.getKeywords(vector);
	}


	public Integer[] findClose(){
		return Nearest.findClose(this.vector);
	}

	public Integer[] nearest(int count){ // находит count ближайших статей.
		return this.vector.nearest(count);
	}

	public Article(){
		text = "";
		vector = new Vector();
	}

	public Article(String file){
		text = ExtractText.parse(file);
		normalize();
		vector = Vector.toVector(Lemmer.lemmer(text));
	}

	public Article(String file, boolean createVector){
		text = ExtractText.parse(file);
		normalize();
		if(!createVector){
			vector = new Vector();
		}
		else{
			vector = Vector.toVector(Lemmer.lemmer(text));
		}
	}

	public String findUDC(){
		this.udc = UDC.findCloseUDC(this.vector);
		return this.udc;
	}


	private void normalize(){//пытается решить проблему с переносами слов

		String[] str = text.split("\n");
		String res = "";
		for(String i : str){
			if(i.length() > 6 && i.length() - i.lastIndexOf("-") < 3){
				res += i.substring(0, i.lastIndexOf("-"));
			}
			else
				res += i + "\n";
		}
		text = text.replaceAll("Ё", "Е");
		text = text.replaceAll("ё", "е");
		text = res.replaceAll("\\p{Punct}", " ");
	}

}
