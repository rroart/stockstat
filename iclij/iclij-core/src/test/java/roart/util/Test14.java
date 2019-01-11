package roart.util;

import java.io.IOException;

import org.junit.Test;

import net.sf.tweety.commons.ParserException;
import net.sf.tweety.logics.commons.analysis.BeliefSetInconsistencyMeasure;
import net.sf.tweety.logics.commons.analysis.EtaInconsistencyMeasure;
import net.sf.tweety.logics.commons.analysis.MaInconsistencyMeasure;
import net.sf.tweety.logics.commons.analysis.MiInconsistencyMeasure;
import net.sf.tweety.logics.commons.analysis.MicInconsistencyMeasure;
import net.sf.tweety.logics.commons.analysis.NaiveMusEnumerator;
import net.sf.tweety.logics.pl.PlBeliefSet;
import net.sf.tweety.logics.pl.analysis.ContensionInconsistencyMeasure;
import net.sf.tweety.logics.pl.analysis.PmInconsistencyMeasure;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.sat.PlMusEnumerator;
import net.sf.tweety.logics.pl.sat.Sat4jSolver;
import net.sf.tweety.logics.pl.sat.SatSolver;
import net.sf.tweety.logics.pl.semantics.PossibleWorldIterator;
import net.sf.tweety.logics.pl.syntax.PropositionalFormula;
import net.sf.tweety.logics.pl.syntax.PropositionalSignature;
import net.sf.tweety.math.opt.Solver;
import net.sf.tweety.math.opt.solver.ApacheCommonsSimplex;

public class Test14 {
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
		SatSolver.setDefaultSolver(new Sat4jSolver());
		Solver.setDefaultLinearSolver(new ApacheCommonsSimplex());
		
		BeliefSetInconsistencyMeasure<PropositionalFormula> miInc = new MiInconsistencyMeasure<PropositionalFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PropositionalFormula> maInc = new MaInconsistencyMeasure<PropositionalFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PropositionalFormula> micInc = new MicInconsistencyMeasure<PropositionalFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PropositionalFormula> cInc = new ContensionInconsistencyMeasure();
		BeliefSetInconsistencyMeasure<PropositionalFormula> pmInc = new PmInconsistencyMeasure();
		BeliefSetInconsistencyMeasure<PropositionalFormula> etaInc = new EtaInconsistencyMeasure<PropositionalFormula>(new PossibleWorldIterator((PropositionalSignature) k.getSignature()));
		
		System.out.println("miInc: " + miInc.inconsistencyMeasure(k));
		System.out.println("micInc: " + micInc.inconsistencyMeasure(k));
		System.out.println("maInc: " + maInc.inconsistencyMeasure(k));
		System.out.println("cInc: " + cInc.inconsistencyMeasure(k));
		System.out.println("pmInc: " + pmInc.inconsistencyMeasure(k));
		System.out.println("etaInc: " + etaInc.inconsistencyMeasure(k));
		
	}
}
