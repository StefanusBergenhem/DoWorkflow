---
id: TS-api-shortener
artifact_type: test-spec
title: "URL Shortener — ShortenService Test Spec"
scope: "api/shortener"
parent_scope: "api"
level: unit
status: active
date: "2026-04-23"
version: 1
verifies:
  - DD-api-shortener-shorten-service
derived_from:
  - DD-api-shortener-shorten-service
governing_adrs:
  - ADR-001-use-redis-for-url-cache
---

## Test Cases

```yaml
id: TC-001
title: "shorten() returns a code of at most 10 alphanumeric characters for a valid URL"
suite: happy-path
type: functional
verifies:
  - DD-api-shortener-shorten-service
inputs:
  url: "https://example.com/some/very/long/path?query=value"
expected:
  - "ShortenResult.code length ≤ 10"
  - "ShortenResult.code matches [a-zA-Z0-9]+"
  - "ShortenResult.url equals the input URL"
```

```yaml
id: TC-002
title: "shorten() is idempotent: same URL always returns the same code"
suite: happy-path
type: property
verifies:
  - DD-api-shortener-shorten-service
inputs:
  url: "https://example.com/stable"
expected:
  - "Two sequential calls to shorten() with the same URL return ShortenResults with identical code values."
```

```yaml
id: TC-003
title: "shorten() raises InvalidUrlError for a non-HTTP URL"
suite: error-paths
type: error
verifies:
  - DD-api-shortener-shorten-service
inputs:
  url: "ftp://example.com/file"
expected:
  - "InvalidUrlError is raised."
  - "No write is issued to UrlStore."
```

```yaml
id: TC-004
title: "resolve() returns null for an unregistered code"
suite: error-paths
type: boundary
verifies:
  - DD-api-shortener-shorten-service
inputs:
  code: "zzzzzzzzzz"
expected:
  - "resolve() returns null."
  - "No exception is raised."
```

```yaml
id: TC-005
title: "shorten() propagates StoreUnavailableError when Redis is unreachable"
suite: error-paths
type: fault-injection
verifies:
  - DD-api-shortener-shorten-service
preconditions: "UrlStore is configured to raise a connection-refused error on every call."
inputs:
  url: "https://example.com/fault-test"
expected:
  - "StoreUnavailableError is raised."
  - "No partial state is stored."
```
