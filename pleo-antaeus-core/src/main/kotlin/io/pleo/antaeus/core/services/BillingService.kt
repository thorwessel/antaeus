package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.InvoiceStatus

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    fun runBilling() {
        val pendingInvoices = invoiceService.fetchAllPending()

        pendingInvoices?.forEach {
            try {
                val invoice = invoiceService.fetch(it.id)
                if (invoice.status == InvoiceStatus.PENDING) {
                    paymentProvider.charge(it)
                }
            } catch (ex: InvoiceNotFoundException) {

            }
        }
    }
}