package bmodeltest.app.beauti;



import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.EnumInputEditor;
import beastfx.app.inputeditor.InputEditor;
import bmodeltest.math.distributions.NucleotideRevJumpSubstModelRatePrior;
import javafx.scene.control.Label;
import beast.base.core.BEASTInterface;
import beast.base.core.Input;

public class NucleotideRevJumpSubstModelRatePriorInputEditor extends InputEditor.Base {

	@Override
	public Class<?> type() {
		return NucleotideRevJumpSubstModelRatePrior.class;
	}

	public NucleotideRevJumpSubstModelRatePriorInputEditor(BeautiDoc doc) {
		super(doc);
	}
	
	@Override
	public void init(Input<?> input, BEASTInterface beastObject, int itemNr, ExpandOption isExpandOption,
			boolean addButtons) {
		pane.getChildren().add(new Label(beastObject.getID() + ":"));
		
		EnumInputEditor editor = new EnumInputEditor(doc);
		editor.init(beastObject.getInput("priorType"), beastObject, -1, isExpandOption, true);
		pane.getChildren().add(editor);
	}

}
