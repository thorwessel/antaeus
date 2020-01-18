package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {

    private val pendingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PENDING,
        dueDate = DateTime.now(),
        scheduleDate = DateTime.now()
    )

    private val processingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PROCESSING,
        dueDate = DateTime.now(),
        scheduleDate = DateTime.now()
    )

    private val paidInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PAID,
        dueDate = DateTime.now(),
        scheduleDate = DateTime.now()
    )

    private val paymentProvider = mockk<PaymentProvider>() {
        every { charge(any()) } returns true
    }

    private val invoiceService = mockk<InvoiceService>() {
        every { fetchAllPending() } returns listOf(pendingInvoice)
        every { fetch(pendingInvoice.id) } returns pendingInvoice
        every { markInvoiceProcessing(pendingInvoice.id) } returns processingInvoice
        every { markInvoicePaid(processingInvoice.id) } returns paidInvoice
    }


    private val billingService = BillingService(
        paymentProvider,
        invoiceService
    )


    @Test
    fun `Will call payment provider charge`() {
        billingService.runBilling()

        verify { paymentProvider.charge(any()) }
    }

    @Test
    fun `Will check status of invoice before attempting to charge`() {
        billingService.runBilling()

        verifyOrder {
            invoiceService.fetch(pendingInvoice.id)
            paymentProvider.charge(pendingInvoice)
        }
    }

    @Test
    fun `Will not charge invoice when status is PROCESSING`() {
        every { invoiceService.fetch(any()) } returns processingInvoice

        billingService.runBilling()

        verify(exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `Will not charge invoice when status is PAID`() {
        every { invoiceService.fetch(any()) } returns paidInvoice

        billingService.runBilling()

        verify(exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `Will not charge invoice when invoiceService throws`() {
        every { invoiceService.fetch(pendingInvoice.id) } throws InvoiceNotFoundException(pendingInvoice.id)

        billingService.runBilling()

        verify(exactly = 0) { paymentProvider.charge(pendingInvoice) }
    }

    @Test
    fun `Will change status of invoice to PROCESSING when handling`() {
        billingService.runBilling()

        verify { invoiceService.markInvoiceProcessing(pendingInvoice.id) }
    }

    @Test
    fun `Will change status of invoice to PAID when charge is successful`() {
        billingService.runBilling()

        verify { invoiceService.markInvoicePaid(pendingInvoice.id) }
    }

    @Test
    fun `Will not change status of invoice to PAID when charge failed`() {
        every { paymentProvider.charge(any()) } returns false

        billingService.runBilling()

        verify(exactly = 0) { invoiceService.markInvoicePaid(pendingInvoice.id) }
    }

    @Test
    fun `Will mark invoice as PENDING and schedule re-attempt date if network exception is thrown`() {
        every { paymentProvider.charge(any()) } throws NetworkException()
        every { invoiceService.rescheduleAndMarkPending(any()) } returns pendingInvoice

        billingService.runBilling()

        verify { invoiceService.rescheduleAndMarkPending(pendingInvoice.id) }
    }

    @Test
    fun `Will not mark invoice as PAID if network exception is thrown`() {
        every { paymentProvider.charge(any()) } throws NetworkException()
        every { invoiceService.rescheduleAndMarkPending(any()) } returns pendingInvoice

        billingService.runBilling()

        verify(exactly = 0) { invoiceService.markInvoicePaid(pendingInvoice.id) }
    }
}