---
id: DD-api-shortener-shorten-service
artifact_type: detailed-design
title: "URL Shortener — ShortenService Detailed Design"
scope: "api/shortener"
parent_scope: "api"
parent_architecture: ARCH
status: active
date: "2026-04-23"
version: 1
derived_from:
  - REQ-001
  - REQ-002
  - REQ-003
governing_adrs:
  - ADR-001-use-redis-for-url-cache
---

## Overview

`ShortenService` is the application logic layer of the URL shortener leaf scope.
It validates inbound URLs, generates short codes, delegates storage to a
`UrlStore` adapter, and returns the code or the resolved URL.
The invariant is that the same long URL always receives the same short code
(idempotent creation).

## Public Interface

```yaml
name: shorten
signature: "shorten(url: string) -> ShortenResult"
description: "Validates the URL, looks up or generates a short code, persists the mapping, and returns the code."
preconditions:
  - "url is a non-empty string containing a valid HTTP or HTTPS URL per RFC 3986."
postconditions:
  on_success:
    - "Returns ShortenResult with code (≤10 chars alphanumeric) and the original url."
    - "The code-to-url mapping is durably stored in UrlStore."
  on_failure:
    - "No state mutation occurs."
    - "Caller receives InvalidUrlError."
errors:
  - error: InvalidUrlError
    raised_when: "url fails RFC 3986 HTTP/HTTPS validation"
    meaning: "The supplied URL is not a valid HTTP or HTTPS URL."
thread_safety: thread-safe
```

```yaml
name: resolve
signature: "resolve(code: string) -> string | null"
description: "Looks up the original URL for a short code; returns null when the code is not found."
preconditions:
  - "code is a non-empty alphanumeric string of at most 10 characters."
postconditions:
  on_success:
    - "Returns the original URL string registered for this code."
  on_failure:
    - "Returns null when the code is absent; no exception is raised for missing codes."
errors: []
thread_safety: thread-safe
```

## Data Structures

```yaml
name: ShortenResult
description: "Return value of shorten(), carrying the generated code and echoing the original URL."
fields:
  - name: code
    type: string
    invariant: "Length ≤ 10; alphanumeric characters only."
  - name: url
    type: string
    invariant: "Valid HTTP or HTTPS URL; identical to the value supplied in the shorten() call."
ownership: "Constructed by ShortenService.shorten(); caller-owned after return."
lifetime: "Request-scoped; discarded after the HTTP response is sent."
returned_semantics: "Value copy; caller may freely mutate without affecting stored state."
```

## Algorithms

Code generation: base-62 encode the lower 6 bytes of a SHA-256 hash of the
canonical URL string. The first collision check is performed against Redis
using `SET NX`; on collision (expected probability < 0.001% at 1 M codes)
the hash is salted with an incrementing nonce.

## State

`ShortenService` is stateless. All persistent state lives in the injected
`UrlStore` (Redis). No instance-level mutable fields.

## Error Handling

```yaml
error: InvalidUrlError
detection: "RFC 3986 validation of the url parameter before any Redis interaction."
containment: "ShortenService.shorten() — not propagated to redis-store."
recovery: fail-fast
caller_receives: "InvalidUrlError exception (or equivalent typed result)."
```

```yaml
error: StoreUnavailableError
detection: "Redis connection timeout or connection-refused from UrlStore.set()."
containment: "ShortenService — wraps the Redis adapter exception."
recovery: propagate
caller_receives: "StoreUnavailableError — caller (HTTP handler) maps to HTTP 503."
```
