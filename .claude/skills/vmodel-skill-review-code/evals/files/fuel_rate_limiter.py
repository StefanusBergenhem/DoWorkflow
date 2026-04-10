"""Fuel rate limiter — clamps fuel rate to mode-specific bounds with rate-of-change limiting."""

from dataclasses import dataclass
from enum import Enum, auto
from threading import Lock


class OperationalMode(Enum):
    STARTUP = auto()
    CRUISE = auto()
    EMERGENCY_SHUTDOWN = auto()


class ClampingReason(Enum):
    NONE = auto()
    MODE_MAX = auto()
    MODE_MIN = auto()
    RATE_OF_CHANGE = auto()
    EMERGENCY = auto()


@dataclass(frozen=True)
class FuelRateConfig:
    startup_min_rate: float = 10.0
    startup_max_rate: float = 50.0
    cruise_max_rate: float = 200.0
    max_rate_change: float = 100.0


@dataclass(frozen=True)
class FuelRateResult:
    actual_rate: float
    was_clamped: bool
    clamping_reason: ClampingReason


class FuelRateLimiter:
    """Clamps fuel rate to mode-specific bounds and enforces rate-of-change limits."""

    def __init__(self, config: FuelRateConfig | None = None):
        self._config = config or FuelRateConfig()
        self._previous_rate: float = 0.0
        self._lock = Lock()

    def limit(
        self,
        requested_rate: float,
        operational_mode: OperationalMode,
        elapsed_time_ms: int,
    ) -> FuelRateResult:
        with self._lock:
            return self._limit_locked(requested_rate, operational_mode, elapsed_time_ms)

    def _limit_locked(
        self,
        requested_rate: float,
        operational_mode: OperationalMode,
        elapsed_time_ms: int,
    ) -> FuelRateResult:
        # E1: negative rate treated as 0.0
        if requested_rate < 0.0:
            requested_rate = 0.0

        # E3: unrecognized mode treated as emergency shutdown
        if not isinstance(operational_mode, OperationalMode):
            operational_mode = OperationalMode.EMERGENCY_SHUTDOWN

        # E2: negative elapsed time — skip rate-of-change limiting
        skip_roc = elapsed_time_ms < 0

        result = self._apply_mode_limits(requested_rate, operational_mode)

        if not skip_roc and operational_mode == OperationalMode.CRUISE:
            result = self._apply_rate_of_change_limit(result, elapsed_time_ms)

        self._previous_rate = result.actual_rate
        return result

    def _apply_mode_limits(
        self, requested_rate: float, mode: OperationalMode
    ) -> FuelRateResult:
        """Apply mode-specific rate bounds."""
        cfg = self._config

        if mode == OperationalMode.EMERGENCY_SHUTDOWN:
            return FuelRateResult(0.0, True, ClampingReason.EMERGENCY)

        if mode == OperationalMode.STARTUP:
            if requested_rate < cfg.startup_min_rate:
                return FuelRateResult(cfg.startup_min_rate, True, ClampingReason.MODE_MIN)
            if requested_rate > cfg.startup_max_rate:
                return FuelRateResult(cfg.startup_max_rate, True, ClampingReason.MODE_MAX)

        if mode == OperationalMode.CRUISE:
            if requested_rate > cfg.cruise_max_rate:
                return FuelRateResult(cfg.cruise_max_rate, True, ClampingReason.MODE_MAX)

        return FuelRateResult(requested_rate, False, ClampingReason.NONE)

    def _apply_rate_of_change_limit(
        self, current_result: FuelRateResult, elapsed_time_ms: int
    ) -> FuelRateResult:
        """Enforce rate-of-change limit during cruise mode."""
        if current_result.was_clamped:
            return current_result

        max_delta = self._config.max_rate_change * elapsed_time_ms / 1000.0
        actual_delta = abs(current_result.actual_rate - self._previous_rate)

        if actual_delta <= max_delta:
            return current_result

        if current_result.actual_rate > self._previous_rate:
            clamped_rate = self._previous_rate + max_delta
        else:
            clamped_rate = self._previous_rate - max_delta

        return FuelRateResult(clamped_rate, True, ClampingReason.RATE_OF_CHANGE)
