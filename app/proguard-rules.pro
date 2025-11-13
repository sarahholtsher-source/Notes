# Add project-specific ProGuard rules here.
# Configuration for ProGuard and R8 are applied using the `proguardFiles` setting in `build.gradle`.
#
# Reference for ProGuard: http://developer.android.com/guide/developing/tools/proguard.html

# For WebView with JavaScript interfaces:
# If your project uses WebView and has a JavaScript interface, uncomment the lines below
# and replace fqcn.of.javascript.interface.with.your.interface.class with the fully qualified class name.
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#     public *;
# }

# Preserve line number information for debugging stack traces.
# Uncomment the following line to keep the line number and source information (useful for debugging).
-keepattributes SourceFile,LineNumberTable

# If you keep line number information, uncomment this line to hide the original source file name.
# This will prevent revealing the source file names in obfuscated code.
-renamesourcefileattribute SourceFile

# Keep the entire package structure of your app (for better security).
# This will prevent your class names, method names, and variables from being obfuscated.
-keep class com.dharmabit.notes.** { *; }

# Keep essential libraries (like Gson, Retrofit, OkHttp, Room, etc.) intact to ensure they work as expected.
# Example: Keep Gson class names intact (if you're using Gson for JSON parsing).
-keep class com.google.gson.** { *; }

# Keep Retrofit and OkHttp network libraries intact
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit2.** { *; }

# Keep Room database components intact
-keep class androidx.room.** { *; }

# Keep Android ViewModel and LiveData intact
-keep class androidx.lifecycle.** { *; }

# Ensure that method names in public classes such as Activities, Fragments, and Services remain intact.
-keep public class com.dharmabit.notes.ui.** {
    public <methods>;
}

# If using annotations such as Dagger or Butterknife, ensure annotations remain intact.
# Dagger annotations are used for dependency injection and Butterknife for view binding.
-keep @interface butterknife.* { *; }
-keep class dagger.** { *; }

# Prevent obfuscation for enums
-keep class com.dharmabit.notes.model.** { *; }

# Ensure serialization works correctly for Serializable models.
# This keeps all the classes that implement Serializable intact.
-keep class com.dharmabit.notes.model.** implements java.io.Serializable { *; }

# Optimize and shrink resources to reduce app size and increase performance.
# Uncomment this if you want ProGuard to shrink resources automatically.
-shrinkresources

# Disable class minification for debugging
# -dontshrink

# This will obfuscate the code by renaming variables and classes to prevent reverse engineering.
# However, use this with caution in case you need to debug specific code.
#-obfuscate
