package roart.util;

import java.io.IOException;

import org.junit.Test;

import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.commons.analysis.NaiveMusEnumerator;
import net.sf.tweety.logics.pl.PlBeliefSet;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.sat.PlMusEnumerator;
import net.sf.tweety.logics.pl.sat.Sat4jSolver;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

public class Test13 {
    @Test
	public void main() throws ParserException, IOException{
		PlBeliefSet k = new PlBeliefSet();
		PlParser parser = new PlParser();

		k.add((PropositionalFormula) parser.parseFormula("a"));
		k.add((PropositionalFormula) parser.parseFormula("b"));
		k.add((PropositionalFormula) parser.parseFormula("!a || !b"));
		k.add((PropositionalFormula) parser.parseFormula("!a"));
		
		System.out.println(k);
		System.out.println();
		
		PlMusEnumerator.setDefaultEnumerator(new NaiveMusEnumerator<PropositionalFormula>(new Sat4jSolver()));
		
		System.out.println("MUSes: " + PlMusEnumerator.getDefaultEnumerator().minimalInconsistentSubsets(k));
		System.out.println("MCSes: " + PlMusEnumerator.getDefaultEnumerator().maximalConsistentSubsets(k));
	}
}
