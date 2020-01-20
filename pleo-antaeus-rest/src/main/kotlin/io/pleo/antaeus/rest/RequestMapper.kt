package io.pleo.antaeus.rest

import io.pleo.antaeus.models.InvoiceStatus

// Was not able to find a better name, but I wanted to include a simple framework which could be used with other requests
object RequestMapper {
    fun mapStatus(status: String?): InvoiceStatus? {
        return if (InvoiceStatus.values().any { it.name == status }) {
            InvoiceStatus.valueOf(status!!)
        } else {
            null
        }
    }
}