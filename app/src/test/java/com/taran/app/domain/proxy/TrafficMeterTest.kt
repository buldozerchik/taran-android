package com.taran.app.domain.proxy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrafficMeterTest {
    @Test
    fun accumulatesDeltasAndSmoothsRates() {
        var bytes = 100L to 200L
        var at = 1_000L
        val meter = TrafficMeter(counters = { bytes }, now = { at })
        assertEquals(TrafficStats(0, 0, 100, 200), meter.sample(10))
        bytes = 1_100L to 2_200L
        at = 2_000L
        assertEquals(TrafficStats(500, 1_000, 1_100, 2_200), meter.sample(10))
        bytes = 2_100L to 4_200L
        at = 3_000L
        assertEquals(TrafficStats(750, 1_500, 2_100, 4_200), meter.sample(10))
    }

    @Test
    fun counterResetKeepsSessionTotals() {
        var bytes = 1_000L to 2_000L
        var at = 1_000L
        val meter = TrafficMeter(counters = { bytes }, now = { at })
        meter.sample(10)
        bytes = 100L to 200L
        at = 2_000L
        assertEquals(TrafficStats(50, 100, 1_100, 2_200), meter.sample(10))
    }

    @Test
    fun newSessionClearsOldTotalsAndRates() {
        var bytes = 1_000L to 2_000L
        var at = 1_000L
        val meter = TrafficMeter(counters = { bytes }, now = { at })
        meter.sample(10)
        bytes = 30L to 40L
        at = 2_000L
        assertEquals(TrafficStats(0, 0, 30, 40), meter.sample(20))
    }

    @Test
    fun longScreenOffGapDoesNotProduceSpeedSpike() {
        var bytes = 100L to 200L
        var at = 1_000L
        val meter = TrafficMeter(counters = { bytes }, now = { at })
        meter.sample(10)
        bytes = 10_000L to 20_000L
        at = 60_000L
        assertEquals(TrafficStats(0, 0, 10_000, 20_000), meter.sample(10))
    }

    @Test
    fun noTunnelProducesNoSample() {
        assertNull(TrafficMeter(counters = { null }, now = { 1_000L }).sample(10))
    }
}
