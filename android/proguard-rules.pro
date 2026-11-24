# LibGDX
-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.ashley.** { *; }
-keep class com.badlogic.gdx.box2d.** { *; }
-keep class com.badlogic.gdx.freetype.** { *; }

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable and Serializable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep our game classes
-keep class com.animeshooter.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.reflect.** { *; }

# ProGuard optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose