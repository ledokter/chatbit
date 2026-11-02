package com.bitchat.android.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.bitchat.android.MainActivity
import com.bitchat.android.R

/**
 * PanicKit responder (Ripple and other panic buttons): the panic app first "connects" (the user
 * confirms here), then its alert wipes chatbit exactly like the duress PIN. Alerts from any other
 * app are ignored, so a random app cannot erase someone's data.
 */
class PanicResponderActivity : Activity() {
    companion object {
        const val ACTION_CONNECT = "info.guardianproject.panic.action.CONNECT"
        const val ACTION_DISCONNECT = "info.guardianproject.panic.action.DISCONNECT"
        const val ACTION_TRIGGER = "info.guardianproject.panic.action.TRIGGER"
        const val EXTRA_PANIC = "chatbit_panic"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // only startActivityForResult reveals the sender; PanicKit uses it for connected apps
        val caller = callingActivity?.packageName
        when (intent?.action) {
            ACTION_TRIGGER -> {
                if (caller != null && caller == SecurityPreferences.panicTrigger.value) {
                    SecurityPreferences.setPanicPending(true)
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra(EXTRA_PANIC, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                }
                finish()
            }
            ACTION_DISCONNECT -> {
                if (caller != null && caller == SecurityPreferences.panicTrigger.value) {
                    SecurityPreferences.setPanicTrigger(null)
                }
                finish()
            }
            ACTION_CONNECT -> {
                if (caller == null || caller == packageName) { finish(); return }
                val label = runCatching {
                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(caller, 0)).toString()
                }.getOrDefault(caller)
                AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle(R.string.sec_panic_title)
                    .setMessage(getString(R.string.sec_panic_connect_ask, label))
                    .setPositiveButton(R.string.sec_panic_connect_yes) { _, _ ->
                        SecurityPreferences.setPanicTrigger(caller)
                        setResult(RESULT_OK)
                        finish()
                    }
                    .setNegativeButton(R.string.cancel_lower) { _, _ -> setResult(RESULT_CANCELED); finish() }
                    .setOnCancelListener { setResult(RESULT_CANCELED); finish() }
                    .show()
            }
            else -> finish()
        }
    }
}
