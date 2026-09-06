# آتیران همراه (نمایشگر دیتابیس) — نسخه اندروید

اپلیکیشن اندرویدی **فقط-خواندنی** برای اتصال به سرویس «آتیران همراه»
(`AtiranLocalServices.svc` — WCF با `webHttpBinding` و JSON) و مشاهده داده‌های
پایگاه داده: مشتریان، کالاها و قیمت‌ها، موجودی انبارها، فاکتورها، چک‌ها، پیام‌ها و
گزارش‌ها. ثبت سفارش فروش/ویزیت در این نسخه وجود ندارد.

- زبان/UI: **Kotlin + Jetpack Compose** (Material 3، راست‌به‌چپ)
- HTTP: **OkHttp** + **kotlinx-serialization**
- ذخیره تنظیمات: **DataStore Preferences**
- حداقل اندروید: **API 24 (Android 7)**، target/compile SDK 35

## ساخت پروژه

```bash
cd AtiranHamrahViewer
gradle assembleDebug        # یا از Android Studio: Run ▶
```

خروجی: `app/build/outputs/apk/debug/app-debug.apk`

نیازمندی‌ها: JDK 17، Android SDK 35، Gradle 8.7+ (با AGP 8.6.1).

## ساخت خودکار با GitHub Actions

در ریشه مخزن فایل `.github/workflows/build-apk.yml` وجود دارد. هر push روی
شاخه کاری یا اجرای دستی `Build Atiran Hamrah Android APK` از تب Actions،
پروژه را روی یک Runner ابری می‌سازد و فایل `app-debug.apk` را به‌صورت
Artifact (فایل قابل دانلود از Actions) منتشر می‌کند.

پس از موفقیت ساخت، APK همان‌جا در روت مخزن به این مسیر هم کپی می‌شود:

```
AtiranHamrahViewer/dist/app-debug.apk
```

## راه‌اندازی روی سرور

پوشه منتشرشده سرویس (`LocalServices.svc` + `bin/`) باید روی وب‌سرور IIS
قابل دسترس باشد و آدرس آن (مثلاً `http://192.168.1.10/LocalServices.svc`)
در صفحه تنظیمات اپ وارد شود. چون روی `webHttpBinding` است، همه عملیات
با `POST` و بدنه JSON انجام می‌شوند؛ برای شبکه‌های داخلی HTTP ساده،
در `AndroidManifest.xml` مقدار `android:usesCleartextTraffic="true"` فعال است.

## فعال‌سازی دستگاه (CPUID)

سرویس، هر درخواست را با `SecurityToken {CPUID, Key}` اعتبارسنجی می‌کند:

- `Key = (YYYYMMDDHHMMSS) × 3593` (نمایش `long`)
- اختلاف زمان ورودی تا زمان سرور باید ≤ ۲۰۰ دقیقه باشد
- سپس رکورد دستگاه در جدول `Devices` بر اساس `CPUID` بررسی می‌شود
  (در غیر این صورت Status=2/3/4 برمی‌گردد)

این اپ کلید را **برای هر درخواست به‌صورت خودکار** از ساعت دستگاه تولید می‌کند
(`AtiranClient.freshToken()`). بنابراین:

1. ساعت دستگاه/تبلت باید با سرور هماهنگ باشد.
2. برای هر CPUID جدید، ابتدا باید تبلت در نسخه ویندوزی «آتیران همراه»
   فعال (Activate) شود؛ CPUID را از برنامه ویندوزی یا
   `GetDeviceInfo` دریافت کنید و در تنظیمات اپ وارد کنید.
3. اگر سرویس روی لوکال‌هوست/شبکه با آدرس IP تغییر کرده، فقط آدرس را عوض کنید.

## صفحه آغازین (سه‌بعدی)

- صفحه اول یک لندینگ تیره و شیشه‌ای با لوگوی خلاقانه **M** است:
  لوگوی M از سه میله گزارش چرخ‌شده + دکمه‌های سه‌بعدی با نور و سایه طراحی شده
  و ارتباط مستقیم با «گزارشات مدیریتی» دارد.
- اطلاعات اتصال از پیش تنظیم‌شده است (`37.143.147.19:1433`, `AdminAn`)
  و با لمس دکمه «ورود و مشاهده گزارشات» بدون تایپ، احراز هویت انجام می‌شود.

## صفحه‌ها

| بخش | سرویس‌های استفاده‌شده |
|---|---|
| تنظیمات + تست اتصال | `CompanyInfo`, `Login`, `GetCustomerByLogin` |
| ورود | `Login`, `GetCustomerByLogin` |
| مشتریان | `Customers`, (فیلتر محلی) |
| کالاها و قیمت‌ها | `Kalas`, `ForoshPrices` |
| موجودی انبارها | `KalaAnbs`, `InventoryAnbars` |
| فاکتورها | `CustomerFactors`, `NotPaidFactors` |
| چک‌ها | `Checks`, `CustomerChecks` |
| پیام‌ها | `VisitorMessages`, `GetUnreadMessages` |
| گزارش‌ها | `CompanyInfo`, `CountKa`, `CountMo`, `MaxShMo`, `GetPeriods`, `CustGroups`, `KaGroups` |
| **گزارشات مدیریتی + داشبورد** | `CompanyInfo`, `CountKa`, `CountMo`, `MaxShMo`, `GetPeriods`, `Checks`, `Customers`, `InventoryAnbars` |

بخش **گزارشات مدیریتی** شامل داشبورد مدیریت با کارت‌های KPI و نمودارهای رنگی
است: توزیع چک‌ها بر اساس بانک و وضعیت، موجودی انبارها، بدهی/اعتبار مشتریان و
دوره‌های سیستم.

برندینگ و طراحی این نسخه:
- **Meelano Studio Design**
- **Milad Yaghoobi**

همه این مسیرها و قالب‌های بدنه (Bare/Wrapped) دقیقاً از متادیتای
`AtiranLocalServices.dll` استخراج شده‌اند — مرجع کامل:
`docs/api-reference.json` و `docs/dto-reference.json` در ریشه مخزن.

## ساختار کد

```
app/src/main/java/ir/atiran/hamrah/viewer/
├── MainActivity.kt
├── data/
│   ├── Models.kt           # DTOهای سرویس (kotlinx-serialization)
│   ├── AtiranClient.kt     # OkHttp + JSON + ساخت کلید امنیتی
│   ├── AtiranRepository.kt # متدهای تایپ‌شده هر endpoint
│   └── SettingsStore.kt    # DataStore
└── ui/
    ├── AppViewModel.kt
    ├── AtiranApp.kt        # مسیریابی صفحه‌ها
    ├── theme/Theme.kt
    ├── screens/            # تنظیمات، ورود، خانه
    └── screens/browse/     # لیست‌ها + جستجو + جزئیات
```

## نکته‌ها

- پاسخ همه endpoint ها به شکل `AtiranResult {Status, Type, Result}` است؛
  `Result` یک **رشته JSON** است و اپ آن را باز می‌کند (`Status==1` یعنی موفق).
- بسیاری از endpoint ها صفحه‌بندی دارند (`startIndex/fetchNo`)؛ لیست‌ها
  به‌صورت اسکرول پیوسته صفحات بعدی را می‌گیرند.
- برای جستجوی فیلترشده سمت سرور (`FilteredCustomers`, `FilteredKalas` و…)
  کلاس `SetInfo` و فیلدهای `OwnerNum/InventoryNum/...` در تنظیمات هست.
