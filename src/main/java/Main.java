import com.akiban.sql.parser.InsertNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import static ru.lisaprog.Common.*;
import org.eclipse.mylyn.wikitext.core.WikiText;


import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.commons.lang3.StringEscapeUtils;
import ru.lisaprog.TF;
import ru.lisaprog.classifiers.*;

import java.io.*;
import java.lang.* ;
import java.sql.Connection;
import java.util.*;

import ru.lisaprog.objects.Vector;
import ru.lisaprog.romip.RomipClassificationTest;
import ru.lisaprog.romip.DocumentClasses;
import ru.lisaprog.sql.SQLQuery;

import org.tartarus.snowball.ext.EnglishStemmer;
import ru.lisaprog.wikipedia.WikipediaParser;


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

    private static Object2IntOpenHashMap<String> ids = new Object2IntOpenHashMap<>();

    private static HashSet<String> terms = new HashSet<>();

    private static Connection conn;
    private static String user = "root";
    private static String password = "1234";
    private static String url = "localhost";
    private static String port = "3306";
    private static String DB = "lisa";

    public static void main(String[] args)throws Exception{

        System.out.println("333s s".matches(".*[a-zA-Z]{3,}"));
        System.out.println("sss333".matches("[a-zA-Z]{3,}.*"));



        /*
//        Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();
        Int2ObjectOpenHashMap<String> map = new Int2ObjectOpenHashMap<>();
        BufferedReader bf = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\indexes_s.txt"));
        String line;
        while((line = bf.readLine()) != null){
            map.put(Integer.parseInt(line.split("\t")[1]), line.split("\t")[0]);
        }

        PrintWriter pw = new PrintWriter("A:\\wikipedia\\enwiki\\text.txt");
        bf = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\dict_s.txt"));
        while((line = bf.readLine()) != null){
            StringBuilder sb = new StringBuilder();
            for(String t : line.split("\t")[0].split(" "))
                sb.append(map.get(Integer.parseInt(t))).append(" ");
            pw.println(sb.toString() + "\t" + line.split("\t")[1]);
        }
        pw.close();







        System.exit(1);
        WikipediaParser parser = new WikipediaParser();
        String test = "==== August 25  Why are human stools so offensive?  In comparison to other mammals, why are human droppings so offensive.";

        System.out.println(parser.parse(test));
        System.exit(1);
        String s = ".";
        WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
        s = s.replaceAll("“", "").replaceAll("”", "");
        String t = wikiModel.render(new PlainTextConverter(), s);

        s = "He first articulated this in 1837, saying, [The] Institution of slavery is founded on both injust";

        t = wikiModel.render(new PlainTextConverter(), s);
        System.out.print(t);

        System.exit(3);
/*        BufferedReader bf = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\dump.sql"));
        String line;

        PrintWriter pw = new PrintWriter("A:\\wikipedia\\enwiki\\file.txt");
        int n = 0;
        while((line = bf.readLine()) != null){
            if(++n > 50 )
                break;
            pw.println(line.replaceAll("\\),\\(", "\n"));
        }
        pw.close();*/

//        code5();

    }

    private static String terms2Ints(String str){
        StringBuilder result = new StringBuilder();
        for(String i : str.split(" ")){
            if(ids.containsKey(i))
                result.append(ids.get(i)).append(" ");
        }
        return result.toString();
    }

    public static void code5() throws Exception {

        String[] testsn = new String[]{"20ng", "webkb", "r52", "r8", "cade"};
        boolean[] b = new boolean[]{false, true};

        for(String test : testsn)
            for(boolean d : b){
                System.out.println(test);
                System.out.println(d);

                HashSet<String> dictionary = new HashSet<>();

                File file = new File("A:\\wikipedia\\enwiki\\pop_bigrams.txt");
//                File file = new File("A:\\wikipedia\\enwiki\\ngramms_stem.txt");

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                int doc = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.charAt(0) == 65279)
                        line = line.substring(1);
//                    int index = line.lastIndexOf(" ");
//                    dictionary.add(line.substring(0, index));
                    dictionary.add(line);
                }

                TF tf = new TF();
                TF tfExt = new TF(dictionary);

//                NearestNeighbours usual = new NearestNeighbours(tf);
//                NearestNeighbours extended = new NearestNeighbours(tfExt);

                MRocchio usual = new MRocchio(tf);
                MRocchio extended = new MRocchio(tfExt);

                extended.setUsingAllSequances(d);

                HashMap<String, Integer> mapOfClasses = new HashMap<>();

                reader = new BufferedReader(new FileReader("A:\\wikipedia\\datasets\\datasets\\" + test + "-train-stemmed.txt"));

                while ((line = reader.readLine()) != null) {
                    if (line.split("\t").length != 2)
                        continue;
                    String cls = line.split("\t")[0];
                    if (!mapOfClasses.containsKey(cls))
                        mapOfClasses.put(cls, mapOfClasses.size() + 1);
                    int[] c = new int[]{mapOfClasses.get(cls)};
                    String body = line.split("\t")[1];
                    usual.addDocument(body, c);
                    extended.addDocument(body, c);
                }

//                System.out.println("Trained " + new Date());

                reader = new BufferedReader(new FileReader("A:\\wikipedia\\datasets\\datasets\\" + test + "-test-stemmed.txt"));

                int tests = 0;
                int uc = 0;
                int ec = 0;
                int conf = 0;


                while ((line = reader.readLine()) != null) {
                    if (line.split("\t").length != 2)
                        continue;
//                    if (tests % 10 == 0)
//                        System.out.print("\r" + tests);
//                    if (tests % 100 == 0)
//                        System.out.print(" : " + 1. * uc / tests + "\t" + 1. * ec / tests + "\n");

                    tests++;
                    int c = mapOfClasses.get(line.split("\t")[0]);
                    String body = line.split("\t")[1];
                    int up = usual.classify(body)[0];
                    int ep = extended.classify(body)[0];
                    if (up == c)
                        uc++;
                    if (ep == c)
                        ec++;
                    if (up == ep)
                        conf++;
//                    Int2DoubleOpenHashMap ur = usual.weights(body);
//                    Int2DoubleOpenHashMap er = extended.weights(body);
//                    Int2DoubleOpenHashMap res = new Int2DoubleOpenHashMap(er);
//                    for(int i : ur.keySet())
//                        res.addTo(i, ur.get(i));
//                    double max = Collections.max(res.values());
//                    int p = -1;
//                    for(int i : res.keySet())
//                        if(res.get(i) == max)
//                            p = i;
//                    if(p == c)
//                        conf++;
                }

                System.out.println("Usual correct: " + 1. * uc / tests);
                System.out.println("Extended correct: " + 1. * ec / tests);
                System.out.println("Confidence: " + 1. * conf / tests);
                System.out.println("=============");
            }
    }

    public static void code4() throws Exception{

        BufferedReader reader = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\parsed.txt"));
        String line;
        int progress = 0;

        while ((line = reader.readLine()) != null) {
            if(progress++ % 100 == 0)
                System.out.print("\r" + progress);
            ArrayList<byte[]> docs = new ArrayList<>();
            int first = line.indexOf("'");
            while(first >= 0){
                int last = line.indexOf("'", first + 1);
                String str = line.substring(first + 1, last);
                byte[] doc = intArrayToByteArray(getArray(str));
                if(doc.length >= 50*4)
                    docs.add(doc);
                line = line.substring(last + 1);
                first = line.indexOf("'");
            }
            SQLQuery.saveWikiDocs(docs);
        }

    }

    public static void code3() throws Exception{

        Int2ObjectOpenHashMap<String> terms = new Int2ObjectOpenHashMap<>();

        BufferedReader indexes = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\indexes.txt"));
        String line;
        while ((line = indexes.readLine()) != null) {
            terms.put(Integer.parseInt(line.split("\t")[1]), line.split("\t")[0]);
        }

        PrintWriter pw = new PrintWriter("A:\\results.txt");

        indexes = new BufferedReader(new FileReader("C:\\Users\\Юлиан\\IdeaProjects\\LisaMaven\\src\\main\\resources\\romip\\dictionary_logs\\2990100.txt"));
        while ((line = indexes.readLine()) != null) {
            StringBuilder sb = new StringBuilder();
            for(String i : line.split("\t")[0].split(" "))
                sb.append(terms.get(Integer.parseInt(i))).append(" "); //Для аррей листа нужно делать - 1
            sb.append(line.split("\t")[1]);
            pw.println(sb.toString());
        }

    }

    public static void code2() throws Exception{

        Object2IntOpenHashMap<String> newTerms = new Object2IntOpenHashMap<>();

        BufferedReader indexes = new BufferedReader(new FileReader("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\new_indexes.txt"));
        String line;
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        while ((line = indexes.readLine()) != null) {
            int i1 = Integer.parseInt(line.split("\t")[0]);
            int i2 = Integer.parseInt(line.split("\t")[1]);
            map.put(i1, i2);
        }
        IntArrayList docsi = new IntArrayList();
        for(int i = 0; i < 3091344; i++){
            docsi.add(i);
            if(docsi.size() == 500){
                System.out.print("\r" + i);
                ArrayList<byte[]> newDocs = new ArrayList<>();
                List<int[]> docs = SQLQuery.getWikiDocs("en", docsi.toIntArray());
                for(int[] doc : docs){
                    IntArrayList newDoc = new IntArrayList();
                    for(int term : doc)
                        if(map.containsKey(term))
                            newDoc.add(map.get(term));
                    if(newDoc.size() > 50){
                        newDocs.add(intArrayToByteArray(newDoc.toIntArray()));
                    }
                }
                if(newDocs.size() > 0)
                    SQLQuery.saveWikiDocs(newDocs);
                docsi.clear();
            }
        }

    }

    public static void code() throws Exception{



        Object2IntOpenHashMap<String> newTerms = new Object2IntOpenHashMap<>();

        BufferedReader terms = new BufferedReader(new FileReader("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\terms.txt"));
        BufferedReader lemms = new BufferedReader(new FileReader("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\terms_lemm.txt"));
        BufferedReader freqs = new BufferedReader(new FileReader("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\freqs.txt"));

        PrintWriter pw = new PrintWriter("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\new_indexes.txt");
        PrintWriter lemmed = new PrintWriter("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\lemmed.txt");

        String term;
        String lemm;
        int freq;
        int progress = 0;
        int index = 1;
        while ((term = terms.readLine()) != null) {
            if(++progress%100 == 0)
                System.out.print("\r" + progress);
            lemm = lemms.readLine();
            int mark = lemm.indexOf("?");
            if(mark > -1)
                lemm = lemm.substring(0, mark);
            mark = lemm.indexOf("|");
            if(mark > -1)
                lemm = lemm.substring(0, mark);
            freq = Integer.parseInt(freqs.readLine());
            if(freq > 100){
                if(!newTerms.containsKey(lemm))
                    newTerms.put(lemm, newTerms.size() + 1);
                pw.println(index + "\t" + newTerms.getInt(lemm));
                lemmed.println(term + "\t" + lemm);
            }
            index++;
        }
        pw.close();
        lemmed.close();
        pw = new PrintWriter("A:\\wikipedia\\ruwiki-20150603-pages-meta-current.xml\\new_terms.txt");
        LinkedHashMap<String, Integer>  map = sortByValues(newTerms);
        for(String key : map.keySet()){
            pw.println(key + "\t" + map.get(key));
        }
        pw.close();

        System.out.println("\n" + newTerms.keySet().size());

    }

    private static int[] getArray(String str){
        IntArrayList list = new IntArrayList();
        for(String i : str.split(" ")){
            if(!i.equals("null") && i.length() > 0)
                list.add(Integer.parseInt(i));
        }
        return list.toIntArray();
//        return new int[]{1};
    }

    public static void parseWikiText2(File file)  throws Exception{

        String dir = "A:\\wikipedia\\enwiki\\";

        {
            BufferedReader reader = new BufferedReader(new FileReader(dir + "terms.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if(Integer.parseInt(line.split("\t")[1]) > 1000)
                    ids.put(line, ids.size() + 1);
            }
        }



        System.out.println("Размер словаря: " + ids.size());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        PrintWriter pw = new PrintWriter(dir + "parsed2.txt");
        int progress = 0;
        int exc = 0;
        long max = file.length();
        StringBuilder current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
        int start = 0;
        int step = 1000000;
        StringBuilder line = new StringBuilder();
        char[] buf = new char[step + 1];
        while (reader.read(buf, 0, step) != -1) {
            line.append(buf);
            System.out.println(line);
            System.exit(1);
/*            try {
                progress++;
                int first = line.indexOf("'");
                while (first > 0) {
                    int last = line.indexOf("'", first + 1);
                    if(last == -1)
                        break;
                    current.append(line.substring(0, first)).append("'");
                    String body = line.substring(first + 1, last);
                    body = terms2Ints(body);
                    current.append(body).append("'");
                    line = new StringBuilder(line.substring(last + 1));
                    first = line.indexOf("'");
                    if(current.length() > 1000000) {
                        pw.println(current.toString().substring(0, current.length() - 1) + ";");
                        current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
                    }
                }
                current.append("),");
            }catch (Exception ex){
                current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
                exc++;
            }
            pw.println(current.toString().substring(0, current.length() - 1) + ";");
            current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
            start += step;
            if(progress % 100 == 0)
                System.out.println("обработано  :\t" + start + "/" + max + "\t число ошибок: \t" + exc);*/
        }
        pw.println(current.toString().substring(0, current.length() - 1) + ";");
        pw.close();
    }

    public static void parseWikiText(File file)  throws Exception{

        String dir = "A:\\wikipedia\\enwiki\\";

        {
            BufferedReader reader = new BufferedReader(new FileReader(dir + "terms.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if(Integer.parseInt(line.split("\t")[1]) > 1000 && line.split("\t")[0].length() > 1)
                    ids.put(line.split("\t")[0], ids.size() + 1);
            }
            PrintWriter pw = new PrintWriter(dir + "indexes.txt");
            for(String s : ids.keySet()){
                pw.println(s + "\t" + ids.getInt(s));
            }
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        PrintWriter pw = new PrintWriter(dir + "parsed.txt");
        int i = 0;
        int progress = 1;
        int exc = 0;
        StringBuilder current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
        while ((line = reader.readLine()) != null) {
            try {
                if (line.startsWith("INSERT INTO text (old_id,old_text,old_flags) VALUES ")) {
                    line = line.replaceAll("\\\\'", " ");
                    i++;
                    progress++;
                    String query = line.substring(52);
                    int first = query.indexOf("'");
                    while (first > 0) {
                        int last = query.indexOf("'", first + 1);
                        current.append(query.substring(0, first)).append("'");
                        String body = query.substring(first + 1, last).replaceAll("[\\[\\]́]", "").replaceAll("[^a-zA-Z]", " ").toLowerCase();
                        body = toStem(body);
                        current.append(body).append("'");
                        query = query.substring(last + 1);
                        first = query.indexOf("'");
                    }
                    current.append("),");
                }
            }catch (Exception ex){
                current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");
                i = 0;
                exc++;
            }

            if(i > 5){

                pw.println(current.toString().substring(0, current.length() - 1) + ";");
                i = 0;
                current = new StringBuilder("INSERT INTO text (old_id,old_text,old_flags) VALUES ");

            }

            if(progress % 100 == 0)
                System.out.println("обработано строк :\t" + progress + "\t число ошибок: \t" + exc + "\t число уникальных слов: \t" + terms.size());
        }
        pw.println(current.toString().substring(0, current.length() - 1) + ";");
        pw.close();
    }

    public static void kNN() throws Exception{



        HashSet<String> dictionary = new HashSet<>();

        File file = new File("src/main/resources/romip/ngramms_wiki.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.charAt(0) == 65279)
                line = line.substring(1);
            int index = line.lastIndexOf(" ");
            dictionary.add(line.substring(0, index));
        }

        TF tf = new TF();
        TF tfExt = new TF(dictionary);

        Object2IntOpenHashMap<String> classesIndexes = new Object2IntOpenHashMap<>();
        for(String cls : DocumentClasses.getClasses()){
            classesIndexes.put(cls, classesIndexes.size() + 1);
        }


        int nTests = 100;

        for(int i = 0; i < nTests; i++){

            NearestNeighbours usual = new NearestNeighbours(tf);
            NearestNeighbours extended = new NearestNeighbours(tfExt);
//            MRocchio usual = new MRocchio(tf);
//            MRocchio extended = new MRocchio(tfExt);

            HashSet<String> train = new HashSet<>();
            HashSet<String> test = new HashSet<>();

            for(String doc : DocumentClasses.getKeySet()){
                if(Math.random() > 0.1) {
                    train.add(doc);
                    if(train.size() == 1000){
                        Map<String, String> bodies = SQLQuery.getArticleRomipBodies(train);
                        for(String key : bodies.keySet()){
                            IntArrayList classes = new IntArrayList();
                            for(String cls : DocumentClasses.getClassOfDoc(key)){
                                classes.add(classesIndexes.get(cls));
                            }
                            usual.addDocument(bodies.get(key), classes.toIntArray());
                            extended.addDocument(bodies.get(key), classes.toIntArray());
                        }
                        train.clear();
                    }
                }
                else {
                    test.add(doc);
                }
            }

            {
                Map<String, String> bodies = SQLQuery.getArticleRomipBodies(train);
                for(String key : bodies.keySet()){
                    IntArrayList classes = new IntArrayList();
                    for(String cls : DocumentClasses.getClassOfDoc(key)){
                        classes.add(classesIndexes.get(cls));
                    }
                    usual.addDocument(bodies.get(key), classes.toIntArray());
                    extended.addDocument(bodies.get(key), classes.toIntArray());
                }
                train.clear();
            }

            System.out.println("Trained");

            int u_precision = 0;
            int u_recall = 0;

            int e_precision = 0;
            int e_recall = 0;

            double u_size = 0;
            double e_size = 0;

            double r_size = 0;


            {
                Map<String, String> bodies = SQLQuery.getArticleRomipBodies(test);
                double t = 0;
                for(String id : bodies.keySet()){
                    System.out.print("\r" + t++/bodies.keySet().size());
                    IntOpenHashSet classes = new IntOpenHashSet();
                    for(String label : DocumentClasses.getClassOfDoc(id)){
                        classes.add(classesIndexes.getInt(label));
                    }
                    IntOpenHashSet uPredicted = new IntOpenHashSet(usual.classify(bodies.get(id)));
                    IntOpenHashSet ePredicted = new IntOpenHashSet(extended.classify(bodies.get(id)));

                    u_size += uPredicted.size();
                    e_size += ePredicted.size();
                    r_size += classes.size();

                    for(int p : uPredicted){
                        if(classes.contains(p))
                            u_precision++;
                    }

                    for(int p : ePredicted){
                        if(classes.contains(p))
                            e_precision++;
                    }

                    for(int c : classes){
                        if(uPredicted.contains(c))
                            u_recall++;
                        if(ePredicted.contains(c))
                            e_recall++;
                    }

                }
            }

            double uf = 2.*u_precision/u_size*u_recall/r_size/(u_precision/u_size + u_recall/r_size);
            double ef = 2.*e_precision/e_size*e_recall/r_size/(e_precision/e_size + e_recall/r_size);

            System.out.println("\nUsual : " + u_precision/u_size + "\t" + u_recall/r_size + "\t" + uf);
            System.out.println("Extended : " + e_precision/e_size + "\t" + e_recall/r_size + "\t" + ef);

            System.out.println("_______________________________________________");

        }
    }


    public static void test3() throws Exception{



        HashSet<String> dictionary = new HashSet<>();

        File file = new File("src/main/resources/romip/ngramms.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            dictionary.add(line.split("\t")[0]);
        }

        TF tf = new TF();
        TF tfExt = new TF(dictionary);

        Object2IntOpenHashMap<String> terms = new Object2IntOpenHashMap<>();

        System.out.println("Dict size = " + dictionary.size());

        for(String term : dictionary){
            terms.put(term, terms.size() + 1);
        }


        int nTests = 100;
        double sum = 0;

        for(int i = 0; i < nTests; i++){

            System.out.println(i);
            RomipClassificationTest test = new RomipClassificationTest();
            System.out.println("Test");
            WekaInstance wekaInstance = new WekaInstance();
            WekaInstance wekaInstanceExt = new WekaInstance();
            int n = 0;
            for(String doc : DocumentClasses.getKeySet()){
                SQLQuery.disconnect();
                if(!test.isForTest(doc))
                    for(String cls : DocumentClasses.getClassOfDoc(doc)){
                        String str = SQLQuery.getArticleRomipBody(doc);
                        if(str != null){
                            Object2IntOpenHashMap<String> map = tf.mapOfTheString(str);
                            Vector vector = new Vector();
                            for(String key : map.keySet()){
                                if(!terms.containsKey(key))
                                    terms.put(key, terms.size() + 1);
                                vector.put(terms.getInt(key), map.getInt(key));
                            }
                            wekaInstance.addVector(cls, vector);

                            Object2IntOpenHashMap<String> mapExt = tfExt.mapOfTheString(str);

                            Vector vectorExt = new Vector();
                            for(String key : mapExt.keySet()){
                                if(!terms.containsKey(key))
                                    terms.put(key, terms.size() + 1);
                                vectorExt.put(terms.getInt(key), mapExt.getInt(key));
                            }
                            wekaInstanceExt.addVector(cls, vectorExt);

                            n++;
                            if(n%100 == 0){
                                System.out.print("\r" + n);
                            }
                        }
                    }
            }
            System.out.println("\nLearn = " + n);
            {
                System.out.println("SVM");
                long time1 = System.currentTimeMillis();
                test.test2(new RandomForest(wekaInstanceExt), terms, tfExt);
                long time2 = System.currentTimeMillis();
                test.test2(new RandomForest(wekaInstance), terms, tf);
                long time3 = System.currentTimeMillis();
                System.out.println("time2 = " + (time2 - time1)/1000.);
                System.out.println("time3 = " + (time3 - time2)/1000.);
            }
        }
        System.out.println(sum/nTests);
    }


    public static void test2() throws Exception{
        HashSet<String> dictionary = new HashSet<>();

        File file = new File("src/main/resources/romip/ngramms_lemm.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            dictionary.add(line.split("\t")[0]);
        }

        TF tf = new TF();
        TF tfExt = new TF(dictionary);

        Object2IntOpenHashMap<String> terms = new Object2IntOpenHashMap<>();

        System.out.println("Dict size = " + dictionary.size());

        for(String term : dictionary){
            terms.put(term, terms.size() + 1);
        }

        int nTests = 100;
        double sum = 0;

        for(int i = 0; i < nTests; i++){

            System.out.println(i);
            RomipClassificationTest test = new RomipClassificationTest();
            System.out.println("Test");
            TopicClassifier tc = new TopicClassifier();
            TopicClassifier tcExt = new TopicClassifier();
            int n = 0;
            WekaInstance wekaInstance = new WekaInstance();

            for(String doc : DocumentClasses.getKeySet()){
                SQLQuery.disconnect();
                if(!test.isForTest(doc))
                    for(String cls : DocumentClasses.getClassOfDoc(doc)){
                        String str = SQLQuery.getArticleRomipBody(doc);
                        if(str != null){
                            tc.addVector(cls, tf.mapOfTheString(str));
                            tcExt.addVector(cls, tfExt.mapOfTheString(str));
                            n++;
                            if(n%10 == 0){
                                System.out.print("\r" + n);
                            }
                        }
                    }
            }
            System.out.println("\nLearn = " + n);
            long time1 = System.currentTimeMillis();
            test.test3(tc, terms, tf);
            long time2 = System.currentTimeMillis();
            test.test3(tcExt, terms, tfExt);
            long time3 = System.currentTimeMillis();
            System.out.println("time2 = " + (time2 - time1)/1000.);
            System.out.println("time3 = " + (time3 - time2)/1000.);
        }
        System.out.println(sum/nTests);
    }

    public static void test() throws Exception{



        HashSet<String> dictionary = new HashSet<>();

        File file = new File("src/main/resources/romip/ngramms_wiki.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.charAt(0) == 65279)
                line = line.substring(1);
            int index = line.lastIndexOf(" ");
            dictionary.add(line.substring(0, index));
        }

        TF tf = new TF();
        TF tfExt = new TF(dictionary);

        Object2IntOpenHashMap<String> terms = new Object2IntOpenHashMap<>();

        System.out.println("Dict size = " + dictionary.size());

        for(String term : dictionary){
            terms.put(term, terms.size() + 1);
        }

        int nTests = 100;
        double sum = 0;

        for(int i = 0; i < nTests; i++){

            System.out.println(i);
            RomipClassificationTest test = new RomipClassificationTest();
            System.out.println("Test");
            WekaInstance wekaInstance = new WekaInstance();
            Rocchio rocchio = new Rocchio();
            Rocchio rocchioExt = new Rocchio();
            TopicClassifier tc = new TopicClassifier();
            TopicClassifier tcExt = new TopicClassifier();
            int n = 0;
            for(String doc : DocumentClasses.getKeySet()){
                SQLQuery.disconnect();
                if(!test.isForTest(doc))
                    for(String cls : DocumentClasses.getClassOfDoc(doc)){
                        String str = SQLQuery.getArticleRomipBody(doc);
                        if(str != null){
                            Object2IntOpenHashMap<String> map = tf.mapOfTheString(str);
                            tc.addVector(cls, map);
                            Vector vector = new Vector();
                            for(String key : map.keySet()){
                                if(!terms.containsKey(key))
                                    terms.put(key, terms.size() + 1);
                                vector.put(terms.getInt(key), map.getInt(key));
                            }
                            rocchio.addVector(cls, vector);

                            Object2IntOpenHashMap<String> mapExt = tfExt.mapOfTheString(str);
                            tcExt.addVector(cls, mapExt);
                            Vector vectorExt = new Vector();
                            for(String key : mapExt.keySet()){
                                if(!terms.containsKey(key))
                                    terms.put(key, terms.size() + 1);
                                vectorExt.put(terms.getInt(key), mapExt.getInt(key));
                            }
                            rocchioExt.addVector(cls, vectorExt);

                            n++;
                            if(n%10 == 0){
                                System.out.print("\r" + n);
                            }
                        }
                    }
            }
            System.out.println("\nLearn = " + n);
            {
                System.out.println("Rocchio");
                long time1 = System.currentTimeMillis();
                test.test2(rocchio, terms, tf);
                long time2 = System.currentTimeMillis();
                test.test2(rocchioExt, terms, tfExt);
                long time3 = System.currentTimeMillis();
                System.out.println("time2 = " + (time2 - time1)/1000.);
                System.out.println("time3 = " + (time3 - time2)/1000.);
            }
            {
                System.out.println("TopicClassifiers");
                long time1 = System.currentTimeMillis();
                test.test3(tc, terms, tf);
                long time2 = System.currentTimeMillis();
                test.test3(tcExt, terms, tfExt);
                long time3 = System.currentTimeMillis();
                System.out.println("time2 = " + (time2 - time1)/1000.);
                System.out.println("time3 = " + (time3 - time2)/1000.);
            }
        }
        System.out.println(sum/nTests);
    }

    private static String toStem(String string){
        StringBuilder result = new StringBuilder();
        EnglishStemmer stemmer = new EnglishStemmer();
        for(String s : string.split(" ")){
            stemmer.setCurrent(s);
            stemmer.stem();
            String r = stemmer.getCurrent();
//            if(ids.keySet().contains(r))
            result.append(r).append(" ");
        }
        return result.toString();
    }

}