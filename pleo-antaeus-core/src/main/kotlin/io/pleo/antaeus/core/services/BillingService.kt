package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    fun runBilling() {
        val pendingInvoices = invoiceService.fetchAllPending()

        pendingInvoices?.forEach {
            paymentProvider.charge(it)
        }
    }
}