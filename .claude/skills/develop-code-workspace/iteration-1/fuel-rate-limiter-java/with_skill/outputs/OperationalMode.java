/**
 * Operational mode of the engine.
 *
 * <p>Used by FuelRateLimiter to apply mode-specific bounds and behavior.
 */
public enum OperationalMode {
  STARTUP,
  CRUISE,
  EMERGENCY_SHUTDOWN
}
