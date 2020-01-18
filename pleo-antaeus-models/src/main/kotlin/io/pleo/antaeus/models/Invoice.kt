package io.pleo.antaeus.models

import java.time.LocalDateTime

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus,
    val dueDate: LocalDateTime,
    val scheduleDate: LocalDateTime
)
