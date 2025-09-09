# 保留 Kotlin 相关的重要元数据
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, InnerClasses, Signature, Exceptions
-dontskipnonpubliclibraryclasses

-ignorewarnings
-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-keepclasseswithmembernames class * { native <methods>; }
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }
-dontwarn  org.h2.**
-keep  class org.h2.** { *; }
-dontwarn  androidx.navigation.**
-keep  class androidx.navigation.** { *; }
-dontwarn  org.apache.commons.compress.**
-keep  class org.apache.commons.compress.** { *; }
-dontwarn  ch.qos.logback.**
-keep  class ch.qos.logback.** { *; }
-dontwarn  com.jetbrains.cef.**
-keep  class com.jetbrains.cef.** { *; }
-dontwarn  org.flywaydb.**
-keep  class org.flywaydb.** { *; }
-dontwarn  okhttp3.internal.**
-keep  class okhttp3.internal.** { *; }
-dontwarn  org.cef.**
-keep  class org.cef.** { *; }
-dontwarn  jogamp.newt.swt.**
-keep  class jogamp.newt.swt.** { *; }
-dontwarn  io.netty.**
-keep  class io.netty.** { *; }
-dontwarn  com.zaxxer.hikari.**
-keep  class com.zaxxer.hikari.** { *; }
-dontwarn  com.jogamp.**
-keep  class com.jogamp.** { *; }


-keep class kotlinx.coroutines.** {*;}
-keep class org.jetbrains.skia.** {*;}
-keep class org.jetbrains.skiko.** {*;}
-keepclassmembernames class kotlinx.** {
volatile <fields>;
}

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-keep class org.ocpsoft.prettytime.i18n**

-dontnote okhttp3.**


-keep class org.eclipse.swt.** { *; }
# Keep all classes and members in com.jogamp.opengl and its subpackages
-keep class com.jogamp.opengl.** { *; }

# Keep all classes and members in com.jogamp.nativewindow and its subpackages
-keep class com.jogamp.nativewindow.** { *; }

# If you are using GlueGen, you might need to keep its classes as well
-keep class com.jogamp.gluegen.** { *; }

# Specific rules for potential reflection or native method interactions
# These might be needed depending on the specific JOGL features you use
-keepclasseswithmembers class * {
    native <methods>;
}

# 保留 GLCanvas 及其成员
-keep class com.jogamp.opengl.swt.GLCanvas {
    *;
}
# 保留 Component 及其成员
-keep class java.awt.Component {
    *;
}
# 保留它们的公共超类（如果有明确的超类，也可指定）
-keep class org.eclipse.swt.widgets.Canvas {
    *;
}

# 保留 jogamp 相关包
-keep class com.jogamp.opengl.swt.** {
    *;
}
# 保留 SWT 相关包
-keep class org.eclipse.swt.widgets.** {
    *;
}
# 保留 AWT 相关包
-keep class java.awt.** {
    *;
}
# 忽略关于这些类的警告
-dontwarn com.jogamp.opengl.swt.**
-dontwarn org.eclipse.swt.widgets.**
# 关闭优化
-dontoptimize

-keep class org.jetbrains.exposed.**
-keep class uk.co.caprica.vlcj.**
-keep class com.sun.jna.**

-keep class com.sun.jna.** { *; }
-keep class com.sun.jna.platform.** { *; }
-keep class * implements com.sun.jna.Library { *; }
-keep class * implements com.sun.jna.Callback { *; }
-keepclasseswithmembers class * {
    native <methods>;
}

-keep class uk.co.caprica.vlcj.** { *; }


# 保留 vlcj 的接口和回调
-keep interface uk.co.caprica.vlcj.** { *; }
-keep class * implements uk.co.caprica.vlcj.** { *; }

# 保留 vlcj 的静态方法
-keep,allowobfuscation class uk.co.caprica.vlcj.** {
    public static void main(java.lang.String[]);
    public static ** valueOf(java.lang.String);
    public static ** valueOf(int);
    public static ** of(...);
    public static ** create(...);
}

#-addconfigurationdebugging