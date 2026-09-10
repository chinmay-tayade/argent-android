package com.argent.core.domain

import com.argent.core.domain.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `rejects non ISO currency`() {
        assertFailsWith<IllegalArgumentException> { Money(100, "pounds") }
        assertFailsWith<IllegalArgumentException> { Money(100, "gbp") }
    }

    @Test
    fun `adds and subtracts within a currency`() {
        assertEquals(Money.gbp(300), Money.gbp(100) + Money.gbp(200))
        assertEquals(Money.gbp(-50), Money.gbp(100) - Money.gbp(150))
    }

    @Test
    fun `refuses arithmetic across currencies`() {
        assertFailsWith<IllegalArgumentException> { Money.gbp(100) + Money(100, "EUR") }
    }

    @Test
    fun `compares by minor units`() {
        assertTrue(Money.gbp(200) > Money.gbp(199))
        assertEquals(0, Money.gbp(200).compareTo(Money.gbp(200)))
    }
}
