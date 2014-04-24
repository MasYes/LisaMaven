package ru.lisaprog.lemmer;

/**
 * Created with IntelliJ IDEA.
 * User: Юлиан
 * Date: 17.08.13
 * Time: 17:27
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.util.Scanner;

public class Lemmer {

	public static String[] lemmer (String str){ // Возможно, не лучшее решение, но лучшего я еще не придумал
		File file = new File("A:\\example.txt");
		try(FileWriter wr = new FileWriter(file)){
			str = str.toLowerCase();
			wr.write(str);
			String command = "A:\\mystem.exe -w -e utf-8 -l -n A:\\example.txt A:\\example_out.txt";
			wr.close();
			java.lang.Runtime.getRuntime().exec(command).waitFor();
			Scanner in = new Scanner(new File("A:\\example_out.txt"));
			String res = "";
			String curr;
			while(in.hasNext()){
				curr = in.nextLine();
				if(!curr.contains("??")){
					if(curr.contains("|"))
						curr = curr.substring(0, curr.indexOf("|"));
					res += curr + ";";
				}
				else
					res += curr.substring(0, curr.length() - 2) + ";";
			}
			return res.split(";");
		} catch (Exception e){
			e.printStackTrace();
			return new String[0];
		}
	}
}
