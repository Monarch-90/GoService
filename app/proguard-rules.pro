# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.avetiso.core.models.ServiceSnapshot { *; }

# Говорим компилятору: "Эти методы ни на что не влияют. Если их результат нигде не сохраняется, удали их из кода".
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
}

# Защищаем классы, которые используются в Navigation Graph через reflection
-keepnames class com.avetiso.core.entity.** { *; }

# Глобальная защита для всех Parcelable классов (рекомендация Google)
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
