/**
 * Input to the fuel rate limiter.
 *
 * <p>Immutable value object containing the requested fuel rate, current operational mode, and
 * elapsed time since the last call.
 */
public final class FuelRateRequest {
  private final float requestedRate;
  private final OperationalMode operationalMode;
  private final int elapsedTimeMs;

  /**
   * Creates a fuel rate request.
   *
   * @param requestedRate desired fuel rate in liters/hour (>= 0)
   * @param operationalMode current engine mode
   * @param elapsedTimeMs milliseconds since last call (>= 0)
   */
  public FuelRateRequest(
      float requestedRate, OperationalMode operationalMode, int elapsedTimeMs) {
    this.requestedRate = requestedRate;
    this.operationalMode = operationalMode;
    this.elapsedTimeMs = elapsedTimeMs;
  }

  public float getRequestedRate() {
    return requestedRate;
  }

  public OperationalMode getOperationalMode() {
    return operationalMode;
  }

  public int getElapsedTimeMs() {
    return elapsedTimeMs;
  }
}
