package com.argent.core.common

import com.argent.core.common.result.Outcome
import com.argent.core.common.result.fold
import com.argent.core.common.result.map
import org.junit.Assert.assertEquals
import org.junit.Test

class OutcomeTest {

    @Test
    fun `map transforms success and passes failure through`() {
        val ok: Outcome<Int, String> = Outcome.Success(2)
        assertEquals(Outcome.Success(4), ok.map { it * 2 })

        val err: Outcome<Int, String> = Outcome.Failure("boom")
        assertEquals(Outcome.Failure("boom"), err.map { it * 2 })
    }

    @Test
    fun `fold picks the right branch`() {
        val ok: Outcome<Int, String> = Outcome.Success(10)
        assertEquals("ok:10", ok.fold(onSuccess = { "ok:$it" }, onFailure = { "err:$it" }))

        val err: Outcome<Int, String> = Outcome.Failure("nope")
        assertEquals("err:nope", err.fold(onSuccess = { "ok:$it" }, onFailure = { "err:$it" }))
    }
}
