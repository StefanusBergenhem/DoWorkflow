---
id: REQS
artifact_type: requirements
title: "URL Shortener — Root Requirements"
scope: ""
parent_scope: null
status: active
date: "2026-04-23"
version: 1
derived_from:
  - PB
governing_adrs:
  - ADR-001-use-redis-for-url-cache
---

## Requirements

```yaml
id: REQ-001
statement: "The system shall shorten any valid HTTP or HTTPS URL to a code of at most 10 lowercase alphanumeric characters."
rationale: "Short codes must fit in SMS messages and rendered Markdown without wrapping."
acceptance: "Given a valid HTTPS URL, when POST /shorten is called, then the response contains a code field whose value is 10 characters or fewer."
```

```yaml
id: REQ-002
statement: "The system shall resolve a short code to its original URL and redirect the caller within 50 ms at the p99 percentile under 500 concurrent requests."
rationale: "Redirect latency is user-visible; exceeding 50 ms degrades perceived performance of linked resources."
acceptance: "Given a previously stored short code, when GET /{code} is called, then a 302 response with the original URL in Location is returned within 50 ms at p99 under load."
```

```yaml
id: REQ-003
statement: "The system shall never resolve a short code to a URL other than the one supplied at creation time."
rationale: "Resolving to the wrong URL would silently corrupt links, which is a correctness invariant, not a performance concern."
acceptance: "Given a code created for URL A, when GET /{code} is called at any time after creation, then the Location header always contains URL A."
```
