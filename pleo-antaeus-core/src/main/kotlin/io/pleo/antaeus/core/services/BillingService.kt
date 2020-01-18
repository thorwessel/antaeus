package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
    fun runBilling() {
        logger.info { "Starting processing invoices" }
        val pendingInvoices = invoiceService.fetchAllPending()

        if (pendingInvoices == null ) {
            logger.info { "No pending invoices found" }
        }

        pendingInvoices?.forEach {
            processInvoice(it)
        }
    }

    private fun processInvoice(invoice: Invoice) {
        logger.info { "Processing invoice id: ${invoice.id}" }

        try {
            val customer = customerService.fetch(invoice.customerId)
            val invoiceInfo = invoiceService.fetch(invoice.id)

            if (invoiceInfo.status == InvoiceStatus.PENDING) {
                invoiceService.markInvoiceProcessing(invoiceInfo.id)

                if (invoiceInfo.amount.currency != customer.currency) throw CurrencyMismatchException(customer.id, invoiceInfo.id)

                val chargeSuccessful = paymentProvider.charge(invoiceInfo)

                if (chargeSuccessful) {
                    logger.info { "Payment for invoice id: '${invoiceInfo.id}' was successful" }
                    invoiceService.markInvoicePaid(invoiceInfo.id)
                } else {
                    logger.warn { "Payment for invoice id: '${invoiceInfo.id}' failed, manuel intervention required" }
                }
            }
        } catch (ex: InvoiceNotFoundException) {
            logger.error { "Invoice id: ${invoice.id} not found, manuel intervention needed" }
        } catch (ex: NetworkException) {
            logger.warn { "Payment for invoice id: '${invoice.id}' failed with a network error, schedule to attempt later" }
            invoiceService.rescheduleAndMarkPending(invoice.id)
        } catch (ex: CurrencyMismatchException) {
            logger.error { "Invoice id: ${invoice.id} does not match customer, will mark invoice as failed" }
            invoiceService.markInvoiceFailed(invoice.id)
        }
    }
}