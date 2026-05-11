package bmodeltest.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.Scalable;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.operator.ScaleOperator;
import beast.base.spec.inference.parameter.BoolVectorParam;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.util.Randomizer;


@Description("Scale operator that only considers the first 'count' elements of the parameter, "
		+ "for use with bModelTest where 'count' tracks how many gamma rate / invariant-site sub-models are active.")
public class BMTScaleOperator extends ScaleOperator {
	public Input<IntScalarParam<? extends NonNegativeInt>> countInput = new Input<>("count",
			"count parameter indicating the nr of elements to consider for scaling", Validate.REQUIRED);

	IntScalarParam<? extends NonNegativeInt> count;

	@Override
	public void initAndValidate() {
		count = countInput.get();
		super.initAndValidate();
	}

	@Override
	public double proposal() {
		try {
			final boolean scaleAll = scaleAllInput.get();
			final int specifiedDoF = degreesOfFreedomInput.get();
			final boolean scaleAllIndependently = scaleAllIndependentlyInput.get();

			final int dim = count.get();
			if (dim == 0) {
				// nothing active — reject so the move is a no-op
				return Double.NEGATIVE_INFINITY;
			}

			final Scalable param = parameterInput.get();

			if (param instanceof RealVectorParam<?> vec) {
				return proposeOnVector(vec, dim, scaleAll, scaleAllIndependently, specifiedDoF);
			}
			if (param instanceof RealScalarParam<?> scalar) {
				// scalar parameter: dim is by construction either 0 (handled above) or >0, so scale once
				final double scale = getScaler(0, scalar.get());
				scalar.scale(scale);
				if (!scalar.withinBounds(scalar.get())) {
					return Double.NEGATIVE_INFINITY;
				}
				return Math.log(scale);
			}
			throw new IllegalArgumentException("BMTScaleOperator only supports RealScalarParam or RealVectorParam, got "
					+ (param == null ? "null" : param.getClass()));
		} catch (Exception e) {
			return Double.NEGATIVE_INFINITY;
		}
	}

	private double proposeOnVector(RealVectorParam<?> param, int dim,
								   boolean scaleAll, boolean scaleAllIndependently, int specifiedDoF) {
		if (scaleAllIndependently) {
			double logHR = 0;
			for (int i = 0; i < dim; i++) {
				final double scaleOne = getScaler(i, param.get(i));
				final double newValue = scaleOne * param.get(i);

				logHR += Math.log(scaleOne);

				if (!param.isValid(newValue)) {
					return Double.NEGATIVE_INFINITY;
				}
				param.set(i, newValue);
			}
			return logHR;
		}
		if (scaleAll) {
			// all 'dim' values assumed independent — HR is df*log(scale), with df = dim-2 unless overridden
			final double scale = getScaler(0, param.get(0));
			final int df = (specifiedDoF > 0) ? specifiedDoF : dim - 2;
			final double logHR = df * Math.log(scale);
			for (int i = 0; i < dim; i++) {
				final double newValue = param.get(i) * scale;
				if (!param.isValid(newValue)) {
					return Double.NEGATIVE_INFINITY;
				}
				param.set(i, newValue);
			}
			return logHR;
		}

		// pick one position to scale, optionally constrained by an indicator vector
		final int index;
		final BoolVectorParam indicators = indicatorInput.get();
		if (indicators != null) {
			final int nDim = indicators.size();
			final boolean[] indicator = indicators.getValues();
			final boolean impliedOne = nDim == (dim - 1);

			final int[] loc = new int[nDim + 1];
			int nLoc = 0;
			if (impliedOne) {
				loc[nLoc++] = 0;
			}
			for (int i = 0; i < nDim && i < dim; i++) {
				if (indicator[i]) {
					loc[nLoc++] = i + (impliedOne ? 1 : 0);
				}
			}
			if (nLoc == 0) {
				return Double.NEGATIVE_INFINITY; // no active indicators
			}
			index = loc[Randomizer.nextInt(nLoc)];
		} else {
			index = Randomizer.nextInt(dim);
		}

		final double oldValue = param.get(index);
		if (oldValue == 0) {
			return Double.NEGATIVE_INFINITY;
		}

		final double scale = getScaler(index, oldValue);
		final double newValue = scale * oldValue;
		if (!param.isValid(newValue)) {
			return Double.NEGATIVE_INFINITY;
		}
		param.set(index, newValue);
		return Math.log(scale);
	}
}
