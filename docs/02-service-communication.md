## Service Communication

### Order Service → Inventory Service

The communication is synchronous, implemented using Spring's `WebClient`.

- Synchronous Communication: Send a request → wait for the response → continue the process.

Although `WebClient` is asynchronous by nature (returns `Mono`/`Flux`), it is
converted to a synchronous call using the `.block()` method, since the order
flow currently requires the inventory check to complete before proceeding.

### Why a Bulk Request Instead of One-by-One Calls

Instead of calling the inventory service once per item, `order-service` sends
a **single batch request** containing the full list of items in the order.

This design choice avoids the performance issue of making N separate HTTP
calls when an order contains a large number of items, which would
significantly slow down the system as order size grows.

### Inventory Response Structure

`inventory-service` responds with a list of items, where each entry contains:

| Field       | Type    | Description                                |
|-------------|---------|----------------------------------------------|
| `itemName`  | String  | Name of the item                            |
| `available` | Boolean | Whether the item is currently in stock      |

Example response:
​```json
[
  { "itemName": "item1", "available": true },
  { "itemName": "item2", "available": false }
]
​```

### Order Placement Condition

- If **all** items in the response have `available = true` → the order **can be placed**.
- If **any** item has `available = false` → the order **cannot be placed**, and it is rejected.