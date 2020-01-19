package io.pleo.antaeus.models

import java.sql.Timestamp

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus,
    val dueDate: Timestamp,
    val scheduleDate: Timestamp
)
