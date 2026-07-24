package bmodeltest.app.beauti;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Lints the BEAUti template for legacy BEAST 2 syntax.
 *
 * <p>The template is only ever read by BEAUti at startup, so nothing in the build
 * exercised it, and the migration left two priors declared the BEAST 2 way:
 *
 * <pre>
 *   &lt;Exponential name="distr"&gt;
 *       &lt;parameter lower="0.0" name="mean" upper="0.0"&gt;1.0&lt;/parameter&gt;
 *   &lt;/Exponential&gt;
 * </pre>
 *
 * A bare element name resolves through BEAST's element-name map to the legacy
 * {@code beast.base.inference.distribution.*} class, which no longer satisfies
 * BMTPrior's spec {@code ScalarDistribution} input. The result was
 * "type mismatch for input distr" when BEAUti parsed the template -- invisible to
 * the compiler and to every other test.
 */
public class FxTemplateTest extends TestCase {

	private static final String TEMPLATE = "/bmodeltest/fxtemplates/bModelTest.xml";

	/** Distribution classes that exist in both a legacy and a spec flavour. */
	private static final String LEGACY_DISTRIBUTIONS =
			"Exponential|Beta|Gamma|LogNormal|Normal|Uniform|LogUniform|Dirichlet|"
			+ "Poisson|InverseGamma|ChiSquare|Laplace|Cauchy";

	private static String readTemplate() throws Exception {
		try (InputStream in = FxTemplateTest.class.getResourceAsStream(TEMPLATE)) {
			assertNotNull("template not found on classpath: " + TEMPLATE, in);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * A distribution must be declared as {@code <distr spec="beast.base.spec...">},
	 * never as a bare {@code <Exponential name="distr">} element.
	 */
	@Test
	public void testNoBareLegacyDistributionElements() throws Exception {
		Matcher m = Pattern.compile("<(" + LEGACY_DISTRIBUTIONS + ")[\\s>/]").matcher(readTemplate());
		List<String> found = new ArrayList<>();
		while (m.find()) {
			found.add(m.group(1));
		}
		assertTrue("template uses legacy bare distribution elements " + found
				+ "; declare them as <distr spec=\"beast.base.spec.inference.distribution.X\"> instead",
				found.isEmpty());
	}

	/**
	 * The legacy {@code <parameter lower=.. upper=..>} form is gone in BEAST 3 --
	 * a parameter must carry a spec and express its bounds as a domain.
	 */
	@Test
	public void testNoLegacyParameterDeclarations() throws Exception {
		Matcher m = Pattern.compile("<parameter\\b[^>]*>").matcher(readTemplate());
		List<String> bad = new ArrayList<>();
		while (m.find()) {
			String tag = m.group();
			// idref-only references carry no type information and are still fine
			if (tag.contains("idref=")) {
				continue;
			}
			if (!tag.contains("spec=")) {
				bad.add(tag.trim());
			}
		}
		assertTrue("template declares parameters without a spec: " + bad, bad.isEmpty());
	}

	/** Everything the template instantiates should come from the spec hierarchy. */
	@Test
	public void testNoLegacyBeastBaseSpecs() throws Exception {
		Matcher m = Pattern.compile("spec=\"(beast\\.base\\.(?!spec\\.)[^\"]*)\"").matcher(readTemplate());
		List<String> legacy = new ArrayList<>();
		while (m.find()) {
			legacy.add(m.group(1));
		}
		assertTrue("template references legacy beast.base classes: " + legacy, legacy.isEmpty());
	}

}
