package ru.lisaprog.articles.yandex;

import ru.lisaprog.articles.ArticleAbstract;
import ru.lisaprog.articles.ArticleInterface;
import ru.lisaprog.objects.Vector;

/**
 * Created by Юлиан on 12.04.14.
 */
public class ArticleYandex extends ArticleAbstract implements ArticleInterface {

	public String rank;
	public double perplexity;

	public ArticleYandex(String udc, String rank, Vector vector){
		this.udc = udc;
		this.rank = rank;
		this.vector = vector;
		perplexity = 0;
	}

	public ArticleYandex(String udc, String rank, Vector vector, double perplexity){
		this.udc = udc;
		this.rank = rank;
		this.vector = vector;
		this.perplexity = perplexity;
	}

}
