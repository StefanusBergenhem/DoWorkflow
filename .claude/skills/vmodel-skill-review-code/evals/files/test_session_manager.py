"""Tests for session management — derived from design CD-003."""

import pytest
from session_manager import (
    InMemorySessionRepository,
    SessionError,
    SessionFailure,
    SessionInfo,
    SessionStore,
    SessionSuccess,
    SessionValidator,
    MAX_SESSIONS,
)


# --- Test doubles ---

class FakeClock:
    """Controllable clock for testing time-dependent behavior."""
    def __init__(self, initial_time: float = 1000.0):
        self._time = initial_time

    def now(self) -> float:
        return self._time

    def advance(self, seconds: float) -> None:
        self._time += seconds


class FakeTokenGenerator:
    """Deterministic token generator for predictable test assertions."""
    def __init__(self):
        self._counter = 0

    def generate(self) -> str:
        self._counter += 1
        return f"token-{self._counter:032d}"


# --- Fixtures ---

@pytest.fixture
def clock():
    return FakeClock()


@pytest.fixture
def token_gen():
    return FakeTokenGenerator()


@pytest.fixture
def repository():
    return InMemorySessionRepository()


@pytest.fixture
def store(repository, clock, token_gen):
    return SessionStore(repository, clock, token_gen)


@pytest.fixture
def validator(repository, clock):
    return SessionValidator(repository, clock)


# --- SessionStore: create ---

def test_create_session_returns_success_with_session_info(store, clock):
    result = store.create("user-1", ["admin", "reader"])
    assert isinstance(result, SessionSuccess)
    assert result.info.user_id == "user-1"
    assert result.info.roles == ["admin", "reader"]
    assert result.info.created_at == 1000.0
    assert result.info.expires_at == 2800.0  # 1000 + 1800


def test_create_session_stores_in_repository(store, repository):
    store.create("user-1", ["reader"])
    assert repository.size() == 1


def test_create_at_capacity_returns_store_full(store, repository, clock, token_gen):
    # Fill to capacity
    for i in range(MAX_SESSIONS):
        repository.put(f"existing-{i}", SessionInfo(
            user_id=f"user-{i}", roles=[], created_at=0, expires_at=9999,
        ))

    result = store.create("overflow-user", [])
    assert isinstance(result, SessionFailure)
    assert result.error == SessionError.STORE_FULL


# --- SessionValidator: validate ---

def test_validate_existing_session_returns_success(store, validator):
    store.create("user-1", ["admin"])
    result = validator.validate("token-00000000000000000000000000000001")
    assert isinstance(result, SessionSuccess)
    assert result.info.user_id == "user-1"


def test_validate_unknown_token_returns_invalid(validator):
    result = validator.validate("nonexistent-token")
    assert isinstance(result, SessionFailure)
    assert result.error == SessionError.INVALID_TOKEN


# CROSS-CHECK DEFECT 3: Missing test for TTL expiry behavior.
# The design says sessions expire after 30 minutes. The test suite should advance
# the fake clock past TTL and verify that validation returns 'expired'.
# This is a critical design behavior with no test coverage.


# --- Boundary: capacity ---

def test_create_at_exactly_max_capacity_minus_one_succeeds(store, repository, clock, token_gen):
    for i in range(MAX_SESSIONS - 1):
        repository.put(f"existing-{i}", SessionInfo(
            user_id=f"user-{i}", roles=[], created_at=0, expires_at=9999,
        ))
    result = store.create("last-user", [])
    assert isinstance(result, SessionSuccess)


# --- CROSS-CHECK DEFECT 4: Orphan test — tests get_stats() which is not in the design.
# This test verifies functionality that the design never specified. It's gold plating
# in the code, and this test covers that gold plating — making both orphans.

def test_get_stats_returns_utilization(store):
    store.create("user-1", [])
    store.create("user-2", [])
    stats = store.get_stats()
    assert stats["total_sessions"] == 2
    assert stats["capacity"] == MAX_SESSIONS
    assert stats["utilization"] == 2 / MAX_SESSIONS
