package com.bitchat.android.geohash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeetingPointTest {

    @Test
    fun plusCodeMatchesSpecVectors() {
        // vectors from the Open Location Code test data
        assertEquals("8FVC2222+22", PlusCode.encode(47.0000625, 8.0000625, 10))
        assertEquals("7FG49QCJ+2V", PlusCode.encode(20.3700625, 2.7821875, 10))
        val (lat, lng) = PlusCode.decode("7FG49QCJ+2V")
        assertEquals(20.3700625, lat, 1e-6)
        assertEquals(2.7821875, lng, 1e-6)
    }

    @Test
    fun plusCodeRoundTripsAtThreeMetres() {
        val code = PlusCode.encode(48.85837, 2.294481, 11)
        assertEquals(12, code.length)
        val (lat, lng) = PlusCode.decode(code)
        assertEquals(48.85837, lat, 3e-5)
        assertEquals(2.294481, lng, 3e-5)
    }

    @Test
    fun shortPlusCodeIsPlacedNearReference() {
        val full = PlusCode.encode(48.85837, 2.294481, 10)
        val short = full.substring(4)
        assertTrue(PlusCode.isShort(short))
        assertEquals(full, PlusCode.recoverNearest(short, 48.86, 2.30))
    }

    @Test
    fun rejectsNonsense() {
        assertFalse(PlusCode.isFull("hello+world"))
        assertNull(MeetingPoint.parse("bonjour à tous"))
        assertNull(MeetingPoint.parse(""))
    }

    @Test
    fun parsesEveryForm() {
        val p = MeetingPoint(48.85837, 2.294481)
        assertEquals(MeetingPoint.Companion.Kind.GEOHASH, MeetingPoint.parse("#" + p.geohash)!!.kind)
        assertEquals(MeetingPoint.Companion.Kind.PLUS_CODE, MeetingPoint.parse(p.plusCode)!!.kind)
        val c = MeetingPoint.parse("48.85837, 2.294481")
        assertNotNull(c)
        assertEquals(MeetingPoint.Companion.Kind.COORDINATES, c!!.kind)
        assertEquals(48.85837, c.point.latitude, 1e-9)
        assertEquals(2.294481, MeetingPoint.parse("geo:48.85837,2.294481")!!.point.longitude, 1e-9)
        assertEquals(p.geohash, MeetingPoint.parse(p.geohash)!!.point.geohash)
    }
}
