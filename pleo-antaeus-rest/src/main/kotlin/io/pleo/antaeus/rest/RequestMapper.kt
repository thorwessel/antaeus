package io.pleo.antaeus.rest

import io.pleo.antaeus.models.InvoiceStatus

object RequestMapper {
    fun mapStatus(status: String?): InvoiceStatus? {
        return if (InvoiceStatus.values().any { it.name == status }) {
            InvoiceStatus.valueOf(status!!)
        } else {
            null
        }
    }
}