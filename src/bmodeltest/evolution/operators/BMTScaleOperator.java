package bmodeltest.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.parameter.BooleanParameter;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.operator.ScaleOperator;
import beast.base.util.Randomizer;


@Description("Operator for bModelTest to scale shape/proportiona invariant if gamma rate heterogeneity and/or invariant sites are present")
public class BMTScaleOperator extends ScaleOperator {
	public Input<IntegerParameter> countInput = new Input<IntegerParameter>("count","count parameter indicating the nr of rates to use", Validate.REQUIRED);

	IntegerParameter count;
	
	@Override
	public void initAndValidate() {
		count = countInput.get();
		if (treeInput.get() != null) {
			throw new IllegalArgumentException("A parameter (not a tree) should not be specified");
		}
		super.initAndValidate();
	}
	
	@Override
	public double proposal() {
    	try {

            double hastingsRatio;
            final double scale = getScaler();

            final boolean bScaleAll = scaleAllInput.get();
            final int nDegreesOfFreedom = degreesOfFreedomInput.get();
            final boolean bScaleAllIndependently = scaleAllIndependentlyInput.get();

            final RealParameter param = parameterInput.get();

            assert param.getLower() != null  && param.getUpper() != null;

            final int dim = (int) count.getArrayValue();//param.getDimension();

            if (bScaleAllIndependently) {
                // update all dimensions independently.
                hastingsRatio = 0;
                for (int i = 0; i < dim; i++) {

                    final double scaleOne = getScaler();
                    final double newValue = scaleOne * param.getValue(i);

                    hastingsRatio -= Math.log(scaleOne);

                    if ( outsideBounds(newValue, param) ) {
                        return Double.NEGATIVE_INFINITY;
                    }

                    param.setValue(i, newValue);
                }
            } else if (bScaleAll) {
                // update all dimensions
                // hasting ratio is dim-2 times of 1dim case. would be nice to have a reference here
                // for the proof. It is supposed to be somewhere in an Alexei/Nicholes article.
                final int df = (nDegreesOfFreedom > 0) ? -nDegreesOfFreedom  : dim - 2;
                hastingsRatio = df * Math.log(scale);

                // all Values assumed independent!
                for (int i = 0; i < dim; i++) {
                    final double newValue = param.getValue(i) * scale;

                    if ( outsideBounds(newValue, param) ) {
                        return Double.NEGATIVE_INFINITY;
                    }
                    param.setValue(i, newValue);
                }
            } else {
                hastingsRatio = -Math.log(scale);

                // which position to scale
                int index;
                final BooleanParameter indicators = indicatorInput.get();
                if ( indicators != null ) {
                    final int nDim = indicators.getDimension();
                    Boolean [] indicator = indicators.getValues();
                    final boolean impliedOne = nDim == (dim - 1);

                    // available bit locations. there can be hundreds of them. scan list only once.
                    int[] loc = new int[nDim + 1];
                    int nLoc = 0;

                    if (impliedOne) {
                        loc[nLoc] = 0;
                        ++nLoc;
                    }
                    for (int i = 0; i < nDim && i < dim; i++) {
                        if ( indicator[i] ) {
                            loc[nLoc] = i + (impliedOne ? 1 : 0);
                            ++nLoc;
                        }
                    }

                    if (nLoc > 0) {
                        final int rand = Randomizer.nextInt(nLoc);
                        index = loc[rand];
                    } else {
                        return Double.NEGATIVE_INFINITY; // no active indicators
                    }

                } else {
                    // any is good
                    index = Randomizer.nextInt(dim);
                }

                final double oldValue = param.getValue(index);

                if (oldValue == 0) {
                    // Error: parameter has value 0 and cannot be scaled
                    return Double.NEGATIVE_INFINITY;
                }

                final double newValue = scale * oldValue;

                if ( outsideBounds(newValue, param) ) {
                    // reject out of bounds scales
                    return Double.NEGATIVE_INFINITY;
                }

                param.setValue(index, newValue);
                // provides a hook for subclasses
                //cleanupOperation(newValue, oldValue);
            }
                       
            return hastingsRatio;

        } catch (Exception e) {
            // whatever went wrong, we want to abort this operation...
            return Double.NEGATIVE_INFINITY;
        }
	}

}
