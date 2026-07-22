# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.mtier.timetracker.**$$serializer { *; }
-keepclassmembers class org.mtier.timetracker.** {
    *** Companion;
}
-keepclasseswithmembers class org.mtier.timetracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# androidx.security.crypto pulls in Tink, which references errorprone's
# build-time-only annotations. They're never present at runtime and never
# actually invoked, so R8 just needs to stop warning about them.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
