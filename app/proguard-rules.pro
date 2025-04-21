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


# --- Rules added for TaskRabbit App ---

# Keep SQLCipher classes needed for encrypted Room
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Keep your Room database, DAOs, entities, and type converters
# Replace 'com.example.taskrabbit' if your package name is different
-keep class com.example.taskrabbit.data.AppDatabase { *; }
-keep class com.example.taskrabbit.data.TaskDao { *; }
-keep class com.example.taskrabbit.data.BackgroundDao { *; }
-keep class com.example.taskrabbit.data.TaskItem { *; }
-keep class com.example.taskrabbit.data.BackgroundImage { *; }
-keep class com.example.taskrabbit.data.Converters { *; }

# Keep your EncryptionHandler
# Replace 'com.example.taskrabbit' if your package name is different
-keep class com.example.taskrabbit.data.security.EncryptionHandler { *; }

# Add any other specific rules your app might need below...

# --- End TaskRabbit App Rules ---