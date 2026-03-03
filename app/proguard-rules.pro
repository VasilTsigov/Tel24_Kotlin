# Keep all app classes
-keep class bg.iag.tel24.** { *; }

# Gson serialization
-keep class bg.iag.tel24.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.migration.Migration { *; }

# Kotlin coroutines
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * inflate(...);
    public static * bind(android.view.View);
}

# Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**
