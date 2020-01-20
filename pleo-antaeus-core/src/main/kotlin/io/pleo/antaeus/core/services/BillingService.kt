package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.sql.Timestamp
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
    fun runBilling() {
        logger.info { "Starting processing invoices" }
        val pendingInvoices = getScheduledPendingInvoices()

        processInvoices(pendingInvoices)
    }

    private fun getScheduledPendingInvoices(): List<Invoice>? {
        return invoiceService
            .fetchAllWithStatus(InvoiceStatus.PENDING)
            ?.filter {
                it.scheduleDate <= Timestamp.valueOf(LocalDateTime.now())
            }
    }

    private fun processInvoices(invoices: List<Invoice>?) {
        runBlocking {
            invoices?.forEach {invoice ->
                launch { processInvoice(invoice) }
            }
        }
    }

    private fun processInvoice(invoice: Invoice) {
        logger.info { "Processing invoice id: ${invoice.id}" }

        try {
            val customer = customerService.fetch(invoice.customerId)
            val invoiceInfo = invoiceService.fetch(invoice.id)

            if (invoiceInfo.status == InvoiceStatus.PENDING) {
                invoiceService.markInvoiceProcessing(invoice.id)
                val payment = attemptPayment(invoiceInfo, customer)
                checkCharge(payment, invoice)
            }

        } catch (ex: InvoiceNotFoundException) {
            logger.error { "Invoice id: ${invoice.id} not found, manuel intervention needed" }
        } catch (ex: NetworkException) {
            logger.warn { "Payment for invoice id: '${invoice.id}' failed with a network error, schedule to attempt later" }
            invoiceService.rescheduleAndMarkPending(invoice.id)
        } catch (ex: CurrencyMismatchException) {
            logger.error { "Invoice id: ${invoice.id} currency does not match customer, will mark invoice as failed" }
            invoiceService.markInvoiceFailed(invoice.id)
        }
    }

    private fun attemptPayment(invoice: Invoice, customer: Customer): Boolean {
        if (invoice.amount.currency != customer.currency) {
            throw CurrencyMismatchException(customer.id, invoice.id)
        }

        return paymentProvider.charge(invoice)
    }

    private fun checkCharge(chargeSuccessful: Boolean, invoice: Invoice) {
        if (chargeSuccessful) {
            logger.info { "Payment for invoice id: '${invoice.id}' was successful" }
            invoiceService.markInvoicePaid(invoice.id)
        } else {
            logger.warn { "Payment for invoice id: '${invoice.id}' failed, manuel intervention required" }
        }
    }
}