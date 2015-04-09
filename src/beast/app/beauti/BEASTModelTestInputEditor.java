package beast.app.beauti;

import javax.swing.Box;

import beast.app.draw.EnumInputEditor;
import beast.core.BEASTInterface;
import beast.core.Input;
import beast.evolution.sitemodel.BEASTModelTest;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.evolution.substitutionmodel.SubstitutionModel;

public class BEASTModelTestInputEditor extends SiteModelInputEditor {
	private static final long serialVersionUID = 1L;

	@Override
    public Class<?> type() {
        return BEASTModelTest.class;
    }
    
	public BEASTModelTestInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
		SubstitutionModel sm = ((BEASTModelTest) input.get()).substModelInput.get();
		NucleotideRevJumpSubstModel substModel = (NucleotideRevJumpSubstModel) sm;
		EnumInputEditor typeEditor = new EnumInputEditor(doc);
		typeEditor.init(substModel.modelChoiseInput, substModel, itemNr, bExpandOption, bAddButtons);
		((Box) getComponent(0)).add(typeEditor, 2);
	}
	
}
