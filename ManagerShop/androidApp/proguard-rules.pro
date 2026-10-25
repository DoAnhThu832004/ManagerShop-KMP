# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve line number information for Sentry stacktraces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.SerialName <fields>;
}
-keep class com.thupo.managershop.data.remote.dto.** { *; }
-keepclassmembers class com.thupo.managershop.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Ktor & OkHttp ---
-dontwarn io.ktor.**
-dontwarn okio.**
-dontwarn okhttp3.**
-keep class io.ktor.** { *; }

# --- Koin DI ---
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# --- Room & SQLite ---
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class androidx.sqlite.** { *; }

# --- Sentry KMP ---
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# --- Coil 3 ---
-keep class coil3.** { *; }
-dontwarn coil3.**
