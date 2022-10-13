package bmodeltest.app.beauti;


import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.BeautiPanel;
import beastfx.app.inputeditor.BooleanInputEditor;
import beastfx.app.inputeditor.EnumInputEditor;
import beastfx.app.inputeditor.SiteModelInputEditor;
import bmodeltest.evolution.sitemodel.BEASTModelTestSiteModel;
import bmodeltest.evolution.substitutionmodel.ModelFrequencies;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
		super.init(input, plugin, itemNr, ExpandOption.TRUE, bAddButtons);
		
		SubstitutionModel sm = ((BEASTModelTestSiteModel) input.get()).substModelInput.get();
		NucleotideRevJumpSubstModel substModel = (NucleotideRevJumpSubstModel) sm;
		EnumInputEditor typeEditor = new EnumInputEditor(doc);
		typeEditor.init(substModel.modelChoiseInput, substModel, itemNr, bExpandOption, bAddButtons);
		
		Pane p = (Pane) pane.getChildren().get(0);
		p = (Pane)p.getChildren().get(0);
		Label label = new Label("Model set: ");
		label.setMinSize(205, 20);
		label.setPadding(new Insets(5));
		p.getChildren().add(new HBox(label, typeEditor));
		
		ModelFrequencies freqs = (ModelFrequencies) substModel.frequenciesInput.get();
		BooleanInputEditor equalFreqs = new BooleanInputEditor(doc);
		equalFreqs.init(freqs.empiricalInput, freqs, itemNr, bExpandOption, bAddButtons);
		Label label2 = new Label("Frequencies: ");
		label2.setMinSize(205, 20);
		label2.setPadding(new Insets(5));
		p.getChildren().add(new HBox(label2, equalFreqs));

		validateInput();
	}
	
}
