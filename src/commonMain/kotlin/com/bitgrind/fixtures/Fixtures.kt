package com.bitgrind.fixtures

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Each Fixtures instance forms a distinct object graph of properties which may reference each other. Fixture
 * properties can be defined through extension properties in any file:
 * ```
 * var Fixtures.tranactionDb by setOnce<TransacctionDatabase> { InMemoryTransactionDb() }
 * var Fixtures.creditCardProcessor by setOnce<CreditCardProcessor> {
 *     FakeCreditCardProcessor(tranactionDb)
 * }
 *```
 *
 * Similar to lazy, values are initialized as needed using the initializer provided. A specific value may also be
 * assigned directly before use. An attempt to change the value after first access will throw an error.
 *
 * This provides an override capability which reduces the need for mocks and mutable Fakes. Setup code can also be
 * reused easily via extension functions:
 * ```
 *
 * var Fixtures.clock by setOnce { Clock.System }
 * var Fixtures.timeZone by setOnce { TimeZone.UTC }
 *
 * fun Fixtures.setClockTo4pm() {
 *     clock = object : Clock {
 *         override fun now(): Instant {
 *             return Clock.System.todayIn(timeZone)
 *                 .atTime(16, 0).toInstant(timeZone)
 *          }
 *     }
 * }
 * ```
 *
 * The initializer is optional. If absent, the property will behave the same as `lateinit`, throwing an error if
 * accessed before set.
 * ```
 * val Fixtures.loggedInUser by setOnce<UserId>()
 * ```
 *
 * To use Fixtures, create an instance using `fixtures()` and access properties defined on it:
 * ```
 * val Fixtures.authCredentials by setOnce {
 *    AuthCredentialsProvider { Credentials(loggedInUser, "password") }
 * }
 *
 * val fixtures = fixtures().apply { loggedInUser = UserId("bob@microsoft.com") }
 *
 * val sut = PasswordChangeFlow(fixtures.authCredentials)
 * ```
 *
 */
interface Fixtures {
    fun <T> get(name: String, initializer: (Fixtures.() -> T)?): T

    /** Sets the [value] of a fixture with the given [name]. */
    fun <T> set(name: String, value: T?)
}

class Fixture<T> internal constructor(
    private val initializer: (Fixtures.() -> T)? = null,
): ReadWriteProperty<Fixtures, T> {
    override fun getValue(thisRef: Fixtures, property: KProperty<*>): T {
        return thisRef.get(property.name, initializer)
    }

    override fun setValue(thisRef: Fixtures, property: KProperty<*>, value: T) {
        thisRef.set(property.name, value)
    }
}
/**
 * Creates a property that can be set only once, either directly or via the provided initializer.
 */
fun <T> fixture(
    initializer: (Fixtures.() -> T)? = null
): ReadWriteProperty<Fixtures, T> = Fixture(initializer)

/**
 * Creates a new set of fixtures.
 */
fun fixtures(): Fixtures = FixturesInstance()

private object UninitializedValue

class FixturesInstance internal constructor() : Fixtures {
    private val fixtures: MutableMap<String, Any?> = mutableMapOf<String, Any?>().withDefault { UninitializedValue }

    override fun <T> get(name: String, initializer: (Fixtures.() -> T)?): T {
        @Suppress("UNCHECKED_CAST")
        val value = fixtures.getValue(name)
        @Suppress("UNCHECKED_CAST")
        return if (value === UninitializedValue) {
            if (initializer == null) {
                error("Attempt to read uninitialized Fixture $name which has no default value.")
            }
            val initialized = initializer.invoke(this)
            fixtures[name] = initialized
            initialized
        } else {
            value
        } as T
    }

    override fun <T> set(name: String, value: T?) {
        val v = fixtures.getValue(name)
        if (v === UninitializedValue) {
            fixtures[name] = value
        } else {
            error("Attempt to replace Fixture $name which has already been initialized.")
        }
    }
}
