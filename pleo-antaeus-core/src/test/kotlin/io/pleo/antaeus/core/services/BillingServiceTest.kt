package io.pleo.antaeus.core.services

import io.mockk.Runs
import io.mockk.just
import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {

    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingService = BillingService(
        paymentProvider,
        invoiceService
    )

    private val pendingInvoice = Invoice(
        id = 1,
        customerId = 1,
        amount = Money(
            value = BigDecimal(999),
            currency = Currency.EUR
        ),
        status = InvoiceStatus.PENDING
    )

    @Test
    fun `runBilling will call payment provider charge`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAllPending() } returns listOf(pendingInvoice)


        billingService.runBilling()

        verify { paymentProvider.charge(any()) }
    }

    @Test
    fun `runBilling will check status of invoice before attempting to charge`() {
        every { paymentProvider.charge(any()) } returns true
        every { invoiceService.fetchAllPending() } returns listOf(pendingInvoice)
        every { invoiceService.fetch(pendingInvoice.id) } returns pendingInvoice

        billingService.runBilling()

        verifyOrder {
            invoiceService.fetch(pendingInvoice.id)
            paymentProvider.charge(pendingInvoice)
        }
    }
}