package com.argent.core.testing.fixtures

import com.argent.core.domain.model.Account
import com.argent.core.domain.model.AccountId
import com.argent.core.domain.model.AccountType
import com.argent.core.domain.model.Money

/** Builders so tests read as scenarios, not as constructor noise. */
fun account(
    id: String = "acc-current",
    displayName: String = "Everyday",
    type: AccountType = AccountType.CURRENT,
    balanceMinor: Long = 250_000,
    availableMinor: Long = balanceMinor,
    currency: String = "GBP",
): Account = Account(
    id = AccountId(id),
    displayName = displayName,
    type = type,
    balance = Money(balanceMinor, currency),
    availableBalance = Money(availableMinor, currency),
)
