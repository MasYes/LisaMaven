package ru.lisaprog.articles.yandex;

import ru.lisaprog.articles.ArticleAbstract;
import ru.lisaprog.objects.Vector;

/**
 * Created by Юлиан on 12.04.14.
 */
public class ArticleYandex extends ArticleAbstract {

	public String udc;
	public String rank;
	public Vector vector;

	public ArticleYandex(String udc, String rank, Vector vector){
		this.udc = udc;
		this.rank = rank;
		this.vector = vector;
	}



}
