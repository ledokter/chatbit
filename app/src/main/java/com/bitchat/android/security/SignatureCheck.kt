package com.bitchat.android.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.bitchat.android.BuildConfig
import java.security.MessageDigest

/**
 * Release builds compare their own signing certificate with the one pinned at build time
 * (gradle.properties): a repackaged APK (malware added, then re-signed by someone else) gets a
 * warning instead of passing for chatbit. A determined attacker can strip this check; it protects
 * people who were handed a modified copy, not against the attacker.
 */
object SignatureCheck {
    /** True when this copy is signed by the pinned key, or when nothing is pinned (debug, forks). */
    fun isGenuine(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true
        val pinned = BuildConfig.GITHUB_RELEASE_CERT_SHA256.replace(":", "").lowercase()
            .takeIf { it.matches(Regex("[a-f0-9]{64}")) } ?: return true
        return runCatching { signerDigests(context).contains(pinned) }.getOrDefault(false)
    }

    private fun signerDigests(context: Context): Set<String> {
        val pm = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                ?: return emptySet()
            if (info.hasMultipleSigners()) info.apkContentsSigners else info.signingCertificateHistory
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
        } ?: return emptySet()
        return signatures.map { sig ->
            MessageDigest.getInstance("SHA-256").digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
        }.toSet()
    }
}
