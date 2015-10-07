package ru.lisaprog;

import junitparams.JUnitParamsRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.FileReader;

@RunWith(JUnitParamsRunner.class)

/**
 * Created by Юлиан on 02.10.2015.
 */
public class Wikipedia {

    @Test
    @Ignore
    public void parsing() throws Exception{
        BufferedReader bf = new BufferedReader(new FileReader("A:\\wikipedia\\enwiki\\dump.sql"));
        String line;
        Wikipedia parser = new Wikipedia()
        while((line = bf.readLine()) != null){
            if(line.startsWith("INSERT INTO")){
                while(line.contains("'")){
                    int index = line.indexOf("'");
                    int nextIndex = line.indexOf("'");
                    for(nextIndex = index + 1; !(line.charAt(nextIndex) == '\'' &&
                            line.charAt(nextIndex - 1) != '\\')
                            ;nextIndex++);
                    String doc =
                }
            }
        }

    }


}
