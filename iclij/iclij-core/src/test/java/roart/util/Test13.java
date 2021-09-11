package roart.util;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.commons.analysis.NaiveMusEnumerator;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.sat.PlMusEnumerator;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.Proposition;

public class Test13 {
    @Test
	public void main() throws ParserException, IOException{
		PlBeliefSet k = new PlBeliefSet();
		PlParser parser = new PlParser();

		k.add((Proposition) parser.parseFormula("a"));
		k.add((Proposition) parser.parseFormula("b"));
		k.add(parser.parseFormula("!a || !b"));
		k.add(parser.parseFormula("!a"));
		
		System.out.println(k);
		System.out.println();
		
		PlMusEnumerator.setDefaultEnumerator(new NaiveMusEnumerator<PlFormula>(new Sat4jSolver()));
		
		System.out.println("MUSes: " + PlMusEnumerator.getDefaultEnumerator().minimalInconsistentSubsets(k));
		System.out.println("MCSes: " + PlMusEnumerator.getDefaultEnumerator().maximalConsistentSubsets(k));
	}
}
