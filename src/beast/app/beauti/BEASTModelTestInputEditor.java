package beast.app.beauti;

import javax.swing.Box;

import beast.app.draw.BooleanInputEditor;
import beast.app.draw.EnumInputEditor;
import beast.core.BEASTInterface;
import beast.core.Input;
import beast.evolution.sitemodel.BEASTModelTestSiteModel;
import beast.evolution.substitutionmodel.ModelFrequencies;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.evolution.substitutionmodel.SubstitutionModel;

public class BEASTModelTestInputEditor extends SiteModelInputEditor {
	private static final long serialVersionUID = 1L;

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
		((Box) getComponent(0)).add(typeEditor, 2);
		
		ModelFrequencies freqs = (ModelFrequencies) substModel.frequenciesInput.get();
		BooleanInputEditor equalFreqs = new BooleanInputEditor(doc);
		equalFreqs.init(freqs.empiricalInput, freqs, itemNr, bExpandOption, bAddButtons);
		((Box) getComponent(0)).add(equalFreqs, 3);
		validate();
	}
	
}
