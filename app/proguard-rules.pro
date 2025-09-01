-keep class android.** { *; }
-keep interface android.** { *; }
-dontwarn java.lang.**
-dontusemixedcaseclassnames
-keep class com.coara.htmlview.MainActivity {
    public static void main(java.lang.String[]);
}
-keepattributes *Annotation*
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
-adaptresourcefilecontents **.xml
-adaptresourcefilenames **.png
-classobfuscationdictionary obfuscation-dictionary.txt
-renamesourcefileattribute SourceFile
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, EnclosingMethod, Record, PermittedSubclasses, NestHost, NestMembers, Module, ModuleMainClass
-optimizationpasses 20
-mergeinterfacesaggressively
-adaptclassstrings
-repackageclasses ''

-keepclassmembers class * {
    void addTextChangedListener(android.text.TextWatcher);
}
-keepclassmembers class * {
    public void requestPermissions(androidx.core.app.ActivityCompat, java.lang.String[], int);
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}
-keep class **$$Lambda$* { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class * {
    public void set*(...);
    public void get*(...);
}
-allowaccessmodification
-dontpreverify
