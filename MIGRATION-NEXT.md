# bModelTest BEAST 3 strong-typing migration — what's left

The BEAST 3 strong-typing migration on the `beast3-migration` branch
covers parameter inputs and substitution-model parents. The remaining
work is the SiteModel/Prior layer, which is interlocked and warrants
its own focused PR.

## Still on legacy parents

| Class | Parent (legacy) | What it forces to legacy |
|---|---|---|
| `BEASTModelTestSiteModel` | `beast.base.evolution.sitemodel.SiteModel` | `gammaShape`, `proportionInvariant` are `RealParameter` |
| `BMTScaleOperator` | `beast.base.evolution.operator.ScaleOperator` | the `parameter` it scales is whatever the legacy `ScaleOperator.parameterInput` accepts (`RealParameter`) |
| `BMTPrior` (when applied to `gammaShape` / `proportionInvariant`) | `beast.base.inference.distribution.Prior` (deprecated) | `m_x` is `Function`, so it can take legacy `RealParameter` but not spec types |

## Order to migrate

1. **`BEASTModelTestSiteModel` → spec `SiteModel`.**
   - Spec parent's `shapeParameterInput` is `Input<RealScalar<PositiveReal>>` and
     `invarParameterInput` is `Input<RealScalar<UnitInterval>>`.
   - `shape.getValue()` / `invar.getValue()` → `.get()`.
   - The current code reads `shapeParameter.isEstimatedInput.get()` directly.
     `RealScalar` is an interface and doesn't expose `isEstimatedInput`; spec
     `SiteModel` itself uses
     `instanceof RealScalarParam<PositiveReal> shape` and reads `shape.getLower()`.
     Mirror that pattern.

2. **Lift the remaining `BMTPrior` uses off legacy `Prior`.**
   `BMTPrior.x` is `Function` (inherited from legacy `Prior`). Once
   `gammaShape` / `proportionInvariant` are `RealScalar<…>` (spec, not
   `Function`), `BMTPrior` needs to extend `Distribution` directly with a
   typed `x` input — same shape as the `ModelSetPrior` and
   `NucleotideRevJumpSubstModelRatePrior` refactors already on this branch.

3. **`BMTScaleOperator` → spec `ScaleOperator`.**
   This is a behavioural rewrite, not just an input retype. Spec
   `ScaleOperator.parameterInput` is `Input<Scalable>` (a `RealScalarParam`
   or `RealVectorParam`) and the parent's `proposal()` does its own scaling
   logic with a Bactrian kernel. The current `BMTScaleOperator.proposal()`
   reimplements scaling against `count` (only the first `count` elements
   are scaled). Decide whether the new spec class still needs the
   `count`-aware variant, and if so, override `proposal()` against the spec
   API (`get/set/size/isValid`).

4. **Update example XMLs and the BEAUti template** to declare
   `gammaShape` / `proportionInvariant` as `RealScalarParam` with the
   appropriate domains, and replace the `<prior name='distribution' x='@…'>`
   wrappers around them with direct spec distributions (per the migration
   guide section "Replace priors with direct spec distributions").

5. **Update tests** that construct `RealParameter` for shape/invar, the
   way `NucleotideRevJumpSubstModelTest` was already updated for rates.

## References

- Migration guide: `~/Git/beast3/scripts/migration-guide.md`
- Spec README: `~/Git/beast3/beast-base/src/main/resources/beast/base/spec/README.md`
- Gold-standard migrated packages: `~/Git/morph-models/`, `~/Git/sampled-ancestors/`
- Spec `SiteModel` source for the pattern to mirror:
  `~/Git/beast3/beast-base/src/main/java/beast/base/spec/evolution/sitemodel/SiteModel.java`
