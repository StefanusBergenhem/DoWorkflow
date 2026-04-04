/**
 * Reason why a fuel rate was clamped.
 *
 * <p>Communicates to downstream components why the requested rate was modified.
 */
public enum ClampingReason {
  NONE,
  MODE_MIN,
  MODE_MAX,
  RATE_OF_CHANGE,
  EMERGENCY
}
