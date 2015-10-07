package ru.lisaprog.wikipedia;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import ru.lisaprog.Common;
import ru.lisaprog.sql.SQLQuery;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Юлиан on 23.08.2015.
 */
public class WikipediaCommon {

    public static void findBigrams(String language) throws IOException{
        int maxIndex = SQLQuery.getMaxIndexOfWikiDocs(language);
        Long2IntOpenHashMap counts = new Long2IntOpenHashMap();
        for(int i = 0; i < maxIndex; i++){
            IntArrayList indexes = new IntArrayList();
            for(int j = i; i < j + 10000; i++)
                indexes.add(i);
            for(int[] doc : SQLQuery.getWikiDocs(language, indexes.toIntArray())){
                for(int t = 1; t < doc.length; t++)
                    counts.addTo(Common.twoIntToLong(doc[t - 1], doc[t]), 1);
            }
        }
        PrintWriter pw = new PrintWriter("A:\\wikipedia\\" + language + "_bigrams.txt");
        for(long key : Common.sortByValues(counts, true).keySet()){
            pw.write(Common.longToTwoInts(key)[0] + "\t" + Common.longToTwoInts(key)[1] + "\t" + counts.get(key));
        }
        pw.close();
    }


}
