package roart.parse;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NumericValueAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import roart.iclij.model.parse.ParseData;
import roart.iclij.model.parse.ParseLocation;
import roart.iclij.model.parse.ParseObject;
import roart.iclij.model.parse.ParseTime;
import roart.iclij.model.parse.ParseUnit;

public class MyParse {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String TEMPORAL_MODIFIER = "temporal modifier";
    public static final String NUMERIC_MODIFIER = "numeric modifier";
    public static final String NMOD_PREPOSITION = "nmod_preposition";
    public static final String ADJECTIVAL_MODIFIER = "adjectival modifier";
    public static final String DEPENDENT = "dependent";
    public static final String ROOT = "root";
    public static final String COPULA = "copula";
    public static final String DETERMINER = "determiner";
    public static final String NOMINAL_SUBJECT = "nominal subject";
    public static final String DIRECT_OBJECT = "direct object";

    public static final String WHO = "who";
    public static final String WHAT = "what";
    public static final String WHICH = "which";

    public static final String AFTER = "after";
    public static final String BEFORE = "before";
    public static final String SINCE = "since";

    LexicalizedParser lexicalizedParser;

    StanfordCoreNLP pipeline;

    public MyParse() {
        //lexicalizedParser = getLexicalizedParser();

        System.out.println("Setting up myparse.");
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        pipeline = new StanfordCoreNLP(props);
        System.out.println("Setting up myparse, done.");

    }

    public ParseData parseme(String q) {
        // create a document object
        System.out.println("Creating core document");
        CoreDocument document = new CoreDocument(q);
        // annnotate the document
        System.out.println("Annotating document");
        pipeline.annotate(document);
        System.out.println("Done annotating");

        // sentence
        CoreSentence sentence = document.sentences().get(0);

        List<String> posTags = sentence.posTags();
        System.out.println("Pos tags");
        System.out.println(posTags);
        System.out.println();

        List<String> nerTags = sentence.nerTags();
        System.out.println("Ner tags");
        System.out.println(nerTags);
        System.out.println();

        Tree constituencyParse = sentence.constituencyParse();
        System.out.println("Constituency parse");
        System.out.println(constituencyParse);
        System.out.println();

        SemanticGraph dependencyParse = sentence.dependencyParse();
        System.out.println("Dependency parse");
        System.out.println(dependencyParse);
        dependencyParse.prettyPrint();
        System.out.println();

        List<CoreEntityMention> entityMentions = sentence.entityMentions();
        System.out.println("Entity mentions");
        System.out.println(entityMentions);
        System.out.println();

        ParseData mydata = new ParseData();
        print(mydata, dependencyParse, dependencyParse.getFirstRoot(), mydata);
        mydata.show();
        return mydata;
    }

    public void prints(ParseUnit my, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata2) {
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
        for (IndexedWord child : dependencyParse.getChildList(indexedWord)) {
            System.out.println("child " + child.originalText());
            print(my, dependencyParse, child, mydata2);
        }
    }

    public void prints(ParseData my, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata2) {
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
        for (IndexedWord child : dependencyParse.getChildList(indexedWord)) {
            System.out.println("child " + child.originalText());
            print(my, dependencyParse, child, mydata2);
        }
    }

    public void prints(ParseTime my, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata2) {
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
        for (IndexedWord child : dependencyParse.getChildList(indexedWord)) {
            System.out.println("child " + child.originalText());
            print(my, dependencyParse, child, mydata2);
        }
    }

    public void print(ParseData mydata, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata2) {
        System.out.println("Main " + indexedWord.originalText());
        String origWord = indexedWord.originalText().toLowerCase();
        for (Class key : indexedWord.keySet()) {
            System.out.println("kv " + key + " " + indexedWord.get(key));
        }
        for (Class key : indexedWord.keySet()) {
            //System.out.println("kv2 " + key + " " + indexedWord.getString(key));
        }
        Set<GrammaticalRelation> relns = dependencyParse.relns(indexedWord);

        boolean plural = isPlural(origWord);
        String word = getSingular(origWord);
        ParseObject workData = mydata;
        if (getUnits().contains(word)) {
            //ParseData my = (ParseData) mydata;
            if (mydata.getUnit() == null) {
                mydata.setUnit(new ParseUnit());
                mydata.getUnit().setUnit(ParseUnit.getUnit(word));
            }
            workData = mydata.getUnit();
            prints(mydata.getUnit(), dependencyParse, indexedWord, mydata);
        }
        if (getTimeUnits().contains(word)) {
            //ParseData my = (ParseData) mydata;
            if (mydata.getTime() == null) {
                mydata.setTime(new ParseTime());
            }
            workData = mydata.getTime();
            prints(mydata.getTime(), dependencyParse, indexedWord, mydata);
        }
        prints(mydata, dependencyParse, indexedWord, mydata);
        //dependencyParse.ch
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
    }

    public void print(ParseUnit myunit, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata) {
        System.out.println("ParseUnit " + indexedWord.originalText() + " " + indexedWord.lemma() + " " + indexedWord.ner() + " " + indexedWord.tag());
        String origWord = indexedWord.originalText().toLowerCase();
        for (Class key : indexedWord.keySet()) {
            System.out.println("kv " + key + " " + indexedWord.get(key));
        }
        for (Class key : indexedWord.keySet()) {
            //System.out.println("kv2 " + key + " " + indexedWord.getString(key));
        }
        Set<GrammaticalRelation> relns = dependencyParse.relns(indexedWord);

        boolean plural = isPlural(origWord);
        myunit.setPlural(plural);
        String word = getSingular(origWord);
        myunit.setHigh(getHigh().contains(word));
        myunit.setLow(getLow().contains(word));
        if (indexedWord.ner().equals(NER_NATIONALITY)) {
            String n = indexedWord.get(LemmaAnnotation.class);
            System.out.println("NAT" + n);
            ParseLocation myloc = mydata.getLocation();
            if (myloc == null) {
                mydata.setLoc(new ParseLocation());
                myloc = mydata.getLocation();
            }
            myloc.setCountry(n);
        }
        if (indexedWord.ner().equals(NER_DATE)) {
            String n = indexedWord.get(NormalizedNamedEntityTagAnnotation.class);
            // 2017-06
            Timex n2 = indexedWord.get(TimexAnnotation.class);
            Calendar n3 = n2.getDate();
            System.out.println("afterdate " + n2.getDate());
            LocalDateTime dateTime = LocalDateTime.ofInstant(n3.toInstant(), ZoneId.systemDefault());
            LocalDate date = dateTime.toLocalDate();
            ParseTime time = mydata.getTime();
            if (time == null) {
                time = new ParseTime();
                mydata.setTime(time);
            }
            time.setDate(date);

        }
        if (indexedWord.ner().equals(NER_DURATION)) {
            String n = indexedWord.get(NormalizedNamedEntityTagAnnotation.class);
            // 2017-06, P1M
            Timex n2 = indexedWord.get(TimexAnnotation.class);
            ParseTime time = mydata.getTime();
            if (time == null) {
                time = new ParseTime();
                mydata.setTime(time);
            }
            time.setPeriod(n);
            //Calendar n3 = n2.getDate();
            //System.out.println("afterdate " + n2.getDate());
            //LocalDateTime dateTime = LocalDateTime.ofInstant(n3.toInstant(), ZoneId.systemDefault());
            //LocalDate date = dateTime.toLocalDate();
            //n2.
        }

        for (GrammaticalRelation reln : relns) {
            if (reln.getLongName().equals(NUMERIC_MODIFIER)) {
                myunit.setNumber(indexedWord.get(NumericValueAnnotation.class));                    
                System.out.println("num " + myunit.getNumber());
                //String n = indexedWord.get(NormalizedNamedEntityTagAnnotation.class);
                //System.out.println("Str " + n);
                //Timex n2 = indexedWord.get(TimexAnnotation.class);
                //System.out.println("Str2 " + " " + n2);
            }
            if (reln.getLongName().equals(TEMPORAL_MODIFIER)) {
                Number num = indexedWord.get(NumericValueAnnotation.class);                    
                System.out.println("num " + num);
                //String n = indexedWord.get(NormalizedNamedEntityTagAnnotation.class);
                //System.out.println("Str " + n);
                //Timex n2 = indexedWord.get(TimexAnnotation.class);
                //System.out.println("Str2 " + " " + n2);
            }
            if (reln.getLongName().equals(NMOD_PREPOSITION)) {
                String specific = reln.getSpecific();
                if (getAfter().contains(specific)) {

                }
            }
        }
        prints(myunit, dependencyParse, indexedWord, mydata);
        //dependencyParse.ch
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
    }

    public void print(ParseTime mytime, SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata) {
        System.out.println("ParseTime " + indexedWord.originalText() + " " + indexedWord.lemma() + " " + indexedWord.ner() + " " + indexedWord.tag());
        String origWord = indexedWord.originalText().toLowerCase();
        for (Class key : indexedWord.keySet()) {
            System.out.println("kv " + key + " " + indexedWord.get(key));
        }
        for (Class key : indexedWord.keySet()) {
            //System.out.println("kv2 " + key + " " + indexedWord.getString(key));
        }
        Set<GrammaticalRelation> relns = dependencyParse.relns(indexedWord);

        boolean plural = isPlural(origWord);
        mytime.setPlural(plural);
        String word = getSingular(origWord);
        switch (word) {
        case "last":
            mytime.setNow(true);
            break;
        case "current":
            mytime.setNow(true);
            break;
        case DECADE:
            mytime.setTimeunit(ChronoUnit.DECADES);
            break;
        case YEAR:
            mytime.setTimeunit(ChronoUnit.YEARS);
            break;
        case MONTH:
            mytime.setTimeunit(ChronoUnit.MONTHS);
            break;
        case WEEK:
            mytime.setTimeunit(ChronoUnit.WEEKS);
            break;
        case DAY:
            mytime.setTimeunit(ChronoUnit.DAYS);
            break;
        }
        for (GrammaticalRelation reln : relns) {
            if (reln.getLongName().equals(NUMERIC_MODIFIER)) {
                mytime.setNumber(indexedWord.get(NumericValueAnnotation.class));                    
                String n = indexedWord.get(NormalizedNamedEntityTagAnnotation.class);
                System.out.println("Str " + n);
                Timex n2 = indexedWord.get(TimexAnnotation.class);
                System.out.println("Str2 " + " " + n2);
            }
        }
        prints(mytime, dependencyParse, indexedWord, mydata);
        //dependencyParse.ch
        for (SemanticGraphEdge edge : dependencyParse.edgeIterable()) {
            //System.out.println("edge " + edge.getGovernor().originalText() + " " + edge.getDependent().originalText());
        }
    }

    public Set<String> timeUnits;

    public Set<String> getTimeUnits() {
        if (timeUnits == null) {
            timeUnits = new HashSet<>();
            timeUnits.add(DECADE);
            timeUnits.add(YEAR);
            timeUnits.add(MONTH);
            timeUnits.add(WEEK);
            timeUnits.add(DAY);
        }
        return timeUnits;
    }

    public Set<String> units;

    public Set<String> getUnits() {
        if (units == null) {
            units = new HashSet<>();
            units.add(ParseUnit.COMMODITIE);
            units.add(ParseUnit.COMMODITY);
            units.add(ParseUnit.FUND);
            units.add(ParseUnit.STOCK);
        }
        return units;
    }

    public static final String BEST = "best";
    public static final String HIGHEST = "highest";
    public static final String WORST = "worst";
    public static final String LOWEST = "lowest";

    public Set<String> lowest;

    public Set<String> getLow() {
        if (lowest == null) {
            lowest = new HashSet<>();
            lowest.add(LOWEST);
            lowest.add(WORST);
        }
        return lowest;
    }

    public Set<String> highest;

    public Set<String> getHigh() {
        if (highest == null) {
            highest = new HashSet<>();
            highest.add(HIGHEST);
            highest.add(BEST);
        }
        return highest;
    }

    public Set<String> before;

    public Set<String> getBefore() {
        if (before == null) {
            before = new HashSet<>();
            before.add(BEFORE);
        }
        return before;
    }

    public Set<String> after;

    public Set<String> getAfter() {
        if (after == null) {
            after = new HashSet<>();
            after.add(AFTER);
            after.add(SINCE);
        }
        return after;
    }

    //public abstract void print(SemanticGraph dependencyParse, IndexedWord indexedWord, ParseData mydata);

    public boolean isPlural(String word) {
        return word.endsWith("s");
    }

    public String getSingular(String word) {
        if (isPlural(word)) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    public static final String DECADE = "decade";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String WEEK = "week";
    public static final String DAY = "day";

    public static final String NER_DATE = "DATE";
    public static final String NER_NATIONALITY = "NATIONALITY";
    public static final String NER_DURATION = "DURATION";
}

