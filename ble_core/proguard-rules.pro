# Save the obfuscation mapping to a file, so we can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-printmapping out.map
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

# Preserve all annotations.

-keepattributes *Annotation*

# Preserve all .class method names.

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Preserve all public classes, and their public and protected fields and
# methods.

#-keep public class * {
#    public protected *;
#}

# Kotlin specifics

#-dontwarn kotlin.**
#-keep class kotlin.** {
#    public protected *;
#}

-keep enum ** {
    public protected *;
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

############## PROJECT SPECIFIC ##############
# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface

# scanner
-keep public class hr.sil.android.ble.scanner.legacy.BLEDeviceScanner { public *; }
-keep public class hr.sil.android.ble.scanner.legacy.BLEDeviceScanner$* { public *; }
-keep public class hr.sil.android.ble.scanner.BLERegionMonitor { public *; }

# exceptions
-keepclasseswithmembers public class * extends java.lang.Exception { public protected *; }
-keepclasseswithmembers public class * extends java.lang.Throwable { public protected *; }

# model
-keep public enum hr.sil.android.ble.scanner.model.** { public *; }
-keep public class hr.sil.android.ble.scanner.model.** { public *; }
-keep public interface hr.sil.android.ble.scanner.model.** { public protected *; }

# parser
-keep public interface hr.sil.android.ble.scanner.parser.** { public protected *; }

# rssi and distance
-keep public class hr.sil.android.ble.scanner.rssi.** { public *; }
-keep public interface hr.sil.android.ble.scanner.rssi.** { public protected *; }

# util
-keep public class hr.sil.android.ble.scanner.util.** { public protected *; }
-keep public interface hr.sil.android.ble.scanner.util.** { public protected *; }

# waker
-keep public class hr.sil.android.ble.scanner.waker.** { public protected *; }