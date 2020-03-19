package com.ledgertech.motor.corda.messages

import java.math.BigDecimal

interface ClaimEvaluatedEvent : Event {
    val stateLinearId: LinearId
}

data class ClaimEvaluatedEventImpl private constructor (
        override val stateLinearId: LinearId,
        override val node: String
): ClaimEvaluatedEvent {
    override var payload: Any? = null
        private set

    override val eventType: String = "CLAIM_EVALUATED"

    constructor(stateLinearId: LinearId,
                node: String,
                jsonEvaluationInfo: String): this(stateLinearId, node) {
        this.payload = jsonEvaluationInfo
    }
}

enum class EvalStatusReason {
    // Data was relevant to produce a result
    CONCLUSIVE_DATA,

    // Not enough data
    NOT_ENOUGH__DATA,

    // Can't decide on provided data
    INCONCLUSIVE,

    // Estimated amount is too high
    AMOUNT_THRESHOLD,

    // Another business rule forced the result
    BUSINESS_RULE
}

data class EvaluationInfo(
    val evaluated: Boolean,
    val reason: EvalStatusReason = EvalStatusReason.CONCLUSIVE_DATA,

    // True if decission was automated (e.g. via AI)
    val automatic: Boolean,
    // Probability of evaluation, in percent.
    val certainty: Int,

    // Total estimated damage value.
    val amount: BigDecimal = 0.0.toBigDecimal(),

    val damagedParts: List<DamagedPart> = listOf()
)

enum class DamageSeverity {
    LIGHT,
    MEDIUM,
    SEVERE
}

data class DamagedPart(
    val description: String,
    val value: BigDecimal,
    val labor: BigDecimal,
    val severity: DamageSeverity
)
