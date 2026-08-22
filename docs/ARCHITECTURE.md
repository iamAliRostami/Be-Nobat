# Architecture

The application follows a pragmatic Clean Architecture split inside the Android module:

- `domain`: framework-independent models, repository contracts, and use cases.
- `data`: Android/network implementations, persistence, DTOs, and mapping to domain models.
- `ui`: activities and view models. UI code calls use cases or domain contracts instead of
  constructing data implementations.
- `di`: the composition root. Koin is the only place where implementations are bound to
  domain contracts and where network and storage dependencies are configured.

## Dependency rules

1. `domain` must not import Android, Ktor, Koin, or data-layer classes.
2. `data` may depend on `domain`, but DTOs must not leak into domain APIs.
3. `ui` must not instantiate repositories, clients, or view models manually.
4. Authentication tokens are accessed through `SessionRepository`; their encrypted DataStore
   representation remains an implementation detail.
5. API clients receive their `HttpClient`, endpoint, and token provider through constructor
   injection so they can be replaced in tests.

## API boundaries

`IAppApi`/`IApiService` remain domain contracts for application API operations. `AppApiImpl`
implements them with `PocketBaseClient`. `INetworkManager` is retained as a compatibility
facade, but deliberately exposes only `IAppApi`: Ktor's `HttpClient` is created and wired in
the DI layer and is never part of a domain signature.
