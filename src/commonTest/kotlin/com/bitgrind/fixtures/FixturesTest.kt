package com.bitgrind.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.fail

class FixturesTest {

    var Fixtures.intValue by fixture { 1 }
    var Fixtures.intValueNoDefault by fixture<Int>()
    var Fixtures.nullable by fixture<String?> { "Hello" }
    var Fixtures.nullableNoDefault by fixture<String?>()
    var Fixtures.nullableNullDefault by fixture<String?> { null }


    @Test
    fun testUniqueInstances() {
        val fixtures1 = fixtures().apply { intValue = 100 }
        val fixtures2 = fixtures().apply { intValue = 200 }

        assertNotEquals(fixtures1.intValue, fixtures2.intValue)
    }

    @Test
    fun testWriteAfterRead() {
        val fixtures = fixtures()

        val value = fixtures.intValue
        assertEquals(1, value)

        runCatching {
            fixtures.intValue = 200
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }

    @Test
    fun testWriteNullAfterRead() {
        val fixtures = fixtures()

        val value = fixtures.nullable
        assertEquals("Hello", value)

        runCatching {
            fixtures.nullable = null
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }

    @Test
    fun testWriteTwice() {
        val fixtures = fixtures()

        fixtures.intValue = 100

        runCatching {
            fixtures.intValue = 200
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }

    @Test
    fun testWriteTwiceNullable() {
        val fixtures = fixtures()

        fixtures.nullable = null

        runCatching {
            fixtures.nullable = null
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }

    @Test
    fun testReadNullDefault() {
        val fixtures = fixtures()

        val nullableString = fixtures.nullableNullDefault
        assertNull(nullableString)
    }

    @Test
    fun testReadBeforeInit() {
        val fixtures = fixtures()

        runCatching {
            val intValue = fixtures.intValueNoDefault
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }

    @Test
    fun testReadBeforeInitNullable() {
        val fixtures = fixtures()

        runCatching {
            val nullableString = fixtures.nullableNoDefault
        }.onSuccess {
            fail("Expected IllegalStateException")
        }.onFailure {
            assertIs<IllegalStateException>(it)
        }
    }
}