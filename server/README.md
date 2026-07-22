# WonderLearn Server

Ktor service that delivers vocabulary content to the app, so words can be added without a store release.

## Running

```
./gradlew :server:run
```

Listens on `PORT` (default 8080).

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/health` | Liveness plus the content version currently served |
| `GET` | `/v1/content/manifest` | Full manifest |
| `GET` | `/v1/content/manifest?since=N` | `304 Not Modified` when the client already has version `N` or newer |

## Content

Served from `src/main/resources/vocabulary.json` via `ContentStore`. Swapping the resource file for a database is a change to that interface alone.

## Client behaviour

The app resolves content through `FallbackContentSource`: it asks the server first and falls back to its bundled copy when the server is unreachable, returns nothing new, or fails to parse. The app therefore works fully offline and on first launch with no network.

The Android emulator reaches the host at `10.0.2.2`; cleartext is permitted for that host and `localhost` only, in `network_security_config.xml`.
