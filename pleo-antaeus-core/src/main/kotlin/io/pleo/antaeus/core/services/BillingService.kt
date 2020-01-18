package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    fun runBilling() {
        logger.info { "Starting processing invoices" }
        val pendingInvoices = invoiceService.fetchAllPending()

        pendingInvoices?.forEach {
            processInvoice(it)
        }
    }

    private fun processInvoice(invoice: Invoice) {
        logger.info { "Processing invoice id: ${invoice.id}" }

        try {
            val invoiceInfo = invoiceService.fetch(invoice.id)

            if (invoiceInfo.status == InvoiceStatus.PENDING) {
                invoiceService.markInvoiceProcessing(invoiceInfo.id)
                val charge = paymentProvider.charge(invoiceInfo)

                if (charge == true) {
                    invoiceService.markInvoicePaid(invoiceInfo.id)
                }
            }
        } catch (ex: InvoiceNotFoundException) {
            logger.info { "Invoice id: ${invoice.id} not found, manuel intervention needed" }
        }
    }
}