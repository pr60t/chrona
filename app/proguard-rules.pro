# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/user/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.

# Keep Room SQLite schemas and entities
-keep class com.example.chrona.core.model.** { *; }
-keep class com.example.chrona.core.database.** { *; }

# Keep AppWidget receivers and services declared in AndroidManifest
-keep class com.example.chrona.widget.** { *; }

# Kotlin Coroutines and Sequences
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

