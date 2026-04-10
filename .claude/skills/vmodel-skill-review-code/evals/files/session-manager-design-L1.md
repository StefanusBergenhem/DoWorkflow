---
artifact_id: CD-003
artifact_type: detailed-design
version: 2.0.0
status: approved
component: session-management
layer: 1
trace_from: [ARCH-021, ARCH-022]
---

# Session Management

## Purpose

Manages user sessions for an HTTP API server. Handles session creation on login, validation on each request, and cleanup of expired sessions. Without this component, the API server would have no way to track authenticated users between requests, forcing re-authentication on every call.

## External Interfaces

| Direction | Partner Component | Mechanism | Data | Description |
|-----------|-------------------|-----------|------|-------------|
| Receives | auth-controller | function call | Credentials | Login request with username and token |
| Provides | auth-controller | return value | SessionToken | Opaque session token string |
| Receives | request-router | function call | SessionToken | Token from request header for validation |
| Provides | request-router | return value | SessionInfo | User id, roles, expiry time — or rejection |
| Receives | scheduler | callback | — | Periodic trigger (every 60s) to clean expired sessions |

## Dependencies

| Dependency | Type | Description |
|------------|------|-------------|
| Clock | infrastructure | Provides current time. Used by SessionValidator for expiry checks and by SessionStore for setting created_at/expires_at. Must be injectable for testing. |
| TokenGenerator | infrastructure | Produces cryptographically random 32-character hex strings. Used by SessionStore when creating new sessions. |
| SessionRepository | infrastructure | Key-value storage interface. Default implementation: in-memory ConcurrentHashMap. Interface: get(token) -> SessionInfo?, put(token, info), delete(token), size() -> int, all_entries() -> list. |

## Unit Inventory

| Unit ID | Layer | Brief Description | Dependencies |
|---------|-------|-------------------|--------------|
| SessionStore | 1 | Creates and stores sessions. Delegates persistence to SessionRepository, uses Clock for timestamps and TokenGenerator for token creation. | SessionRepository, Clock, TokenGenerator |
| SessionValidator | 1 | Checks if a session token is valid, not expired, and not revoked. Reads from SessionRepository, compares expiry against Clock. | SessionRepository, Clock |
| SessionCleaner | 1 | Iterates all sessions via SessionRepository, removes those expired per Clock. Must not block request-handling threads. | SessionRepository, Clock |

## Shared Patterns

### Error Strategy

All units return result objects rather than throwing exceptions. Callers check the result type (success or specific error variant). No session operation should ever crash the server — all failures are contained and reported through return values.

Error variants: `invalid_token` (not found), `expired` (past TTL), `revoked` (explicitly invalidated), `store_full` (at capacity limit).

### Threading Model

The SessionRepository is accessed from multiple request-handling threads concurrently. All access must be thread-safe. The cleaner runs on a separate timer thread and must not block request-handling threads for more than 1ms.

### Common Data Types

- `SessionToken`: opaque string, 32 hex characters
- `SessionInfo`: contains user_id (string), roles (list of strings), created_at (timestamp), expires_at (timestamp)
- `SessionResult`: tagged union — Success(SessionInfo) | Error(SessionError)
- `SessionError`: enum with values invalid_token, expired, revoked, store_full

## Constraints

- Maximum 10,000 concurrent sessions
- Session creation must complete in under 5ms
- Session validation must complete in under 1ms
- Default session TTL: 30 minutes
- Token generation must be cryptographically random
- Cleaner must process all entries without holding a lock for more than 1ms at a time
