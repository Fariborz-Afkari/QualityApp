# QualityApp

اپلیکیشن اندروید مدیریت چک‌لیست‌های کنترل کیفیت با Kotlin و Jetpack Compose.

## امکانات فعلی
- صفحه اصلی فارسی
- انتخاب چک‌لیست
- پاسخ‌دهی بله/خیر به پرسش‌ها
- صفحه پایان ارزیابی
- صفحه گزارش‌ها و تنظیمات اولیه

## اجرا در Android Studio
1. پوشه `QualityApp` را در Android Studio باز کنید.
2. JDK 17 و Android SDK Platform 35 را نصب کنید.
3. منتظر بمانید Gradle Sync کامل شود.
4. برنامه را روی شبیه‌ساز یا دستگاه اندرویدی اجرا کنید.

## مشخصات
- Minimum SDK: 24
- Target / Compile SDK: 35
- Kotlin 1.9.24
- Jetpack Compose + Material 3
- Navigation Compose

## نکته
گزارش‌ها در این نسخه هنوز به‌صورت پایدار ذخیره نمی‌شوند و صفحه گزارش‌ها نمایشی است. توکن یا رمز عبور GitHub را داخل سورس یا مخزن قرار ندهید.


## ساخت خودکار APK با GitHub Actions

فایل `.github/workflows/android-apk.yml` به پروژه اضافه شده است. پس از commit و push این فایل به مخزن GitHub، به زبانه **Actions** بروید و اجرای **Build Android APK** را باز کنید. پس از موفق شدن job، در بخش **Artifacts** فایل `QualityApp-debug-apk` را دانلود کنید. این artifact فایل ZIP است و APK داخل آن قرار دارد.

این workflow نسخهٔ debug را می‌سازد؛ برای انتشار عمومی در فروشگاه‌ها باید APK/AAB نسخهٔ release را با کلید امضای خودتان امضا کنید.
