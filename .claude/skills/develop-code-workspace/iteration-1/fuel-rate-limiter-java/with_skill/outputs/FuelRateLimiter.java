/**
 * Clamps fuel rate requests to mode-specific bounds and enforces rate-of-change limits.
 *
 * <p>Applies fail-safe semantics: invalid inputs are treated as safe values (emergency shutdown,
 * zero rate). Thread-safe with thread-confined state. Executes in constant time.
 */
public final class FuelRateLimiter {

  // Configuration constants (liters/hour)
  private static final float STARTUP_MIN_RATE = 10.0f;
  private static final float STARTUP_MAX_RATE = 50.0f;
  private static final float CRUISE_MAX_RATE = 200.0f;
  private static final float MAX_RATE_CHANGE = 100.0f; // liters/hour/second
  private static final float ABSOLUTE_MAX_RATE = 500.0f;

  // Internal state: thread-confined (not volatile, no synchronization needed if called from
  // single control loop)
  private float previousRate;

  /**
   * Creates a fuel rate limiter with initial rate of 0.0.
   *
   * <p>Thread-safe to construct. If called from multiple threads, ensure external
   * synchronization.
   */
  public FuelRateLimiter() {
    this.previousRate = 0.0f;
  }

  /**
   * Applies fuel rate limits to a request.
   *
   * <p>Enforces mode-specific bounds, rate-of-change limits, and fail-safe error handling.
   * Updates internal state with the actual rate computed.
   *
   * @param request the fuel rate request with mode and elapsed time
   * @return the fuel rate result with actual rate, clamp flag, and reason
   * @throws NullPointerException if request is null
   */
  public FuelRateResult limit(FuelRateRequest request) {
    if (request == null) {
      throw new NullPointerException("request cannot be null");
    }

    float requestedRate = request.getRequestedRate();
    OperationalMode mode = request.getOperationalMode();
    int elapsedTimeMs = request.getElapsedTimeMs();

    // Normalize negative requested rate to 0 (fail-safe)
    if (requestedRate < 0.0f) {
      requestedRate = 0.0f;
    }

    // Normalize negative elapsed time to 0 (skip rate-of-change limiting)
    if (elapsedTimeMs < 0) {
      elapsedTimeMs = 0;
    }

    // Default to emergency shutdown on unrecognized mode (fail-safe)
    if (mode == null) {
      mode = OperationalMode.EMERGENCY_SHUTDOWN;
    }

    // Apply mode-specific limiting
    FuelRateResult result = applyModeLimiting(requestedRate, mode);

    // Apply rate-of-change limiting if not already clamped to emergency
    if (result.getClampingReason() != ClampingReason.EMERGENCY) {
      result = applyRateOfChangeLimiting(result, mode, elapsedTimeMs);
    }

    // Cap to absolute maximum
    if (result.getActualRate() > ABSOLUTE_MAX_RATE) {
      result =
          new FuelRateResult(ABSOLUTE_MAX_RATE, true, ClampingReason.MODE_MAX);
    }

    // Update state with the actual rate
    this.previousRate = result.getActualRate();

    return result;
  }

  /**
   * Applies mode-specific minimum and maximum rate bounds.
   *
   * @param requestedRate normalized requested rate
   * @param mode the operational mode
   * @return result with mode-specific clamping applied
   */
  private FuelRateResult applyModeLimiting(float requestedRate, OperationalMode mode) {
    switch (mode) {
      case STARTUP:
        return applyStartupLimiting(requestedRate);
      case CRUISE:
        return applyCruiseLimiting(requestedRate);
      case EMERGENCY_SHUTDOWN:
        return new FuelRateResult(0.0f, true, ClampingReason.EMERGENCY);
      default:
        // Unreachable, but defensive
        return new FuelRateResult(0.0f, true, ClampingReason.EMERGENCY);
    }
  }

  /**
   * Applies startup mode bounds: [STARTUP_MIN_RATE, STARTUP_MAX_RATE].
   *
   * @param requestedRate normalized requested rate
   * @return result with startup bounds applied
   */
  private FuelRateResult applyStartupLimiting(float requestedRate) {
    if (requestedRate < STARTUP_MIN_RATE) {
      return new FuelRateResult(STARTUP_MIN_RATE, true, ClampingReason.MODE_MIN);
    }
    if (requestedRate > STARTUP_MAX_RATE) {
      return new FuelRateResult(STARTUP_MAX_RATE, true, ClampingReason.MODE_MAX);
    }
    return new FuelRateResult(requestedRate, false, ClampingReason.NONE);
  }

  /**
   * Applies cruise mode bounds: [0.0, CRUISE_MAX_RATE].
   *
   * @param requestedRate normalized requested rate
   * @return result with cruise bounds applied
   */
  private FuelRateResult applyCruiseLimiting(float requestedRate) {
    if (requestedRate > CRUISE_MAX_RATE) {
      return new FuelRateResult(CRUISE_MAX_RATE, true, ClampingReason.MODE_MAX);
    }
    return new FuelRateResult(requestedRate, false, ClampingReason.NONE);
  }

  /**
   * Applies rate-of-change limiting in cruise mode.
   *
   * <p>Constrains the actual rate to: previous_rate ± (MAX_RATE_CHANGE * elapsed_time_ms / 1000)
   *
   * @param modeResult result from mode limiting (not yet rate-of-change limited)
   * @param mode the operational mode
   * @param elapsedTimeMs milliseconds since last call
   * @return result with rate-of-change clamping applied (if needed)
   */
  private FuelRateResult applyRateOfChangeLimiting(
      FuelRateResult modeResult, OperationalMode mode, int elapsedTimeMs) {
    // Only enforce rate-of-change limiting in cruise mode
    if (mode != OperationalMode.CRUISE) {
      return modeResult;
    }

    float requestedRate = modeResult.getActualRate();
    float maxChange = MAX_RATE_CHANGE * elapsedTimeMs / 1000.0f;
    float upper = previousRate + maxChange;
    float lower = Math.max(0.0f, previousRate - maxChange);

    if (requestedRate > upper) {
      return new FuelRateResult(upper, true, ClampingReason.RATE_OF_CHANGE);
    }
    if (requestedRate < lower) {
      return new FuelRateResult(lower, true, ClampingReason.RATE_OF_CHANGE);
    }

    return modeResult;
  }
}
