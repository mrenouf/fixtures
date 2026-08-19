# Fixtures

A Kotlin Multiplatform library for managing test fixtures with set-once semantics. Fixtures provides a lightweight alternative to mocks and mutable fakes by letting you define lazily-initialized, overridable test properties that can only be set once.

## Platforms

- JVM / Android
- JS / WasmJS
- Linux x64

## Usage

Define fixture properties as extension properties on `Fixtures`:

```kotlin
var Fixtures.clock by fixture { Clock.System }
var Fixtures.timeZone by fixture { TimeZone.UTC }
var Fixtures.userId by fixture<UserId>()  // no default — must be set before use
```

Create an instance with `fixtures()` and optionally override values before first access:

```kotlin
val f = fixtures().apply {
    userId = UserId("alice@example.com")
}

val sut = AuthService(f.clock, f.userId)
```

### Rules

- **Lazy initialization** — default values are computed on first read, and can reference other fixtures.
- **Set-once** — a fixture can be explicitly set *or* initialized from its default, but not both, and never changed after.
- **No default** — omitting the initializer makes the property behave like `lateinit`; reading before setting throws `IllegalStateException`.

### Reusable setup via extension functions

```kotlin
fun Fixtures.setClockTo4pm() {
    clock = object : Clock {
        override fun now(): Instant =
            Clock.System.todayIn(timeZone).atTime(16, 0).toInstant(timeZone)
    }
}
```

## Building

```bash
./gradlew build
```
