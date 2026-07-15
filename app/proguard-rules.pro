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
