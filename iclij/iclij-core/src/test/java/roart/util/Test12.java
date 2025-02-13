package roart.util;

import java.io.IOException;

import org.tweetyproject.arg.deductive.reasoner.CompilationReasoner;
import org.tweetyproject.arg.deductive.syntax.DeductiveKnowledgeBase;
import org.tweetyproject.arg.deductive.accumulator.SimpleAccumulator;
import org.tweetyproject.arg.deductive.categorizer.ClassicalCategorizer;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.Proposition;

import org.junit.jupiter.api.Test;

public class Test12 {
    @Test
	public void main() throws ParserException, IOException{
		DeductiveKnowledgeBase k = new DeductiveKnowledgeBase();
		PlParser parser = new PlParser();
		
                Proposition p1 = (Proposition) parser.parseFormula("ose will rise, then equi will rise");
                Proposition p = (Proposition) parser.parseFormula("equi will rise");
                Proposition p2 = (Proposition) parser.parseFormula("equi will sink");
		Proposition a = (Proposition) parser.parseFormula("a");
		Proposition b = (Proposition) parser.parseFormula("b");
		k.add(a);
		k.add(parser.parseFormula("!a || b"));
		k.add(parser.parseFormula("!b"));
		k.add(parser.parseFormula("b && !c"));
		k.add(parser.parseFormula("c && a"));
		
		System.out.println(k);
		System.out.println();
		
		System.out.println(k.getDeductiveArguments(a.combineWithOr(b)));
		
		CompilationReasoner reasoner = new CompilationReasoner(new ClassicalCategorizer(), new SimpleAccumulator());
		
		System.out.println();
		
		System.out.println(reasoner.query(k, a));
		
	}
}
