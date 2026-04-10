"""Session management — handles creation, validation, and cleanup of user sessions."""

from dataclasses import dataclass, field
from enum import Enum, auto
from typing import Protocol


# --- Common data types ---

class SessionError(Enum):
    INVALID_TOKEN = auto()
    EXPIRED = auto()
    REVOKED = auto()
    STORE_FULL = auto()


@dataclass(frozen=True)
class SessionInfo:
    user_id: str
    roles: list[str]
    created_at: float  # timestamp
    expires_at: float  # timestamp


@dataclass(frozen=True)
class SessionResult:
    pass


@dataclass(frozen=True)
class SessionSuccess(SessionResult):
    info: SessionInfo


@dataclass(frozen=True)
class SessionFailure(SessionResult):
    error: SessionError


# --- Dependency interfaces ---

class Clock(Protocol):
    def now(self) -> float: ...


class TokenGenerator(Protocol):
    def generate(self) -> str: ...


class SessionRepository(Protocol):
    def get(self, token: str) -> SessionInfo | None: ...
    def put(self, token: str, info: SessionInfo) -> None: ...
    def delete(self, token: str) -> None: ...
    def size(self) -> int: ...
    def all_entries(self) -> list[tuple[str, SessionInfo]]: ...


# --- Default in-memory repository ---

class InMemorySessionRepository:
    def __init__(self):
        self._store: dict[str, SessionInfo] = {}

    def get(self, token: str) -> SessionInfo | None:
        return self._store.get(token)

    def put(self, token: str, info: SessionInfo) -> None:
        self._store[token] = info

    def delete(self, token: str) -> None:
        self._store.pop(token, None)

    def size(self) -> int:
        return len(self._store)

    def all_entries(self) -> list[tuple[str, SessionInfo]]:
        return list(self._store.items())


# --- Units ---

MAX_SESSIONS = 10_000
DEFAULT_TTL_SECONDS = 1800  # 30 minutes


class SessionStore:
    """Creates and stores sessions. Design unit: SessionStore."""

    def __init__(self, repository: SessionRepository, clock: Clock, token_generator: TokenGenerator):
        self._repository = repository
        self._clock = clock
        self._token_generator = token_generator

    def create(self, user_id: str, roles: list[str]) -> SessionResult:
        if self._repository.size() >= MAX_SESSIONS:
            return SessionFailure(SessionError.STORE_FULL)

        now = self._clock.now()
        token = self._token_generator.generate()
        info = SessionInfo(
            user_id=user_id,
            roles=roles,
            created_at=now,
            expires_at=now + DEFAULT_TTL_SECONDS,
        )
        self._repository.put(token, info)
        return SessionSuccess(info)

    # CROSS-CHECK DEFECT 1: Gold plating — this method is not in the design.
    # The design says SessionStore has create() only. This is extra functionality.
    def get_stats(self) -> dict:
        """Return session store statistics."""
        return {
            "total_sessions": self._repository.size(),
            "capacity": MAX_SESSIONS,
            "utilization": self._repository.size() / MAX_SESSIONS,
        }


class SessionValidator:
    """Validates session tokens. Design unit: SessionValidator."""

    def __init__(self, repository: SessionRepository, clock: Clock):
        self._repository = repository
        self._clock = clock

    def validate(self, token: str) -> SessionResult:
        info = self._repository.get(token)
        if info is None:
            return SessionFailure(SessionError.INVALID_TOKEN)

        if self._clock.now() > info.expires_at:
            return SessionFailure(SessionError.EXPIRED)

        return SessionSuccess(info)


# CROSS-CHECK DEFECT 2: Missing design element — SessionCleaner is specified in the
# design's unit inventory but is not implemented at all. The design says:
# "SessionCleaner: Iterates all sessions via SessionRepository, removes those expired
# per Clock. Must not block request-handling threads."
# This unit is completely absent from the code.
