package com.argent.core.domain.transfer

import com.argent.core.domain.model.Account
import com.argent.core.domain.model.Money

/**
 * Pure business rule: can this amount be transferred out of this account right now?
 *
 * This lives in `:core:domain` precisely because it has no Android, no coroutines,
 * no framework — it is trivially unit-testable and is the same rule whether the
 * transfer is made online or queued offline.
 */
class ValidateTransferAmount {

    operator fun invoke(source: Account, amount: Money): TransferAmountResult = when {
        amount.currency != source.availableBalance.currency ->
            TransferAmountResult.Invalid.CurrencyMismatch

        !amount.isPositive ->
            TransferAmountResult.Invalid.NonPositive

        amount > source.availableBalance ->
            TransferAmountResult.Invalid.InsufficientFunds(
                shortBy = amount - source.availableBalance,
            )

        else -> TransferAmountResult.Valid
    }
}

sealed interface TransferAmountResult {
    data object Valid : TransferAmountResult

    sealed interface Invalid : TransferAmountResult {
        data object NonPositive : Invalid
        data object CurrencyMismatch : Invalid
        data class InsufficientFunds(val shortBy: Money) : Invalid
    }
}
