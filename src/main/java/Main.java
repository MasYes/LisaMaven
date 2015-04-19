import ru.lisaprog.classifiers.*;

import java.lang.* ;

import ru.lisaprog.lemmer.Lemmer;
import ru.lisaprog.objects.Vector;
import ru.lisaprog.romip.RomipClassificationTest;
import ru.lisaprog.romip.DocumentClasses;
import ru.lisaprog.sql.SQLQuery;

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

		int nTests = 100;
		double sum = 0;
		for(int i = 0; i < nTests; i++){
			System.out.println(i);
			RomipClassificationTest test = new RomipClassificationTest();
			System.out.println("Test");
			Rocchio rocchio = new Rocchio();
			int n = 0;
			for(String doc : DocumentClasses.getKeySet()){
				if(n++ > 1000)
					break;
				SQLQuery.disconnect();
				if(!test.isForTest(doc))
					for(String cls : DocumentClasses.getClassOfDoc(doc)){
						Vector vector = SQLQuery.getArticleRomip(doc);
						if(vector != null)
							rocchio.addVector(cls, vector);
					}
			}
			System.out.println("start");
			sum += test.test(rocchio);
			System.out.println("sum = " + sum);
		}
		System.out.println(sum/nTests);
	}

}






