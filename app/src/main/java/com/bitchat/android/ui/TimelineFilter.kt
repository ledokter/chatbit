package com.bitchat.android.ui

import com.bitchat.android.model.BitchatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * chatbit: what the user chose to see in the public timeline, on top of blocking.
 * - focus: only one person's messages (long press on a message → "only their messages")
 * - query: plain text or regex search (folded line at the top of the timeline)
 * - muted: people hidden for a few minutes, kept in memory only
 * Focus and search follow the conversation on screen; mutes follow the person.
 */
object TimelineFilter {
    data class Focus(val key: String, val nickname: String)
    data class Mute(val nickname: String, val until: Long)

    private val _focus = MutableStateFlow<Focus?>(null)
    val focus: StateFlow<Focus?> = _focus

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _regex = MutableStateFlow(false)
    val regex: StateFlow<Boolean> = _regex

    private val _muted = MutableStateFlow<Map<String, Mute>>(emptyMap())
    val muted: StateFlow<Map<String, Mute>> = _muted

    private var conversationKey: String? = null

    fun senderKey(message: BitchatMessage): String = message.senderPeerID ?: message.sender

    /** A new conversation on screen: a focus or a search from the previous one makes no sense here. */
    fun onConversation(key: String) {
        if (conversationKey != null && conversationKey != key) {
            _focus.value = null
            _query.value = ""
        }
        conversationKey = key
    }

    fun setFocus(key: String, nickname: String) { _focus.value = Focus(key, nickname) }
    fun clearFocus() { _focus.value = null }

    fun setQuery(text: String) { _query.value = text }
    fun setRegex(on: Boolean) { _regex.value = on }

    fun mute(key: String, nickname: String, minutes: Int) {
        _muted.update { it + (key to Mute(nickname, System.currentTimeMillis() + minutes * 60_000L)) }
    }
    fun unmute(key: String) { _muted.update { it - key } }
    fun unmuteAll() { _muted.value = emptyMap() }
    fun isMuted(key: String): Boolean = _muted.value[key]?.let { it.until > System.currentTimeMillis() } == true

    /** Drops the mutes that ran out; returns the next expiry, if any. */
    fun pruneMutes(now: Long = System.currentTimeMillis()): Long? {
        _muted.update { map -> map.filterValues { it.until > now } }
        return _muted.value.values.minOfOrNull { it.until }
    }

    /** Null when the pattern is fine (or not a regex), else the error to show. */
    fun regexError(query: String, regex: Boolean): String? =
        if (!regex || query.isBlank()) null
        else runCatching { Regex(query, RegexOption.IGNORE_CASE); null }.getOrElse { it.message?.lineSequence()?.firstOrNull() ?: "regex" }

    fun apply(
        messages: List<BitchatMessage>,
        focus: Focus?,
        query: String,
        regex: Boolean,
        muted: Map<String, Mute>,
        now: Long = System.currentTimeMillis()
    ): List<BitchatMessage> {
        val activeMutes = muted.filterValues { it.until > now }.keys
        val matcher: ((String) -> Boolean)? = when {
            query.isBlank() -> null
            regex -> runCatching { Regex(query, RegexOption.IGNORE_CASE) }.getOrNull()?.let { r -> { s: String -> r.containsMatchIn(s) } }
            else -> { s: String -> s.contains(query.trim(), ignoreCase = true) }
        }
        if (focus == null && matcher == null && activeMutes.isEmpty()) return messages
        return messages.filter { m ->
            val key = senderKey(m)
            (focus == null || key == focus.key) &&
                key !in activeMutes &&
                (matcher == null || matcher(m.content) || matcher(m.sender))
        }
    }
}
