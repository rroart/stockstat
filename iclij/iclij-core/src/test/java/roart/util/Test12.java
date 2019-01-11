package roart.util;

import java.io.IOException;

import net.sf.tweety.arg.deductive.CompilationReasoner;
import net.sf.tweety.arg.deductive.DeductiveKnowledgeBase;
import net.sf.tweety.arg.deductive.accumulator.SimpleAccumulator;
import net.sf.tweety.arg.deductive.categorizer.ClassicalCategorizer;
import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;

import org.junit.Test;

public class Test12 {
    @Test
	public void main() throws ParserException, IOException{
		DeductiveKnowledgeBase k = new DeductiveKnowledgeBase();
		PlParser parser = new PlParser();
		
		PropositionalFormula a = (PropositionalFormula) parser.parseFormula("a");
		PropositionalFormula b = (PropositionalFormula) parser.parseFormula("b");
		k.add(a);
		k.add((PropositionalFormula) parser.parseFormula("!a || b"));
		k.add((PropositionalFormula) parser.parseFormula("!b"));
		k.add((PropositionalFormula) parser.parseFormula("b && !c"));
		k.add((PropositionalFormula) parser.parseFormula("c && a"));
		
		System.out.println(k);
		System.out.println();
		
		System.out.println(k.getDeductiveArguments(a.combineWithOr(b)));
		
		CompilationReasoner reasoner = new CompilationReasoner(k, new ClassicalCategorizer(), new SimpleAccumulator());
		
		System.out.println();
		
		System.out.println(reasoner.query(a).getAnswerBoolean());
		
	}
}
