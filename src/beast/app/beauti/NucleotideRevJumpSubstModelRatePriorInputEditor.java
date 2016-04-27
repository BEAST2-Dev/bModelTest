package beast.app.beauti;

import javax.swing.JLabel;

import beast.app.draw.EnumInputEditor;
import beast.app.draw.InputEditor;
import beast.core.BEASTInterface;
import beast.core.Input;
import beast.math.distributions.NucleotideRevJumpSubstModelRatePrior;

public class NucleotideRevJumpSubstModelRatePriorInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;

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
		add(new JLabel(beastObject.getID() + ":"));
		
		EnumInputEditor editor = new EnumInputEditor(doc);
		editor.init(beastObject.getInput("priorType"), beastObject, -1, isExpandOption, true);
		add(editor);
	}

}
