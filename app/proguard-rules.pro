# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
# chatbit: protocol, crypto, identity, favorites and nostr used to be kept whole, which left the
# crypto readable like source in the APK. Only what Gson reads by reflection keeps its names now:
# JSON field names are part of the Nostr wire format and of the stored favorites.
-keep class com.bitchat.android.nostr.NostrEvent { <fields>; <init>(...); }
-keep class com.bitchat.android.nostr.NostrRequest$* { <fields>; }
-keep class com.bitchat.android.nostr.NostrFilter { <fields>; }
-keep class com.bitchat.android.favorites.FavoriteRelationshipData { <fields>; <init>(...); }
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# Keep SecureIdentityStateManager from being obfuscated to prevent reflection issues
-keep class com.bitchat.android.identity.SecureIdentityStateManager {
    private android.content.SharedPreferences prefs;
    *;
}


# Room loads generated database implementations by name and invokes their no-argument
# constructors reflectively. R8 full-mode can otherwise optimize away WorkDatabase_Impl's
# constructor, causing AndroidX Startup to crash before Application.onCreate.
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep Tor implementation (always included)
-keep class com.bitchat.android.net.RealTorProvider { *; }

# Arti (Custom Tor implementation in Rust) ProGuard rules
-keep class info.guardianproject.arti.** { *; }
-keep class org.torproject.arti.** { *; }
-keepnames class org.torproject.arti.**
-dontwarn info.guardianproject.arti.**
-dontwarn org.torproject.arti.**

# Fix for AbstractMethodError on API < 29 where LocationListener methods are abstract
-keepclassmembers class * implements android.location.LocationListener {
    public <methods>;
}

# chatbit obfuscation: every renamed class lands in one flat package (no com.bitchat.android.crypto
# tree left to browse), access modifiers are widened so more code can be merged and inlined.
-repackageclasses ''
-allowaccessmodification
# verbose/debug/info logs are removed from release builds (they describe the protocol step by
# step); warnings and errors stay for crash diagnosis
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
