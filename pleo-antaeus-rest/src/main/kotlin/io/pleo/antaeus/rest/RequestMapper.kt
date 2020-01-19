package io.pleo.antaeus.rest

import io.pleo.antaeus.models.InvoiceStatus

object RequestMapper {
    fun mapStatus(status: String): InvoiceStatus {
        return InvoiceStatus.valueOf(status)
    }
}