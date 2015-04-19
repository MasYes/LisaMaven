package ru.lisaprog.sql;

/**
 * Created with IntelliJ IDEA.
 * User: masyes
 * Date: 14.07.13
 * Time: 3:07
 * To change this template use File | Settings | File Templates.
 * в некоторых функциях можно ретернить не из самого результсет, а сначала сохранить результат в стринги,
 * а потом вызвать rs.close(), что несколько снижает затраты оперативной (вроде).
 * Засунуть все коннекты в "трай витх ресорс"
 *
 * CREATE TABLE lisa.articles(id int primary key auto_increment, author text, title text, vector long binary, udc text, Template text, link text,
 mark int, language text, info text, publication text);
 *
 */

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ru.lisaprog.articles.yandex.ArticleYandex;
import ru.lisaprog.Common;
import ru.lisaprog.objects.Dictionary;
import ru.lisaprog.articles.ArticleAbstract;
import ru.lisaprog.objects.Term;
import ru.lisaprog.objects.UDC;
import ru.lisaprog.objects.Vector;

import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class SQLQuery {
	private static Connection conn;
	private static String user = "masyes";
	private static String password = "jYbtGf27";
	private static String url = "localhost";
	private static String port = "3306";
	private static String DB = "lisa";
	private static boolean connected = false;

	private SQLQuery(){}
//-----------------------------------------------------------------------
//------------------------------Общие-----------------------------------0
//-----------------------------------------------------------------------


	private static void connect() throws SQLException {

		conn = DriverManager.getConnection(
				"jdbc:mysql://" + url + ":" + port + "/" + DB,
				user, password);
		if (conn == null) {
			System.out.println("Нет соединения с БД!");
		}
		else connected = true;
	}

	public static void disconnect() throws SQLException {
		//На самом деле я не хотел делать эту функцию, но
		//если иногда не закрывать соединение, то объём используемой
		//оперативки приводит к аутофмемори
		if(connected)
			conn.close();
		connected = false;
	}


	private static byte[] serialize(Object obj) { // ПЕРЕНЕСТИ СЕРИАЛИЗАЦИЮ СЮДА!
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			return os.toByteArray();
		} catch(IOException e){
			Common.createLog(e);
			return null;
		}
	}

	public static String serialize(Vector vect){
		String res = vect.getNorm() + "!";
		for(Integer i : vect.keySet()){
			res += i + ":" + vect.get(i) + ";";
		}
		return res;
	}

	public static Vector deserialize(String str){
		Vector vect = new Vector();
		vect.setNorm(Double.parseDouble(str.substring(0, str.indexOf("!"))));
		str = str.substring(str.indexOf("!") + 1);
		if(str.split(";").length > 1)
			for(String i : str.split(";")){
				vect.put(Integer.parseInt(i.substring(0, i.indexOf(":"))),
						Double.parseDouble(i.substring(i.indexOf(":") + 1)));
			}
		return vect;
	}

	private static Object deserialize(byte[] array) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(array);
			ObjectInputStream objInputStream = new ObjectInputStream(is);
			return objInputStream.readObject(); //Если класс каст эксепшн, то, скорее всего, класс был изменен, а посему восстановить его нет возможности.
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int createDump(String id, Object obj){
		if(id.length() > 16)
			return -1;
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO dumps (id, ser) VALUES (?, ?)");
			ps.setString(1, id);
			ps.setString(2, arrayToString(serialize(obj)));
			ps.executeUpdate();
			ps.close();
		} catch(SQLException e){Common.createLog(e);}
		return 0;
	}


	public static Object readDump (String id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ser FROM dumps WHERE id=\'" + id + "\'");
			rs.next();
			return deserialize(stringToArray(rs.getString("ser")));
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}


	private static String arrayToString(byte[] array){
		String res = "\'";
		for(int i = 0; i < array.length; i++){
			if(array[i] <= 15 && array[i]>=0){
				res += "0" + Integer.toHexString(array[i] & 0xFF);
			} else {
				res +=Integer.toHexString(array[i] & 0xFF);
			}
		}
		res += '\'';
		return res;
	}



	private static byte[] stringToArray(String str){
		str = str.substring(1, str.length() - 1);
		byte[] res = new byte[str.length()/2];
		for(int i = 0; i < str.length(); i = i + 2){
			res[i/2] = (byte) (Integer.parseInt("" + str.charAt(i) + str.charAt(i + 1), 16) & 0xFF);
		}
		return res;
	}



//-----------------------------------------------------------------------
//------------------------------Статьи----------------------------------1
//-----------------------------------------------------------------------

	public static void saveArticle(ArticleAbstract article){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO lisa.articles " +
					"(author, title, vector, udc, Template," +
					"link, mark, language, info,  publication) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, article.getAuthor());
			ps.setString(2, article.getTitle());
			ps.setString(3, serialize(article.getVector()));
			ps.setString(4, article.getUdc());
			ps.setString(5, article.getTemplate().toString());
			ps.setString(6, article.getLink());
			ps.setInt(7, article.getMark());
			ps.setString(8, article.getLanguage().toString());
			ps.setString(9, article.getInfo());
			ps.setString(10, article.getPublication());
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void saveArticlesYandex(ArrayList<ArticleYandex> articles){
		try{
			if(!connected)
				connect();
			String query = "INSERT INTO lisa.articleYandex (udc, rank, vector, perplexity) VALUES ";
			for(ArticleYandex article : articles){
				query += "('" + article.udc + "'," + article.rank + ",'" + serialize(article.vector) + "', " + article.perplexity + "),";
			}

//			System.out.println("Saving articles");

			PreparedStatement ps = conn.prepareStatement(query.substring(0, query.length() - 1) + ";");

			ps.executeUpdate();
			ps.close();
//			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static ArrayList<ArticleYandex> getArticlesYandex(String where){
		try{
			if(connected)
				disconnect();
			if(!connected)
				connect();
			ArrayList<ArticleYandex> result = new ArrayList<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM lisa.articleYandex WHERE " + where + ";");
			while(rs.next()){
				result.add(new ArticleYandex(rs.getString("udc"), rs.getString("rank"), deserialize(rs.getString("vector")), rs.getDouble("perplexity")));
			}
			return result;
		} catch (SQLException e){
			Common.createLog(e);
			return new ArrayList<>();
		}
	}

	public static ArrayList<String> getArticlesYandexUDCs(){
		try{
			if(!connected)
				connect();
			ArrayList<String> result = new ArrayList<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT(udc) FROM lisa.articleYandex;");
			while(rs.next()){
				result.add(rs.getString("udc"));
			}
			return result;
		} catch (SQLException e){
			Common.createLog(e);
			return new ArrayList<>();
		}
	}

	public static void savePerplexity(String udc, String rank, double perplexity){

		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.articleyandex SET perplexity = " + perplexity + " " +
					"WHERE udc = '" + udc + "' AND rank = '" + rank + "';");

			ps.executeUpdate();
			ps.close();
		} catch (SQLException e){
			Common.createLog(e);
		}



	}


	public static String getArticleUDC(int i){
		try{
			if(connected)
				disconnect();
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT udc FROM lisa.articles WHERE id=" + i);
			rs.next();
			return rs.getString("udc");
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}

	public static String getArticleInfo(int i){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT info FROM lisa.articles WHERE id=" + i);
			rs.next();
			return rs.getString("info");
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}


	public static Vector getArticleVector(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT vector FROM lisa.articles WHERE id=" + id);
			rs.next();
			return deserialize(rs.getString("vector"));
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}

	public static String getArticleTitle(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT title FROM lisa.articles WHERE id=" + id);
			rs.next();
			return rs.getString("title");
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}

	public static int getCountOfArticles(){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM lisa.articles");
			rs.next();
			return rs.getInt("MAX(id)");
		} catch (SQLException e){
			Common.createLog(e);
			return -1;
		}
	}


//-----------------------------------------------------------------------
//------------------------------Термы-----------------------------------2
//-----------------------------------------------------------------------
	public static void saveIntoDict(String word, int units, int freq, double meas){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO dict (word, units, freq, meas) VALUES (?, ?, ?, ?)");
			ps.setString(1, word);
			ps.setInt(2, units);
			ps.setInt(3, freq);
			ps.setDouble(4, meas);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void saveIntoDict(ArrayList<Term> terms){
		try{
			if(!connected)
				connect();
			String query = "INSERT INTO dict (id, word, units, freq, meas) VALUES ";
			for(Term term : terms){
				query+="(" + Dictionary.indexes.getInt(term.getWord()) + ",'" + term.getWord() + "'," + term.getUnits() + "," + term.getFrequency() + "," + term.getMeasure() + "),";
			}
//			System.out.println(query);
			query = query.substring(0, query.length() - 1) + ";";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}


	public static void updateWord(Term term){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.dict SET freq=?, units=?, meas=? WHERE word=?;");
			ps.setInt(1, term.getFrequency());
			ps.setInt(2, term.getUnits());
			ps.setDouble(3, term.getMeasure());
			ps.setString(4, term.getWord());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static int getCountOfWords(){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM lisa.dict");
			rs.next();
			return rs.getInt("MAX(id)");
		} catch (SQLException e){
			Common.createLog(e);
			return -1;
		}
	}

	public static Term getWordData(int i){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM lisa.dict WHERE id=" + i);
			rs.next();
			return  new Term(rs.getString("word"), rs.getInt("freq"), rs.getInt("units"), rs.getDouble("meas"));
		} catch (SQLException e){
			Common.createLog(e);
			return null;
		}
	}

	public static int getIdWord(String word){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM lisa.dict WHERE word=\"" + word + "\"");

			rs.next();
			return  rs.getInt("id");
		} catch (SQLException e){
			//Common.createLog(e); // Если будет забивать логи - убить :D upd убил :D
			return -1;
		}
	}

	public static HashSet<String> getWordsFromDict(String dict){
		try{
			if(!connected)
				connect();
			HashSet<String> set = new HashSet<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT word FROM lisa." + dict + ";");

			while(rs.next()){
				set.add(rs.getString("word"));
			}

			return set;

		} catch (SQLException e){
			//Common.createLog(e); // Если будет забивать логи - убить :D upd убил :D
			return new HashSet<>();
		}
	}

	public static Object2IntOpenHashMap<String> getWords(){
		try{
			if(!connected)
				connect();
			Object2IntOpenHashMap<String> result = new Object2IntOpenHashMap<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT word, id FROM lisa.dict");
			while(rs.next()){
				result.put(rs.getString("word"), rs.getInt("id"));
			}

			return result;

		} catch (SQLException e){
			Common.createLog(e); // Если будет забивать логи - убить :D upd убил :D
			return new Object2IntOpenHashMap<>();
		}
	}


//-----------------------------------------------------------------------
//------------------------------УДК-------------------------------------3
//-----------------------------------------------------------------------
	public static UDC getUDC(String code){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM udc WHERE id=\'" + code + "\'");
			rs.next();
			return new UDC(rs.getString("id"), rs.getString("description"),
					rs.getString("parent"),rs.getString("children"));
		} catch (SQLException e){
			if(!code.equals(""))
				Common.createLog(e);
			return new UDC();
		}
	}

	public static Vector getUDCVector(String code){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT vector FROM lisa.udc WHERE id=\'" + code + "\';");
			rs.next();
			return deserialize(rs.getString("vector"));
		} catch (Exception e){
			return new Vector();
		}
	}

	public static Vector getUDCTerms(String code){
		try{
			if(connected)
				disconnect();
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT terms FROM lisa.udc WHERE id=\'" + code + "\';");
			rs.next();
			return deserialize(rs.getString("terms"));
		} catch (Exception e){
			return new Vector();
		}
	}

	public static int getUDCCount(String code){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count FROM lisa.udc WHERE id=\'" + code + "\';");
			rs.next();
			return rs.getInt("count");
		} catch (Exception e){
			return 0;
		}
	}

	public static void setUDCVector(String id, Vector vector){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET vector=? WHERE id=?;");
			ps.setString(1, serialize(vector));
			ps.setString(2, id);
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void setUDCTerms(String id, Vector vector){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET terms=? WHERE id=?;");
			ps.setString(1, serialize(vector));
			ps.setString(2, id);
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void setUDCCount(String id, Integer count){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET count=? WHERE id=?;");
			ps.setInt(1, count);
			ps.setString(2, id);
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void setUDCURL(String id, String url){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET url=? WHERE id=?;");
			ps.setString(1, url);
			ps.setString(2, id);
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static ArrayList<String> getDistinctParents(){
		try{
			if(!connected)
				connect();
			ArrayList<String> results = new ArrayList<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT(parent) FROM lisa.udc");
			while(rs.next()){
				results.add(rs.getString("parent"));
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new ArrayList<>();
		}
	}


	public static HashMap<String, String> getUDCURLByParent(String parent){
		try{
			if(!connected)
				connect();
			HashMap<String, String> results = new HashMap<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id, url FROM lisa.udc WHERE parent = '" + parent + "';");
			while(rs.next()){
				results.put(rs.getString("id"), rs.getString("url"));
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new HashMap<>();
		}
	}

	public static ArrayList<String> getListOfUDCs(){
		try{
			if(!connected)
				connect();
			ArrayList<String> results = new ArrayList<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT(id) FROM lisa.udc;");
			while(rs.next()){
				results.add(rs.getString("id"));
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new ArrayList<>();
		}
	}

	public static HashSet<String> getUDCByParent(String parent){
		try{
			if(!connected)
				connect();
			HashSet<String> results = new HashSet<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM lisa.udc WHERE parent = '" + parent + "';");
			while(rs.next()){
				results.add(rs.getString("id"));
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new HashSet<String>();
		}
	}

	public static IntArrayList getArticlesForTests(String parent){
		try{
			if(!connected)
				connect();
			IntArrayList results = new IntArrayList();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM lisa.articles WHERE udc like \" " +parent +  "%\";");
			while(rs.next()){
				results.add(Integer.parseInt(rs.getString("id")));
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new IntArrayList();
		}
	}

	public static IntArrayList getArticlesForTests(String[] udcs){
		try{
			if(!connected)
				connect();
			IntArrayList results = new IntArrayList();
			for(String udc : udcs){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT id FROM lisa.articles WHERE udc like \" " +udc +  "%\";");// length(udc) < 20
				int size = 0;
				while(rs.next()){
					results.add(Integer.parseInt(rs.getString("id")));
				}
			}
			return results;
		} catch (Exception e){
			Common.createLog(e);
			return new IntArrayList();
		}
	}


//-----------------------------------------------------------------------
//------------------------------Группы----------------------------------4
//-----------------------------------------------------------------------
	public static void saveGroup(String group, Vector vector){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO lisa.groups " +
					"(articles, vector) VALUES (?, ?)");
			ps.setString(1, group);
			ps.setString(2, serialize(vector));
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static String getGroupArticles(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT articles FROM lisa.groups WHERE id=\'" + id + "\';");
			rs.next();
			return rs.getString("articles");
		} catch (Exception e){
			return "";
		}
	}

	public static Vector getGroupVector(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT vector FROM lisa.groups WHERE id=\'" + id + "\';");
			rs.next();
			return deserialize(rs.getString("vector"));
		} catch (Exception e){
			return new Vector();
		}
	}


//-----------------------------------------------------------------------
//------------------------------URLы------------------------------------5
//-----------------------------------------------------------------------

	public static void saveURL(HashSet<String> set){
		try{
			if(!connected)
				connect();
			for(String i : set){
				PreparedStatement ps = conn.prepareStatement("INSERT INTO lisa.url (url) VALUES (?)");
				ps.setString(1, i);
				ps.executeUpdate();
				ps.close();
			}
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static int getURLCount(){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lisa.url;");
			rs.next();
			return rs.getInt("COUNT(*)");
		} catch (SQLException e){
			Common.createLog(e);
			return -1;
		}
	}

	public static String getURL(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT url FROM lisa.url WHERE id = " + id + ";");
			rs.next();
			return rs.getString("url");
		} catch (SQLException e){
			Common.createLog(e);
			return "";
		}
	}

	public static void setURLVector(int id, Vector vector){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.url SET vector=? WHERE id=?;");
			ps.setString(1, serialize(vector));
			ps.setInt(2, id);
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static void setURLText(String url, String text){
		try{
			if(!connected)
				connect();
			text = text.replaceAll("\\p{Punct}", " ").replaceAll("\\p{javaWhitespace}", " ").replaceAll("\n", " ");
			while(text.contains("  ")){
				text = text.replaceAll("  ", " ");
			}
			PreparedStatement ps = conn.prepareStatement("UPDATE lisa.url SET text=\"" + text + "\" WHERE url=?;");
			ps.setString(1, url);
			ps.executeUpdate();
			ps.close();
			disconnect();

		} catch (SQLException e){
			try{ //Из-за википедии приходится проверять каждый символ, иначе SQL не ест.
				if(e.toString().contains("com.mysql.jdbc.PacketTooBigException")){ //вызывается 7856
					throw new Exception();
				}
				for(int i = 0; i < text.length(); i++){
					if(!(((text.charAt(i) >= 'а')&&(text.charAt(i) <= 'я'))||
							((text.charAt(i) >= 'А')&&(text.charAt(i) <= 'Я'))||
							((text.charAt(i) >= 'a')&&(text.charAt(i) <= 'z'))||
							((text.charAt(i) >= 'A')&&(text.charAt(i) <= 'Z'))||
							((text.charAt(i) >= '0')&&(text.charAt(i) <= '9')))){
						text = text.substring(0, i) + " " + text.substring(i + 1);
					}

				}
				while(text.contains("  ")){
					text = text.replaceAll("  ", " ");
				}
				PreparedStatement ps = conn.prepareStatement("UPDATE lisa.url SET text=\"" + text + "\" WHERE url=?;");
				ps.setString(1, url);
				ps.executeUpdate();
				ps.close();
				disconnect();

			}catch(Exception i){
				Common.createLog(i);
			}
		}
	}

	public static Vector getURLVector(String url){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT vector FROM lisa.url WHERE id=\'" + url + "\';");
			rs.next();
			return deserialize(rs.getString("vector"));
		} catch (Exception e){
			return new Vector();
		}
	}

	public static String getURLText(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT text FROM lisa.url WHERE id = " + id + ";");
			rs.next();
			return rs.getString("text");
		} catch (SQLException e){
			Common.createLog(e);
			return "";
		}
	}



//-----------------------------------------------------------------------
//--------------------------------Rocchio-------------------------------6
//-----------------------------------------------------------------------

	@Deprecated
	public static void rocchioSetTermTF(int term, String udc, String parent, long tf){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO rocchio_terms (id, udc, parent, tf) VALUES (?, ?, ?, ?)");
			ps.setInt(1, term);
			ps.setString(2, udc);
			ps.setString(3, parent);
			ps.setLong(4, tf);
			ps.executeUpdate();
			ps.close();
		} catch(SQLException e){
			Common.createLog(e);
		}
	}

	@Deprecated
	public static void rocchioSetTermTF(String values){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO rocchio_terms (id, udc, parent, tf) VALUES " + values + ";");
			ps.executeUpdate();
			ps.close();
		} catch(SQLException e){
			Common.createLog(e);
		}
	}

	@Deprecated
	public static void rocchioSetTermIDF(int term, String parent, double idf){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("UPDATE rocchio_terms SET idf = " + idf + " WHERE term = " + term + " AND parent = " + parent + ";");
			ps.executeUpdate();
			ps.close();
		} catch(SQLException e){
			Common.createLog(e);
		}
	}


	public static void rocchioSaveUDCWeights(String udc, Vector vector){
		try{
			if(!connected)
				connect();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO lisa.rocchio_terms (udc, weights) VALUES(?, ?)");
			ps.setString(1, udc);
			ps.setString(2, serialize(vector));
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	public static Vector rocchioGetUDCWeights(String udc){
		try{
			if(connected)
				disconnect();
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT weights FROM lisa.rocchio_terms WHERE udc=\'" + udc + "\';");
			rs.next();
			return deserialize(rs.getString("weights"));
		} catch (Exception e){
			return new Vector();
		}
	}

//-----------------------------------------------------------------------
//----------------------------------MI----------------------------------7
//-----------------------------------------------------------------------


	public static void saveMutualInformation(HashMap<String, Int2DoubleOpenHashMap> mi){
		try{
			if(!connected)
				connect();
			String query = "INSERT INTO lisa.mutual_information (udc, term_id, mi) VALUES ";
			int count = 0;
			for(String udc : mi.keySet())
				for(int termId : mi.get(udc).keySet()){
					if(mi.get(udc).get(termId) >= 0.0001){
						count++;
						query += "('" + udc + "', " + termId + ", " + mi.get(udc).get(termId)  + "),";
					}
					if(count >= 5000){
						query = query.substring(0, query.length() - 1);
						PreparedStatement ps = conn.prepareStatement(query + ";");
						ps.executeUpdate();
						ps.close();
						query = "INSERT INTO lisa.mutual_information (udc, term_id, mi) VALUES ";
						count = 0;
					}
				}

			if(count > 0){
				query = query.substring(0, query.length() - 1);
				PreparedStatement ps = conn.prepareStatement(query + ";");
				ps.executeUpdate();
				ps.close();
			}

		}catch(Exception e){
			Common.createLog(e);
		}
	}

	public static double getMutualInformation(String udc, int term){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT mi FROM lisa.mutual_information WHERE udc=\'" + udc + "\' AND term_id = " + term + ";");
			rs.next();
			return rs.getDouble("mi");
		}catch(Exception e){
			return 0;
		}
	}

	public static IntOpenHashSet getMutualInformationWithLimitMI(String udc, double limit){
		try{
			if(!connected)
				connect();
			IntOpenHashSet result = new IntOpenHashSet();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT term_id FROM lisa.mutual_information WHERE udc=\'" + udc + "\' AND MI >= " + limit + ";");
			while(rs.next()){
				result.add(rs.getInt("term_id"));
			}
			return result;
		}catch(Exception e){
			return new IntOpenHashSet();
		}
	}

//-----------------------------------------------------------------------
//---------------------------------ROMIP--------------------------------7
//-----------------------------------------------------------------------

	public static void saveArticlesRomip(HashMap<String, Vector> map){
		try{
			if(!connected)
				connect();
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO romip_legal_2007 (original_id, vector) VALUES ");

			int i = 0;
			for(String key : map.keySet()){
				if(i++ == 0){
					query.append("(\"").append(key).append("\",\"").append(serialize(map.get(key))).append("\")");
				}
				else{
					query.append(",(\"").append(key).append("\",\"").append(serialize(map.get(key))).append("\")");
				}
			}
			PreparedStatement ps = conn.prepareStatement(query.toString() + ";");
			ps.executeUpdate();
			ps.close();
			disconnect();
		} catch (SQLException e){
			Common.createLog(e);
		}
	}


	public static Vector getArticleRomip(String id){
		try{
			if(!connected)
				connect();

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT vector FROM lisa.romip_legal_2007 WHERE original_id = \"" + id + "\";");
			rs.next();
			return deserialize(rs.getString("vector"));
		} catch (SQLException e){
//			Common.createLog(e);//реально некоторые документы отсутствуют 0о
			return null;
		}
	}




//-----------------------------------------------------------------------
//------------------------------Устаревшие------------------------------9
//-----------------------------------------------------------------------




	@Deprecated
	public static void changeURLs(){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM lisa.udc WHERE NOT url is NULL");
			for(int i = 0; i < 1; i++)
				rs.next();
			while(rs.next()){
				String id = rs.getString("id");
				String[] urls = rs.getString("url").split(";");
				String url = "";
				for(String i : urls){
					try{
						ResultSet rs2 = stmt2.executeQuery("SELECT id FROM lisa.url WHERE url = '" + i + "';");
						rs2.next();
						url += rs2.getInt("id") + ";";
					}catch(Exception e){

					}
				}

				PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET url=\"" + url + "\" WHERE id= '" + id + "';");
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException e){
			Common.createLog(e);
		}
	}

	@Deprecated
	public static boolean setURLsToParents(){
		boolean oneMoreTime = false;
		try{
			if(!connected)
				connect();
			HashMap<String, IntOpenHashSet> map = new HashMap<>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id, url FROM lisa.udc");
			while(rs.next()){
				String id = rs.getString("id");
				map.put(id, new IntOpenHashSet());
				if(rs.getString("url").length() > 2){
					for(String i : rs.getString("url").split(";")){
						map.get(id).add(Integer.parseInt(i));
					}
				}
			}
			rs.close();

			rs = stmt.executeQuery("SELECT * FROM lisa.udc WHERE length(url) > 2;");
			while(rs.next()){
				String parent = rs.getString("parent");
				if(!parent.equals("NULL"))
					for(String i : rs.getString("url").split(";"))
						if(map.get(parent).add(Integer.parseInt(i)))
							oneMoreTime = true;
			}
			rs.close();

			for(String key : map.keySet()){
				String url = "";
				for(int i : map.get(key)){
					url += i + ";";
				}
				PreparedStatement ps = conn.prepareStatement("UPDATE lisa.udc SET url=\"" + url + "\" WHERE id= '" + key + "';");
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException e){
			Common.createLog(e);
		}
		return oneMoreTime;
	}








	@Deprecated
	public static String getEnd(String word){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM words WHERE word=\'" + word + "\'");
			rs.next();
			return rs.getString("ends");
		} catch (SQLException e){
			//Common.createLog(e); и это забивает тоже ><
			return null;
		}
	}



	@Deprecated
	public static double averageNumberOfChildren(){
		try{
			int i = 0;
			int number = 0;
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM udc");
			while(rs.next()){
				if(rs.getString("children").length() > 0){
					i += rs.getString("children").split(";").length;
					number++;
				}
			}
			return 1.0*i/number;
		} catch (SQLException e){
			//Common.createLog(e); и это забивает тоже ><
			return -1;
		}
	}


	@Deprecated
	public static String getEnd(int id){
		try{
			if(!connected)
				connect();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM ends WHERE id=" + id + ";");
			rs.next();
			return rs.getString("ends");
		} catch (SQLException e){
			//Common.createLog(e); Не загоняем в соммон крейт лог, поскольку они засоряют весь лог файл ><
			return "";
		}
	}
}