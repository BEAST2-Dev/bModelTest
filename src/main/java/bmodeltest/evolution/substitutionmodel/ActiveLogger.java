package bmodeltest.evolution.substitutionmodel;

import java.io.PrintStream;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.core.Loggable;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.Real;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;

@Description("Logs parameter when indicated the parameter is in active use by the mask input")
public class ActiveLogger extends BEASTObject implements Loggable {
	final public Input<IntScalarParam<? extends NonNegativeInt>> maskInput = new Input<>("mask", "mask parameter assumed to take values 0 or 1", Validate.REQUIRED);
	final public Input<RealScalarParam<? extends Real>> parameterInput = new Input<>("parameter", "parameter of interetest, only logged if mask = 1", Validate.REQUIRED);

	private IntScalarParam<? extends NonNegativeInt> mask;
	private RealScalarParam<? extends Real> parameter;

	@Override
	public void initAndValidate() {
		parameter = parameterInput.get();
		mask = maskInput.get();
	}



	@Override
	public void init(PrintStream out) {
		out.print(getID() + "\t");
	}

	@Override
	public void log(long sample, PrintStream out) {
		out.print(mask.get() * parameter.get() + "\t");
	}

	@Override
	public void close(PrintStream out) {
		// nothing to do
	}

}
