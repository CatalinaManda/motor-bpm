package com.ledgertech.motor.bpm

interface MessageDispatcher {
    fun handleMessage(msg: String)
    fun handleMessage(msg: Any)
}