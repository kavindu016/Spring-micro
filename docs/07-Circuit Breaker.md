# Synchronous Communication, Circuit Breaker, Timeout & Retry

The **Order Service** communicates with the **Inventory Service** synchronously. Since the order process depends on the inventory response, the Order Service waits for the Inventory Service response.

To make this communication more reliable, I use **Resilience4j** with:

* **Circuit Breaker** – Prevents continuous calls to an unavailable service.
* **Time Limiter** – Treats requests that take too long as failures.
* **Retry** – Retries failed requests automatically.

---

## 1. Circuit Breaker

The Circuit Breaker follows the **Fail-Fast** principle.

The Circuit Breaker does not know that a request has failed simply because no response has arrived. It normally detects failures when the request completes with an exception/failure.

Therefore, if the Inventory Service becomes unavailable and the request keeps waiting, the Circuit Breaker may not immediately know that it has failed.

### Circuit Breaker States

* **CLOSED** – Normal state. Requests are sent to Inventory Service.
* **OPEN** – After 5 failed requests, the circuit opens and stops sending requests.
* **HALF_OPEN** – The circuit allows 3 test requests.
    * If they succeed → `CLOSED`
    * If they fail → `OPEN`

### Configuration

```properties
resilience4j.circuitbreaker.instances.inventory.slidingWindowType=COUNT_BASED
resilience4j.circuitbreaker.instances.inventory.slidingWindowSize=5

resilience4j.circuitbreaker.instances.inventory.permittedNumberOfCallsInHalfOpenState=3

resilience4j.circuitbreaker.instances.inventory.automaticTransitionFromOpenToHalfOpenEnabled=true
```

---

## 2. Time Limiter

To solve the problem where the Inventory Service does not respond, I use a **Time Limiter**.

The Time Limiter defines how long the application should wait for a response.

```properties
resilience4j.timelimiter.instances.inventory.timeoutDuration=2s
```

This means:

> If the Inventory Service does not respond within **2 seconds**, the request is treated as a failure.

The failure can then be recorded by the Circuit Breaker.

### Why Time Limiter is Needed

**Without Time Limiter**

```text
Order Service
     │
     │ Request
     ↓
Inventory Service
     │
     │ No Response
     ↓
   Waiting...
     │
     │
   Waiting...
     │
     ↓
Circuit Breaker doesn't immediately know
that the request has failed
```

**With the Time Limiter**

```text
Order Service
     │
     │ Request
     ↓
Inventory Service
     │
     │ No Response
     ↓
  2 Seconds
     │
     ↓
Time Limiter → Timeout → Failure
                         │
                         ↓
                   Circuit Breaker
```

---

## 3. `@CircuitBreaker` and `@TimeLimiter`

Both annotations use the same Resilience4j instance name:

```java
@CircuitBreaker(
    name = "inventory",
    fallbackMethod = "fallBackMethod"
)
@TimeLimiter(name = "inventory")
```

The name **`inventory`** must match the Resilience4j configuration:

```properties
resilience4j.circuitbreaker.instances.inventory...
resilience4j.timelimiter.instances.inventory...
```

This allows both components to use the configuration for the Inventory Service.

---

## 4. Why `CompletableFuture<String>` is Used

When using a Time Limiter with asynchronous execution, the method returns a `CompletableFuture`.

Instead of:

```java
String placeOrder(...)
```

I use:

```java
CompletableFuture<String> placeOrder(...)
```

With a normal `String` return type, the calling thread blocks and waits until `placeOrder()` finishes and returns the result.

With `CompletableFuture<String>`, the method can return a **promise for a future result**.

```text
String

Request
  │
  ↓
placeOrder()
  │
  │ Thread waits
  │
  ↓
String result
```

**With `CompletableFuture`:**

```text
Request
  │
  ↓
placeOrder()
  │
  └──────→ CompletableFuture
             │
             │ Result available later
             ↓
          String
```

The actual supplier/work is executed asynchronously on a separate thread, allowing the Time Limiter to control how long the operation is allowed to run.

---

## 5. Retry

I also use **Resilience4j Retry**.

```properties
resilience4j.retry.instances.inventory.max-attempts=3
resilience4j.retry.instances.inventory.wait-duration=5s
```

This means that when an Inventory Service request fails, Resilience4j can retry the request up to **3 attempts**, waiting **5 seconds** between attempts.

For example:

```text
User clicks "Place Order"
          │
          ↓
   Attempt 1 → FAIL
          │
        5 sec
          ↓
   Attempt 2 → FAIL
          │
        5 sec
          ↓
   Attempt 3 → FAIL
          │
          ↓
      Final Failure
```

So the user sends **one request**, while the server can internally retry the Inventory Service call.

---

## Complete Flow

```text
                    User
                      │
                      │ Place Order
                      ↓
                Order Service
                      │
                      │ Synchronous Request
                      ↓
              ┌─────────────────┐
              │   Resilience4j  │
              │                 │
              │  Retry          │
              │  TimeLimiter    │
              │  CircuitBreaker │
              └────────┬────────┘
                       │
                       ↓
                Inventory Service
                       │
             ┌─────────┴─────────┐
             │                   │
          Response           No Response
             │                   │
             ↓                   ↓
          Success            2 Seconds
             │                   │
             │                   ↓
             │                Timeout
             │                   │
             │                   ↓
             │               Failure
             │                   │
             │              Retry Attempt
             │                   │
             │          ┌────────┴────────┐
             │          │                 │
             │       Success           Failure
             │          │                 │
             │          ↓                 ↓
             │       Continue        Retry Again
             │                            │
             │                       Up to 3 Attempts
             │                            │
             │                            ↓
             │                       Final Failure
             │                            │
             │                            ↓
             │                    Circuit Breaker
             │                            │
             │                   5 Failures Reached
             │                            │
             │                            ↓
             │                          OPEN
             │                            │
             │                     Stop Requests
             │                            │
             │                  Automatic Transition
             │                            ↓
             │                       HALF_OPEN
             │                            │
             │                       3 Test Calls
             │                            │
             │                ┌───────────┴───────────┐
             │                ↓                       ↓
             │            Success                   Failure
             │                │                       │
             │                ↓                       ↓
             └──────────── CLOSED                   OPEN
```

## Overall Behavior

```text
Request
   ↓
Retry
   ↓
Time Limiter
   ↓
Inventory Service
   │
   ├── Response → Success
   │
   └── No Response within 2s → Timeout → Failure
                                      ↓
                                   Retry
                                      ↓
                              Up to 3 Attempts
                                      ↓
                              Repeated Failures
                                      ↓
                             Circuit Breaker
                                      ↓
                               5 Failed Calls
                                      ↓
                                    OPEN
                                      ↓
                             Stop Communication
                                      ↓
                                  HALF_OPEN
                                      ↓
                               3 Test Requests
                               /             \
                          Success           Failure
                             ↓                 ↓
                          CLOSED             OPEN
```

## Monitoring

Resilience4j exposes useful information through Spring Boot Actuator.

```text
http://order-service/actuator/
```

The Actuator endpoints provide JSON information that can be used to understand what is happening inside the application, including Circuit Breaker, Retry, and other Resilience4j components.

For example, the Circuit Breaker endpoint can be used to inspect the current state and metrics:

```text
http://order-service/actuator/circuitbreakers
```

This makes it easier to identify whether the Circuit Breaker is **CLOSED, OPEN, or HALF_OPEN**, and to understand the failures and calls being recorded.

## Summary

| Feature               | Purpose                                                                   |
| ---------------------- | -------------------------------------------------------------------------- |
| **Circuit Breaker**   | Stops communication when repeated failures occur                         |
| **Time Limiter**      | Converts long/no-response requests into failures                         |
| **Retry**             | Automatically retries failed requests                                    |
| **CompletableFuture** | Supports asynchronous execution required for the Time Limiter            |
| **Actuator**          | Provides monitoring and JSON information about the resilience components |

This combination prevents the Order Service from continuously waiting for or repeatedly calling an unhealthy Inventory Service while still allowing temporary failures to recover automatically.