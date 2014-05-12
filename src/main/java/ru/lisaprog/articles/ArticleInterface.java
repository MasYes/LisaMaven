/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 12.07.13
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
package ru.lisaprog.articles;

import ru.lisaprog.TemplateStyle;
import ru.lisaprog.objects.Vector;

public interface ArticleInterface {
	boolean equals(ArticleAbstract article); // mb if title equals - true?
	String getSense(); //from that we will try to find keywords etc.
	//double closeness(ArticleAbstract article); //distance between two article
	TemplateStyle getTemplate();
	String getTitle();
	Vector getVector();
}
