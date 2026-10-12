## نسخهٔ متصل به ASP.NET Core (۲۰۲۶-۱۰-۱۲)

مسیر فعال برنامه اکنون StartActivity → MainActivity است. صفحه‌های مشتری و مدیریت از API دات‌نت در /api/v1 استفاده می‌کنند. کلاس‌های ورود PocketBase برای مرجع مهاجرت باقی مانده‌اند و در مسیر شروع جدید اجرا نمی‌شوند.

- مشتری: جست‌وجو و فیلتر کسب‌وکار، شعب و خدمات، علاقه‌مندی‌ها، رزرو چند خدمت و متخصص، انتخاب زمان، لغو نوبت، ثبت نظر و تاریخچه.
- حساب: ثبت‌نام و ورود ایمیلی، ویرایش پروفایل و تصویر، تغییر ایمیل و گذرواژه و ابطال نشست هنگام خروج.
- مدیریت: داشبورد، تقویم و وضعیت نوبت‌ها، شعب، خدمات، منابع، اعضا، ساعات دسترسی، تخصیص خدمات، بررسی نظرات و ارزیابی خصوصی مشتری.
- مدیر سامانه: کسب‌وکارها، دسته‌بندی‌ها، کاتالوگ و کاربران و دسترسی‌ها. دسترسی واقعی همیشه توسط سرور تعیین می‌شود.
- فارسی، انگلیسی و عربی، راست‌به‌چپ و حالت شب.

معماری: رابط و NativeViewModel → BookingUseCases → BookingRepository → BookingRepositoryImpl → BeNobatApi (Ktor). وابستگی‌ها در BookingModule با Koin ثبت می‌شوند. نشست API به‌صورت رمز‌شده با کلید Android Keystore نگهداری می‌شود؛ توکن‌های قدیمی PocketBase به سرور جدید ارسال نمی‌شوند.

آدرس پیش‌فرض Debug برابر http://10.0.2.2:8080 است و در تنظیمات برنامه قابل تغییر است. روی دستگاه USB از adb reverse tcp:8080 tcp:8080 و http://127.0.0.1:8080 استفاده کنید. HTTP فقط برای میزبان‌های محلی در Debug مجاز است. ورود API فعلی با ایمیل است.

ساخت و تست: gradlew.bat :app:testUserDebugUnitTest :app:assembleUserDebug :app:assembleAdminDebug :app:lintUserDebug
خروجی‌ها در app/build/outputs/apk/user/debug و app/build/outputs/apk/admin/debug قرار می‌گیرند.
Release قابل دیباگ نیست. آدرس HTTPS را با BENOBAT_API_BASE_URL و امضا را با BENOBAT_KEYSTORE_PATH، BENOBAT_KEYSTORE_PASSWORD، BENOBAT_KEY_ALIAS و BENOBAT_KEY_PASSWORD تنظیم کنید. رمز امضا در کد قرار نمی‌گیرد.

# به نوبت

رابط ورود بازطراحی‌شده، سازگار با صفحه‌کلید و راست‌به‌چپ، همراه با مونوگرام اختصاصی BN و آیکن جدید برنامه است.

**فارسی** · [English](README.md) · [العربية](README.ar.md)

«به نوبت» یک اپلیکیشن اندرویدی رزرو نوبت است که با Kotlin توسعه داده شده است. در نسخه فعلی، ورود
از طریق ایمیل یا شماره موبایل ایرانی و سرویس PocketBase انجام می‌شود.

## امکانات اصلی

- جداسازی لایه‌های نمایش، دامنه، مخزن و منبع داده شبکه
- تزریق وابستگی با Koin
- ارتباط شبکه با Ktor و Kotlin Serialization
- ذخیره امن توکن ورود
- پشتیبانی از فارسی، انگلیسی و عربی با چیدمان خودکار راست‌به‌چپ و چپ‌به‌راست
- امکان تغییر زبان در صفحه ورود
- ثبت امن لاگ درخواست‌های API فقط در نسخه Debug

## API محیط توسعه

نسخه Debug به PocketBase با آدرس `http://10.0.2.2:8090` متصل می‌شود. مسیر ورود
`/api/collections/users/auth-with-password` است. پیش از انتشار، آدرس آزمایشی نسخه Release را در
فایل `app/build.gradle.kts` با آدرس واقعی سرور جایگزین کنید.

## ساخت و آزمایش

```bash
./gradlew test
./gradlew assembleUserDebug
```

نسخه Gradle باید با مقدار تعریف‌شده در `gradle/wrapper/gradle-wrapper.properties` یکسان باشد و
Android SDK سازگار با compile SDK پروژه نیز نصب شده باشد.

## تغییر زبان

در صفحه ورود، دکمه زبان بالای صفحه را لمس و یکی از گزینه‌های English، فارسی یا العربية را انتخاب
کنید. در اولین اجرای برنامه، فارسی به‌صورت خودکار زبان پیش‌فرض است. زبان انتخاب‌شده توسط AppCompat
ذخیره می‌شود و در اجراهای بعدی با مقدار پیش‌فرض بازنویسی نخواهد شد.

## مستندات کد

[مستندات دوزبانه کد](docs/CODE_DOCUMENTATION.md) عملکرد تمام کلاس‌ها و توابع صریح کد اصلی را به
فارسی و انگلیسی توضیح می‌دهد. از این پس این سند باید همراه هر تغییر کد و در همان کامیت به‌روزرسانی
شود. توضیحات تخصصی شبکه و لاگ‌گیری نیز در [docs/README.md](docs/README.md) قرار دارد.


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
