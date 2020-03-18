package com.ledgertech.motor.bpm

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.slf4j.LoggerFactory

@RunWith(MockitoJUnitRunner::class)
class DummyTest() {
    companion object {
        val L = LoggerFactory.getLogger(DummyTest::class.java)
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun test() {
        Assert.assertTrue(true)
    }
}
