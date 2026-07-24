package bmodeltest.app.beauti;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Guards the parent of {@link BEASTModelTestInputEditor}.
 *
 * <p>The bModelTest site model is a BEAST 3 spec {@code SiteModel}, so its BEAUti editor
 * must extend the spec {@code SiteModelInputEditor}. It used to extend the legacy one,
 * which casts the {@code FixMeanMutationRatesOperator} plugin to the legacy
 * {@code BactrianDeltaExchangeOperator}; in a spec analysis that plugin is a spec
 * {@code DeltaExchangeOperator}, so selecting the model in BEAUti threw a
 * ClassCastException before the panel could render (BEAST2-Dev/bModelTest#8).
 *
 * <p>This is a pure class-relationship check -- it does not construct the editor, so it
 * needs no JavaFX toolkit and runs headless.
 */
public class BEASTModelTestInputEditorTest extends TestCase {

	@Test
	public void testExtendsSpecSiteModelInputEditor() {
		assertTrue(
				"BEASTModelTestInputEditor must extend the spec SiteModelInputEditor, not the legacy one",
				beastfx.app.inputeditor.spec.SiteModelInputEditor.class
						.isAssignableFrom(BEASTModelTestInputEditor.class));
	}

	@Test
	public void testDoesNotExtendLegacySiteModelInputEditor() {
		assertFalse(
				"BEASTModelTestInputEditor must not extend the legacy SiteModelInputEditor",
				beastfx.app.inputeditor.SiteModelInputEditor.class
						.isAssignableFrom(BEASTModelTestInputEditor.class));
	}

}
