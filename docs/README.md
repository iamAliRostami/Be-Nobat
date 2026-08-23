# Be Nobat

## Network configuration

The application uses PocketBase through a single Ktor `HttpClient` managed by Koin. Set the
production endpoint in `app/build.gradle.kts` before producing a release build. Debug builds use
`http://10.0.2.2:8090`, which maps the Android emulator to PocketBase running on the host machine.

The authentication flow follows these boundaries:

1. `LoginUseCase` validates presentation input and calls the domain `AuthRepository` contract.
2. `AuthRepositoryImpl` delegates authentication to `PocketBaseClient` and persists the returned
   token only after a successful response.
3. `PocketBaseClient` owns HTTP request/response details, while Koin owns the client, JSON,
storage, repository, use case, and ViewModel lifecycles.

The login endpoint is assembled from `BuildConfig.POCKET_BASE_URL` and
`BuildConfig.AUTH_LOGIN_PATH`. The debug test URL is
`http://10.0.2.2:8090/api/collections/users/auth-with-password`; release builds use the placeholder
base URL `https://api.example.com`, which must be replaced before shipping.

Login accepts an email address or an Iranian mobile number. Supported mobile formats are normalized
to `+98` before being sent as PocketBase's `identity`. Authentication, rate-limit, timeout, server,
and network failures are translated to safe user-facing errors instead of exposing API responses.
