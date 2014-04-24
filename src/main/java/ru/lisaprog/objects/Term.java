package ru.lisaprog.objects;

import ru.lisaprog.sql.SQLQuery;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 24.07.13
 * Time: 23:41
 * To change this template use File | Settings | File Templates.
 */
public class Term {

	private static BigInteger[][] snsk;
	private static int height = 2000; //1150
	private static int width = 500; //200
	private static boolean init = false;


	private String word;
	private int units;
	private int frequency;
	private double measure;

	protected void setWord(String str){
		word = str;
	}

	public String getWord(){
		return word;
	}

	public int getUnits(){
		return units;
	}

	public int getFrequency(){
		return frequency;
	}

	public double getMeasure(){
		return measure;
	}

	protected void incrementFrequency(){
		frequency++;
	}

	protected void addToFrequency(int i){
		frequency += i;
	}

	protected void incrementUnits(){
		units++;
	}

	public Term (String str, int freq){
		word = str;
		units = 1;
		frequency = freq;
		measure = 1;
	}

	protected Term(String str){
		word = str;
		units = 0;
		frequency = 0;
		measure = 1;
	}

	protected Term(){
		word = "";
		units = 0;
		frequency = 0;
		measure = 1;
	}

	protected Term(int freq){
		word = "";
		units = 1;
		frequency = freq;
		measure = 1;
	}

	public Term (String str, int fr, int un, double ms){
		word = str;
		units =un;
		frequency = fr;
		measure = ms;
	}

	private static void initsnsk(){
		init = true;
		snsk = new BigInteger[height][];
		for(int i = 0; i < height; i++){
			snsk[i] = new BigInteger[width];
		}
		snsk[0][0] = BigInteger.ONE;
		for(int i = 1; i < width; i++){
			snsk[0][i] = BigInteger.ZERO;
		}
		for(int i = 1; i < height; i++){
			for(int j = 0; j < width; j++){
				snsk[i][j] = get(i-1, j-1).add( get(i-1, j).multiply(BigInteger.valueOf(j)) ); // OMG, ай хейт джава, все дела. Перегрузка операторов ><
			}
		}
	}

	private static BigInteger get(int i, int j){
		if(i < 0 || j < 0)
			return BigInteger.ZERO;
		return snsk[i][j];
	}

	public void computeMeasure(){
		if(!init)
			initsnsk();
		this.measure = 1;
		if(frequency < height && units < width && word.length() > 2){
			int count = SQLQuery.getCountOfArticles();
			BigInteger res = BigInteger.ZERO;
				for(int n = 1; n <= this.units; n++){
					res = res.add(multiplyFromTo(count - n, count).multiply(get(this.frequency, n)));
				}
			this.measure = division(res, BigInteger.valueOf(count).pow(this.frequency));
		}
		else this.measure = 1;
	}

	private static double division(BigInteger dividend, BigInteger divider){
		if(dividend.compareTo(divider) >= 0)
			return 1;
		double res = 0;
		int pow = 1;
		while(pow < 25){
			dividend = dividend.multiply(BigInteger.TEN);
			int count = 0;
			while(dividend.compareTo(divider) == 1){
				dividend = dividend.subtract(divider);
				count++;
			}
			res+=count*java.lang.Math.pow(0.1, pow);
			pow++;
		}
		return res;
	}

	private static BigInteger multiplyFromTo(int from, int to){ //перемножает все числа от from+1 до (to); Если from == to, то единицу
		BigInteger res = BigInteger.ONE;
		while(from < to){
			from++;
			res = res.multiply(BigInteger.valueOf(from));
		}
		return res;
	}

	public static BigInteger computeBigSNSK(int n, int k){
		//В будущем потребуется, когда оперативки не будет хватать под snsk
		n--;
		BigInteger[] mass = new BigInteger[k];
		for(int i = 0; i < k; i++)
			mass[i] = BigInteger.ZERO;
		mass[0] = BigInteger.ONE;
		for(int i = 0; i < n; i++){
			System.out.println(i);
			for(int j = k - 1; j >0; j--){
				mass[j] = mass[j].multiply(BigInteger.valueOf(j+1)).add(mass[j-1]);
			}
		}
		return mass[k - 1];
	}

}



