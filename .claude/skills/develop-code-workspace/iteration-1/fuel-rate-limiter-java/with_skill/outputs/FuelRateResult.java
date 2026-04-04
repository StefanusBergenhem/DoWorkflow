/**
 * Output from the fuel rate limiter.
 *
 * <p>Immutable value object containing the actual fuel rate after limiting, whether it was
 * clamped, and the reason for clamping.
 */
public final class FuelRateResult {
  private final float actualRate;
  private final boolean wasClamped;
  private final ClampingReason clampingReason;

  /**
   * Creates a fuel rate result.
   *
   * @param actualRate fuel rate after all limits applied (>= 0, <= 500)
   * @param wasClamped true if actual_rate differs from requested_rate
   * @param clampingReason why clamping was applied
   */
  public FuelRateResult(float actualRate, boolean wasClamped, ClampingReason clampingReason) {
    this.actualRate = actualRate;
    this.wasClamped = wasClamped;
    this.clampingReason = clampingReason;
  }

  public float getActualRate() {
    return actualRate;
  }

  public boolean wasClamped() {
    return wasClamped;
  }

  public ClampingReason getClampingReason() {
    return clampingReason;
  }
}
