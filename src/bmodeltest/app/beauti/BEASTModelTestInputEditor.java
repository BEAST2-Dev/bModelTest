package bmodeltest.app.beauti;


import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BooleanInputEditor;
import beastfx.app.inputeditor.EnumInputEditor;
import beastfx.app.inputeditor.SiteModelInputEditor;
import bmodeltest.evolution.sitemodel.BEASTModelTestSiteModel;
import bmodeltest.evolution.substitutionmodel.ModelFrequencies;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.evolution.substitutionmodel.SubstitutionModel;

public class BEASTModelTestInputEditor extends SiteModelInputEditor {

	@Override
    public Class<?> type() {
        return BEASTModelTestSiteModel.class;
    }
    
	public BEASTModelTestInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
		SubstitutionModel sm = ((BEASTModelTestSiteModel) input.get()).substModelInput.get();
		NucleotideRevJumpSubstModel substModel = (NucleotideRevJumpSubstModel) sm;
		EnumInputEditor typeEditor = new EnumInputEditor(doc);
		typeEditor.init(substModel.modelChoiseInput, substModel, itemNr, bExpandOption, bAddButtons);
		
		pane.getChildren().add(typeEditor);
		// ((Box) getComponent(0)).add(typeEditor, 2);
		
		ModelFrequencies freqs = (ModelFrequencies) substModel.frequenciesInput.get();
		BooleanInputEditor equalFreqs = new BooleanInputEditor(doc);
		equalFreqs.init(freqs.empiricalInput, freqs, itemNr, bExpandOption, bAddButtons);
		pane.getChildren().add(typeEditor);
		// ((Box) getComponent(0)).add(equalFreqs, 3);
		validateInput();
	}
	
}
