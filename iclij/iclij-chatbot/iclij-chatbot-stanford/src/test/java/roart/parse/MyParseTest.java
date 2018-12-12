package roart.parse;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.stanford.nlp.pipeline.Annotation;
import roart.model.parse.ParseData;
import roart.parse.MyParse;

public class MyParseTest {

    @Test
    public void method2() {
        String question = "Which is the best performing commodity stock?";
        System.out.println("q2 " + question);
        MyParse parse = new MyParse();
        //parse.parseSentence(question);
        String q2 = "Which is the best performing stocks?";
        //parse.parseSentence(q2);
        String q3 = "Which is the best performing swedish stocks last year?";
        //parse.parseSentence(q3);
        String q4 = "Which is the ten best performing swedish stocks last three years?";
        q4 = "Which is the ten best performing swedish stocks one month period after June 1 2017?";
        //parse.parseSentence(q4);
        //parse.mymethod3(text2);
        System.out.println(q4);
        ParseData mydata = parse.parseme(q4);
        System.out.println(mydata);
        assertEquals("P1M", mydata.getTime().getPeriod());
        q4 = "Which is the ten best performing swedish stocks one month duration after June 1 2017?";
        mydata = parse.parseme(q4);
        System.out.println(mydata);
        assertEquals("P1M", mydata.getTime().getPeriod());
    }

}