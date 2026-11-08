# Skill — ms-credit-service

> Contrato: `ms-credit-service-openapi.yaml` (fuente de verdad).
> Reglas transversales: ver `common-rules.md`.
> **Reactividad**: Spring WebFlux + Reactor (`Mono`/`Flux`). No usa RxJava.

## Responsabilidad
- Créditos `PERSONAL` y `BUSINESS`.
- Estados `ACTIVE`, `PAID`, `OVERDUE`, `CANCELLED`.
- Saldo adeudado, vencimientos y morosidad.
- Pagos propios y de terceros (delegados a `ms-transaction-service`).

## Endpoints
| Método | Ruta | operationId |
|---|---|---|
| GET | `/api/v1/credits` | `findAllCredits` |
| POST | `/api/v1/credits` | `createCredit` |
| GET | `/api/v1/credits/{creditId}` | `findCreditById` |
| PATCH | `/api/v1/credits/{creditId}` | `updateCredit` |
| DELETE | `/api/v1/credits/{creditId}` | `deactivateCredit` |
| POST | `/api/v1/credits/{creditId}/payments` | `createCreditPayment` |
| GET | `/api/v1/credits/{creditId}/overdue-status` | `getOverdueStatus` |

## Headers
- GETs: `X-Correlation-ID`, `X-Channel-ID`.
- POST, PATCH, DELETE: `X-Transaction-ID` (UUID).

## Persistencia
- Base: `banking_credit`.
- Colección: `credits`.
- Escritura directa a MongoDB propia vía `ReactiveMongoRepository`.

## Reglas de negocio
- PERSONAL: máximo un crédito personal por persona → 409 si excede.
- BUSINESS: más de un crédito por empresa permitido.
- `PATCH` no permite cambiar estado a `PAID` si `balance > 0` → 409.
- Transiciones de estado financiero (`PAID`, `OVERDUE`) solo por pagos o procesos automáticos.
- `POST /payments` es **fachada**: delega a `ms-transaction-service`, responde 202.
  - Pago parcial reduce `balance`.
  - Pago total marca `PAID`.
  - Pago mayor que deuda → 422.
  - `OVERDUE`: pagar permitido, pero no crear nuevos productos.
- `GET /overdue-status` devuelve `hasOverdueDebt`, `overdueAmount`, `daysOverdue`.

### Verificación de deuda vencida antes de adquirir productos (Parte III)
- La regla se valida en el servicio que **abre** el producto (`account`, `credit`, `card`).
- **Parte II**: consulta vía API de `ms-credit-service` (`GET /overdue-status`) con Resilience4j y timeout 2 s.
- **Parte III**: proyección alimentada por `CreditOverdueStatusChanged`. No se lee la base de `ms-credit-service`.

## Eventos (Parte III)
- Tópico: `banking.credit.events.v1`.
- Productor: `ms-credit-service`.
- Eventos: `CreditCreated`, `CreditUpdated`, `CreditCancelled`.
- Eventos adicionales para reportes: `CreditPaymentApplied`, `CreditOverdueStatusChanged` (propuestas; formalizar en catálogo Kafka antes de implementar).

## Pruebas mínimas
- PERSONAL con 2 créditos → 409.
- BUSINESS con 2 créditos → OK.
- Pago parcial reduce balance.
- Pago total marca PAID.
- Pago > deuda → 422.
- PATCH intentando PAID con balance > 0 → 409.
- `GET /overdue-status` con y sin deuda.
- Controladores reactivos con Reactor.

## Criterios de aceptación
1. Endpoints y DTOs idénticos al YAML.
2. Persistencia solo en `banking_credit`.
3. Pagos delegados, sin escrituras cruzadas.
4. Reglas de morosidad cubiertas por tests.
5. La consulta de deuda vencida no accede a MongoDB de `ms-credit-service` desde otros servicios.
6. Eventos formalizados antes de publicarse.