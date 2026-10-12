## ASP.NET Core client (2026-10-12)

The active entry point is StartActivity → MainActivity. Customer and management screens now use the .NET /api/v1 contract. The older PocketBase login classes below remain migration references and are not used by the new launcher.

Customer workflows include discovery, filters, favorites, multi-service booking, provider/time selection, appointment history, cancellation and reviews. Account screens support email registration/login, profile/avatar editing, email/password replacement sessions and logout. Management includes dashboard, calendar/status changes, branches, services, resources, memberships, availability, service assignments, moderation and private customer evaluations. Platform screens cover businesses, categories, catalog and user access. Server membership checks remain authoritative. Persian, English and Arabic resources and dark mode are included.

Architecture: UI/NativeViewModel → BookingUseCases → BookingRepository → BookingRepositoryImpl → BeNobatApi (Ktor), wired by BookingModule through Koin. Sessions use Android Keystore encryption and are excluded from backup; legacy PocketBase tokens are not reused.

Debug defaults to http://10.0.2.2:8080. Change the server in Settings; USB devices can use adb reverse tcp:8080 tcp:8080 with http://127.0.0.1:8080. Debug HTTP is restricted to local hosts. Current API sign-in uses email.

Build: gradlew.bat :app:testUserDebugUnitTest :app:assembleUserDebug :app:assembleAdminDebug :app:lintUserDebug
APKs: app/build/outputs/apk/user/debug and app/build/outputs/apk/admin/debug.
Release is not debuggable; configure HTTPS with BENOBAT_API_BASE_URL and optional signing through BENOBAT_KEYSTORE_PATH, BENOBAT_KEYSTORE_PASSWORD, BENOBAT_KEY_ALIAS and BENOBAT_KEY_PASSWORD.

# Be Nobat

[فارسی](README.fa.md) · **English** · [العربية](README.ar.md)

Be Nobat is an Android appointment-booking application built with Kotlin. It currently includes a
PocketBase authentication flow that accepts an email address or Iranian mobile number.

## Highlights

- Clean separation between presentation, domain, repository, and remote data layers
- Dependency injection with Koin
- Networking with Ktor and Kotlin Serialization
- Secure local token storage
- English, Persian, and Arabic resources with automatic LTR/RTL layout direction
- In-app language selector on the login screen
- Refined, keyboard-friendly Material login experience with a custom BN monogram and launcher icon
- Safe, debug-only API request logging

## Development API

The debug build connects to PocketBase at `http://10.0.2.2:8090`. The authentication path is
`/api/collections/users/auth-with-password`. Replace the placeholder release URL in
`app/build.gradle.kts` before publishing.

## Build

```bash
./gradlew test
./gradlew assembleUserDebug
```

The project requires the Gradle version declared by `gradle/wrapper/gradle-wrapper.properties` and
an Android SDK compatible with the configured compile SDK.

## Language selection

Open the login screen and tap the language button at the top. Select English, فارسی, or العربية.
The selected application locale is managed by AppCompat and remains active when activities are
recreated. Persian is selected automatically on first launch; an existing user selection is never
overwritten.

## Code documentation

The bilingual [code documentation](docs/CODE_DOCUMENTATION.md) explains every production class and
explicit function in English and Persian. It must be updated in the same commit as future code
changes. For focused network and logging notes, see [docs/README.md](docs/README.md).


## Web parity update — 2026-10-12
Added independent scoped Team and Customers screens with search, branch filtering, pagination and links to membership/platform user management. Customer history reads /admin/appointments?customerId=... across all dates. People endpoints remain read-only; no unsupported create/delete calls are offered.
Added the full paginated public review screen with published dates, manager replies and a link to review eligible appointments. Management option lists now use BookingUseCases.all so records after the first 100 remain selectable. Missing ratings display a localized unrated label and sort below rated businesses.
Booking date defaults and the 90-day date picker horizon now use the branch time zone, matching server validation.
صفحهٔ اعضای تیم و مشتریان، سوابق کامل نوبت مشتری، همهٔ نظرات عمومی و گزینه‌های مدیریتی بیش از ۱۰۰ مورد اضافه شد. محدودهٔ تاریخ رزرو بر مبنای منطقهٔ زمانی شعبه اصلاح شد.

Management now also exposes completed customer appointments awaiting evaluation, through /admin/customer-reviews/pending with paging and the existing private evaluation form.


### Parity validation — 2026-10-12
The updated client passed 24 JVM tests and 3 instrumented tests on the connected physical phone against the disposable .NET API. Instrumented coverage includes guest route protection, real sign-in, public reviews, scoped management lists, customer appointment history and pending customer evaluations. Both userDebug and adminDebug APKs built; lintUserDebug reported 0 errors and 35 warnings. Existing server results remain 306 tests plus 70 booking/management and 28 account HTTP checks; no server code was changed in this parity update.
Build verification uses the isolated .gradle-user-codex cache with direct repository access; the local proxy timed out for Java. Dependency versions in the project were preserved.
اعتبارسنجی این تغییرات: ۲۴ تست واحد و ۳ تست رابط روی گوشی واقعی موفق شدند. هر دو APK مشتری و مدیریت ساخته شدند؛ lint بدون خطا و با ۳۵ هشدار پایان یافت. این نتایج پوشش سناریوهای فوق را تأیید می‌کند و به معنی آزمون دستی همهٔ امکانات وب نیست.
