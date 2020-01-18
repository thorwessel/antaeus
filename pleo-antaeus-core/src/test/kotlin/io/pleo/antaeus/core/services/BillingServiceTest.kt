package io.pleo.antaeus.core.services

import io.mockk.Called
import io.mockk.Runs
import io.mockk.just
import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {

    private val pendingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PENDING
    )

    private val processingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PROCESSING
    )

    private val paidInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PAID
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
    fun `runBilling will call payment provider charge`() {
        billingService.runBilling()

        verify { paymentProvider.charge(any()) }
    }

    @Test
    fun `runBilling will check status of invoice before attempting to charge`() {
        billingService.runBilling()

        verifyOrder {
            invoiceService.fetch(pendingInvoice.id)
            paymentProvider.charge(pendingInvoice)
        }

        verifyOrder {
            invoiceService.fetch(pendingInvoice.id)
            paymentProvider.charge(pendingInvoice)
        }
    }

    @Test
    fun `Will not charge invoice when invoiceService throws`() {
        every { invoiceService.fetch(pendingInvoice.id) } throws InvoiceNotFoundException(pendingInvoice.id)

        billingService.runBilling()

        verify(exactly = 0) { paymentProvider.charge(pendingInvoice) }
    }

    @Test
    fun `runBilling will change status of invoice to PROCESSING when handling`() {
        billingService.runBilling()

        verify { invoiceService.markInvoiceProcessing(pendingInvoice.id) }
    }

    @Test
    fun `runBilling will change status of invoice to PAID when charge is successful`() {

        billingService.runBilling()

        verify { invoiceService.markInvoicePaid(pendingInvoice.id) }
    }
}