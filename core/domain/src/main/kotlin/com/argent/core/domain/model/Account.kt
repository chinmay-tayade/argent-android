package com.argent.core.domain.model

data class Account(
    val id: AccountId,
    val displayName: String,
    val type: AccountType,
    val balance: Money,
    val availableBalance: Money,
)

@JvmInline
value class AccountId(val value: String)

enum class AccountType { CURRENT, SAVINGS, CREDIT_CARD }
