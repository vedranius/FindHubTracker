# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.findhubtracker.data.model.** { *; }

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
