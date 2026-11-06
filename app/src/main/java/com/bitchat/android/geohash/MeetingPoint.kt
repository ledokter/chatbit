package com.bitchat.android.geohash

import java.util.Locale

/**
 * chatbit: a place to meet in real life, written three open ways at once. Everything is
 * computed on the phone: nothing is looked up online, so the place never leaves the device
 * unless the user sends it.
 */
data class MeetingPoint(val latitude: Double, val longitude: Double) {
    val geohash: String get() = Geohash.encode(latitude, longitude, GEOHASH_PRECISION)
    val plusCode: String get() = PlusCode.encode(latitude, longitude, 11)
    val coordinates: String get() = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
    val geoUri: String get() = String.format(Locale.US, "geo:%.6f,%.6f?q=%.6f,%.6f", latitude, longitude, latitude, longitude)

    /** The line sent in the channel: readable by people and by any map app. */
    fun message(prefix: String): String = "$prefix geohash $geohash · $plusCode · $coordinates"

    companion object {
        /** 9 characters ≈ 4.8 m × 4.8 m, the size of a meeting spot. */
        const val GEOHASH_PRECISION = 9

        private const val GEOHASH_CHARS = "0123456789bcdefghjkmnpqrstuvwxyz"
        private val COORDS = Regex("""^\s*(?:geo:)?\s*(-?\d{1,2}(?:[.,]\d+)?)\s*[,;\s]\s*(-?\d{1,3}(?:[.,]\d+)?)""")
        private val COORDS_DOT = Regex("""^\s*(?:geo:)?\s*(-?\d{1,2}(?:\.\d+)?)\s*,\s*(-?\d{1,3}(?:\.\d+)?)""")

        enum class Kind { GEOHASH, PLUS_CODE, COORDINATES }
        data class Parsed(val point: MeetingPoint, val kind: Kind)

        /**
         * Reads what the user typed or pasted: "u09tvw0f6", "#u09tvw0f6", "8FW4V75V+8QH",
         * "V75V+8Q" (placed next to [reference]), "48.8584, 2.2945" or "geo:48.8584,2.2945".
         * Null when it is none of these.
         */
        fun parse(input: String, reference: Pair<Double, Double>? = null): Parsed? {
            val text = input.trim()
            if (text.isEmpty()) return null

            // a plus code is the only form with a '+'; ignore a town name after a short code
            val plusToken = text.split(Regex("\\s+")).firstOrNull { it.contains('+') }
            if (plusToken != null) {
                val code = plusToken.trimEnd(',', ';').uppercase()
                if (PlusCode.isFull(code)) {
                    val (lat, lng) = PlusCode.decode(code)
                    return Parsed(MeetingPoint(lat, lng), Kind.PLUS_CODE)
                }
                if (PlusCode.isShort(code) && reference != null) {
                    val (lat, lng) = PlusCode.decode(PlusCode.recoverNearest(code, reference.first, reference.second))
                    return Parsed(MeetingPoint(lat, lng), Kind.PLUS_CODE)
                }
            }

            (COORDS_DOT.find(text) ?: COORDS.find(text))?.let { m ->
                val lat = m.groupValues[1].replace(',', '.').toDoubleOrNull()
                val lng = m.groupValues[2].replace(',', '.').toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    return Parsed(MeetingPoint(lat, lng), Kind.COORDINATES)
                }
            }

            val gh = text.removePrefix("#").lowercase()
            if (gh.length in 1..12 && gh.all { it in GEOHASH_CHARS }) {
                val (lat, lng) = Geohash.decodeToCenter(gh)
                return Parsed(MeetingPoint(lat, lng), Kind.GEOHASH)
            }
            return null
        }
    }
}
