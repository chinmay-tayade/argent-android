package com.argent.core.domain

import com.argent.core.domain.model.Account
import com.argent.core.domain.model.AccountId
import com.argent.core.domain.model.AccountType
import com.argent.core.domain.model.Money
import com.argent.core.domain.transfer.TransferAmountResult
import com.argent.core.domain.transfer.ValidateTransferAmount
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateTransferAmountTest {

    private val validate = ValidateTransferAmount()

    private fun account(available: Long) = Account(
        id = AccountId("acc-1"),
        displayName = "Everyday",
        type = AccountType.CURRENT,
        balance = Money.gbp(available),
        availableBalance = Money.gbp(available),
    )

    @Test
    fun `valid when amount is within available balance`() {
        assertEquals(
            TransferAmountResult.Valid,
            validate(account(available = 10_000), Money.gbp(2_500)),
        )
    }

    @Test
    fun `invalid when amount is zero or negative`() {
        assertEquals(
            TransferAmountResult.Invalid.NonPositive,
            validate(account(available = 10_000), Money.gbp(0)),
        )
    }

    @Test
    fun `invalid with the exact shortfall when funds are insufficient`() {
        val result = validate(account(available = 2_000), Money.gbp(5_000))
        assertEquals(
            TransferAmountResult.Invalid.InsufficientFunds(shortBy = Money.gbp(3_000)),
            result,
        )
    }

    @Test
    fun `invalid on currency mismatch`() {
        assertEquals(
            TransferAmountResult.Invalid.CurrencyMismatch,
            validate(account(available = 10_000), Money(1_000, "EUR")),
        )
    }
}
