package com.bitchat.android.geohash

import kotlin.math.floor

/**
 * Open Location Code ("Plus Code"), the open Apache-2.0 grid behind Google Maps' plus codes.
 * Written here from the published specification so it works offline with no dependency.
 * 10 digits ≈ 14 m, 11 digits ≈ 3 m.
 */
object PlusCode {
    private const val ALPHABET = "23456789CFGHJMPQRVWX"
    private const val SEPARATOR = '+'
    private const val SEPARATOR_POSITION = 8
    private const val PAIR_LENGTH = 10
    private const val GRID_ROWS = 5
    private const val GRID_COLUMNS = 4
    private const val LAT_MULTIPLIER = 8000L * 3125L   // 20^3 * 5^5
    private const val LNG_MULTIPLIER = 8000L * 1024L   // 20^3 * 4^5

    fun encode(latitude: Double, longitude: Double, length: Int = 11): String {
        require(length in 8..15 && length != 9) { "length" }
        var latVal = floor((latitude.coerceIn(-90.0, 90.0) + 90.0) * LAT_MULTIPLIER).toLong()
        latVal = latVal.coerceAtMost(180L * LAT_MULTIPLIER - 1)
        val lngSpan = 360L * LNG_MULTIPLIER
        var lngVal = floor((longitude + 180.0) * LNG_MULTIPLIER).toLong()
        lngVal = ((lngVal % lngSpan) + lngSpan) % lngSpan

        val reversed = StringBuilder()
        // grid refinement digits, always computed to 5 then trimmed
        for (i in 0 until 5) {
            val row = (latVal % GRID_ROWS).toInt()
            val col = (lngVal % GRID_COLUMNS).toInt()
            reversed.append(ALPHABET[row * GRID_COLUMNS + col])
            latVal /= GRID_ROWS
            lngVal /= GRID_COLUMNS
        }
        for (i in 0 until PAIR_LENGTH / 2) {
            reversed.append(ALPHABET[(lngVal % 20).toInt()])
            reversed.append(ALPHABET[(latVal % 20).toInt()])
            latVal /= 20
            lngVal /= 20
        }
        val digits = reversed.reverse().toString().substring(0, length)
        return digits.substring(0, SEPARATOR_POSITION) + SEPARATOR + digits.substring(SEPARATOR_POSITION)
    }

    /** True for a full code such as 8FW4V75V+8QH (padding with 0 allowed before the +). */
    fun isFull(code: String): Boolean {
        val c = code.trim().uppercase()
        val sep = c.indexOf(SEPARATOR)
        if (sep != SEPARATOR_POSITION || c.count { it == SEPARATOR } != 1) return false
        // the first pair of a full code bounds the planet: latitude < 180°, longitude < 360°
        if (ALPHABET.indexOf(c[0]) > 8 || ALPHABET.indexOf(c[1]) > 17) return false
        return isValidDigits(c)
    }

    /** True for a short code such as V75V+8Q, which needs a nearby reference to be placed. */
    fun isShort(code: String): Boolean {
        val c = code.trim().uppercase()
        val sep = c.indexOf(SEPARATOR)
        if (sep < 2 || sep >= SEPARATOR_POSITION || sep % 2 != 0 || c.count { it == SEPARATOR } != 1) return false
        if (c.contains('0')) return false
        return isValidDigits(c)
    }

    private fun isValidDigits(c: String): Boolean {
        val sep = c.indexOf(SEPARATOR)
        val after = c.substring(sep + 1)
        if (after.length == 1) return false
        val before = c.substring(0, sep)
        val pad = before.indexOf('0')
        if (pad >= 0) {
            if (pad == 0 || pad % 2 != 0 || before.substring(pad).any { it != '0' } || after.isNotEmpty()) return false
        }
        return c.all { it == SEPARATOR || it == '0' || ALPHABET.indexOf(it) >= 0 }
    }

    /** Centre of a full code. */
    fun decode(code: String): Pair<Double, Double> {
        require(isFull(code)) { "not a full plus code" }
        val digits = code.trim().uppercase().replace("+", "").trimEnd('0')
        var lat = -90.0
        var lng = -180.0
        var res = 400.0
        val pairDigits = minOf(digits.length, PAIR_LENGTH)
        var i = 0
        while (i + 1 < pairDigits) {
            res /= 20.0
            lat += ALPHABET.indexOf(digits[i]) * res
            lng += ALPHABET.indexOf(digits[i + 1]) * res
            i += 2
        }
        var latSize = res
        var lngSize = res
        for (j in PAIR_LENGTH until digits.length) {
            latSize /= GRID_ROWS
            lngSize /= GRID_COLUMNS
            val d = ALPHABET.indexOf(digits[j])
            lat += (d / GRID_COLUMNS) * latSize
            lng += (d % GRID_COLUMNS) * lngSize
        }
        return Pair((lat + latSize / 2).coerceIn(-90.0, 90.0), lng + lngSize / 2)
    }

    /** Places a short code (V75V+8Q) next to a reference point, as the spec's recoverNearest. */
    fun recoverNearest(shortCode: String, refLat: Double, refLng: Double): String {
        val c = shortCode.trim().uppercase()
        if (isFull(c)) return c
        require(isShort(c)) { "not a plus code" }
        val padding = SEPARATOR_POSITION - c.indexOf(SEPARATOR)
        val resolution = Math.pow(20.0, 2.0 - padding / 2)
        val half = resolution / 2
        val full = encode(refLat, refLng).substring(0, padding) + c
        var (lat, lng) = decode(full)
        if (refLat + half < lat && lat - resolution >= -90) lat -= resolution
        else if (refLat - half > lat && lat + resolution <= 90) lat += resolution
        if (refLng + half < lng) lng -= resolution
        else if (refLng - half > lng) lng += resolution
        return encode(lat, lng, full.length - 1)
    }
}
