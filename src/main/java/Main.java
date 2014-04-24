import ru.lisaprog.classifiers.*;

import java.lang.* ;


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
		//MulticlassSVM.test();
		//LDA lda = new LDA(1, 0.01, 50);
		TopicClassifier.test();
//		SaveArticlesYandex.saveArticles();
	}
}


