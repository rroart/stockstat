package roart.util;

import java.io.IOException;

import org.junit.Test;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.commons.analysis.BeliefSetInconsistencyMeasure;
import org.tweetyproject.logics.commons.analysis.EtaInconsistencyMeasure;
import org.tweetyproject.logics.commons.analysis.MaInconsistencyMeasure;
import org.tweetyproject.logics.commons.analysis.MiInconsistencyMeasure;
import org.tweetyproject.logics.commons.analysis.MicInconsistencyMeasure;
import org.tweetyproject.logics.commons.analysis.NaiveMusEnumerator;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.analysis.ContensionInconsistencyMeasure;
import org.tweetyproject.logics.pl.analysis.PmInconsistencyMeasure;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.sat.PlMusEnumerator;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.semantics.PossibleWorldIterator;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.Proposition;
import org.tweetyproject.logics.pl.syntax.PlSignature;
import org.tweetyproject.math.opt.solver.Solver;
import org.tweetyproject.math.opt.solver.ApacheCommonsSimplex;

public class Test14 {
    @Test
	public void main() throws ParserException, IOException{
		PlBeliefSet k = new PlBeliefSet();
		PlParser parser = new PlParser();

		k.add(parser.parseFormula("a"));
		k.add(parser.parseFormula("b"));
		k.add(parser.parseFormula("!a || !b"));
		k.add(parser.parseFormula("!a"));
		
		System.out.println(k);
		System.out.println();
		
		PlMusEnumerator.setDefaultEnumerator(new NaiveMusEnumerator<PlFormula>(new Sat4jSolver()));
		SatSolver.setDefaultSolver(new Sat4jSolver());
		Solver.setDefaultLinearSolver(new ApacheCommonsSimplex());
		
		BeliefSetInconsistencyMeasure<PlFormula> miInc = new MiInconsistencyMeasure<PlFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PlFormula> maInc = new MaInconsistencyMeasure<PlFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PlFormula> micInc = new MicInconsistencyMeasure<PlFormula>(PlMusEnumerator.getDefaultEnumerator());
		BeliefSetInconsistencyMeasure<PlFormula> cInc = new ContensionInconsistencyMeasure();
		BeliefSetInconsistencyMeasure<PlFormula> pmInc = new PmInconsistencyMeasure();
		//BeliefSetInconsistencyMeasure<PlBeliefSet, PlFormula> etaInc = new EtaInconsistencyMeasure<PlBeliefSet, PlFormula>(k, new PossibleWorldIterator((PlSignature) k.getSignature()));
		
		System.out.println("miInc: " + miInc.inconsistencyMeasure(k));
		System.out.println("micInc: " + micInc.inconsistencyMeasure(k));
		System.out.println("maInc: " + maInc.inconsistencyMeasure(k));
		System.out.println("cInc: " + cInc.inconsistencyMeasure(k));
		System.out.println("pmInc: " + pmInc.inconsistencyMeasure(k));
		//System.out.println("etaInc: " + etaInc.inconsistencyMeasure(k));
		
	}
}
