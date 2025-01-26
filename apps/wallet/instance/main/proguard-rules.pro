-keepattributes *Annotation*
-keepclassmembers class com.ton_keeper.** {
    @org.jetbrains.annotations.** <fields>;
    @org.jetbrains.annotations.** <methods>;
}

-keep class io.tonapi.** { *; }

-keep class io.batteryapi.** { *; }

-keep class com.google.j2objc.annotations.** { *; }

# Keep enum values to ensure correct deserialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep methods annotated with Retrofit annotations
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Moshi generated adapter methods
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-keep class java.time.** { *; }

-keep class com.tonapps.tonkeeper.worker.** { *; }

-keep class com.tonapps.tonkeeper.manager.** { *; }

-keep class android.graphics.ColorSpace { *; }
-keep class org.koin.** { *; }
-keep class com.tonapps.tonkeeper.App { *; }

-keep class com.tonapps.wallet.api.holders.** { *; }

-keepnames class com.tonapps.tonkeeper.ui.screen.** { *; }



