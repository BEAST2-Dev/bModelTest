package bmodeltest.app.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Guards the substitution model that BModelAnalyser builds internally when rendering
 * its output. Both renderers construct a NucleotideRevJumpSubstModel through
 * initByName(), which takes Object..., so passing parameters of the wrong type
 * compiles fine and only fails at run time -- exactly what happened when the rest of
 * the package moved to the beast.base.spec API and this tool was left behind.
 */
public class BModelAnalyserTest extends TestCase {

	private static final List<Integer> MODELS = Arrays.asList(121121, 123456);

	private static Map<Integer, Integer> countMap() {
		Map<Integer, Integer> countMap = new HashMap<>();
		countMap.put(121121, 30);
		countMap.put(123456, 70);
		return countMap;
	}

	private static Set<Integer> in95HPD() {
		return new HashSet<>(MODELS);
	}

	@Test
	public void testToDottyBuildsSubstModel() throws Exception {
		BModelAnalyser analyser = new BModelAnalyser();
		String dotty = analyser.toDotty(MODELS, countMap(), in95HPD(), 100);
		assertTrue("expected dotty graph output", dotty.startsWith("digraph {"));
		assertTrue("expected the sampled models to appear", dotty.contains("123456"));
	}

	@Test
	public void testToSVGBuildsSubstModel() throws Exception {
		BModelAnalyser analyser = new BModelAnalyser();
		String svg = analyser.toSVG(MODELS, countMap(), in95HPD(), 100);
		assertTrue("expected SVG output", svg.contains("<svg"));
		// named models are rendered with their friendly label rather than the raw model ID
		assertTrue("expected model 123456 to render as SYM/GTR", svg.contains("SYM/GTR"));
		assertTrue("expected model 121121 to render as K80/HKY", svg.contains("K80/HKY"));
	}

}
