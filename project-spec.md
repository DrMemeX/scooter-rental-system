# Scooter Rental System

> Система аренды самокатов с поддержкой тарифов, контроля заряда, обработки поломок и ручного возврата.

---

## Table of Contents

* [1. Overview](#1-overview)
* [2. Roles](#2-roles)
* [3. Domain Model](#3-domain-model)
* [4. Scooter Classes](#4-scooter-classes)
* [5. Business Rules](#5-business-rules)

    * [5.1 Distance Validation](#51-distance-validation)
    * [5.2 Battery](#52-battery)
    * [5.3 Pricing](#53-pricing)
    * [5.4 Battery Depletion (Hourly)](#54-battery-depletion-hourly)
    * [5.5 Overtime](#55-overtime)
    * [5.6 Breakdowns](#56-breakdowns)
    * [5.7 Start Rental](#57-start-rental)
    * [5.8 Minimum Charge Threshold](#58-minimum-charge-threshold)
    * [5.9 Rental Finish Processing Order](#59-rental-finish-processing-order)
* [6. Manual Finish](#6-manual-finish)
* [7. Statuses](#7-statuses)
* [8. Maintenance](#8-maintenance)
* [9. Constraints](#9-constraints)
* [10. Architecture](#10-architecture)
* [11. Design Principles](#11-design-principles)
* [12. State Transitions](#12-state-transitions)
* [13. Configuration Rules](#13-configuration-rules)
* [14. Out of Scope](#14-out-of-scope)

---

## 1. Overview

Система реализует полный цикл аренды самокатов:

* старт аренды
* завершение аренды
* расчёт стоимости
* контроль батареи
* обработка ошибок и нештатных ситуаций
* управление парком самокатов

---

## 2. Roles

### 👤 USER

* арендует самокат
* завершает аренду
* вводит дистанцию поездки
* указывает точку возврата при штатном завершении аренды
* инициирует ручное завершение аренды (если не может завершить её штатно в допустимой точке возврата)

### 🛠 MANAGER

* создаёт, редактирует и деактивирует точки проката
* просматривает состав самокатов по точкам
* вручную перемещает самокаты между точками
* подтверждает или отклоняет запросы на ручное завершение аренды
* заряжает самокаты
* выполняет ремонт самокатов
* просматривает сервисную историю

---

## 3. Domain Model

Основные сущности:

* `User`
* `ScooterModel`
* `RentalPoint`
* `Scooter`
* `PromoCode`
* `Rental`
* `ScooterServiceEvent`

---

## 4. Scooter Classes

| Class  | Speed (km/h) | Consumption | Price/min | Price/hour | Charge |
| ------ | ------------ | ----------- | --------- | ---------- | ------ |
| SLOW   | 10           | 1           | 4 ₽       | 200 ₽      | 1000   |
| BASIC  | 20           | 2           | 8 ₽       | 400 ₽      | 1000   |
| SPEEDY | 30           | 3           | 12 ₽      | 600 ₽      | 1000   |

---

## 5. Business Rules

### 5.1 Distance Validation

Максимально допустимая дистанция определяется двумя ограничениями (временем и зарядом батареи):

```text
allowedDistance = min(
  speed × (time / 60),
  charge / consumption
)
```

✔ Допустимо:

* любое значение ≥ 0
* ≤ allowedDistance

---

### 5.2 Battery

```text
charge = charge - (distance × consumption)
```

```text
if charge < 0 → charge = 0
```

---

### 5.3 Pricing

#### Поминутный тариф

```text
total = minutes × pricePerMinute
```

---

#### Почасовой тариф

```text
total = plannedHours × pricePerHour
```



```text
📌 Важно:
При добровольном досрочном завершении аренды по почасовому тарифу стоимость не пересчитывается: пользователь оплачивает полную стоимость выбранного почасового пакета.
```
---
#### Скидка

Скидка по почасовому тарифу применяется через уникальный promo code.
Доступны скидки: 5%, 10%, 15%.
Код проверяется при старте аренды и фиксируется в истории аренды.

### 5.4 Battery Depletion (Hourly)

Если батарея села до завершения текущего часа:

```text
fullHours = minutes / 60
remaining = minutes % 60

total =
  fullHours × pricePerHour +
  remaining × (pricePerHour / 60)
  
📌 Примечание:
Остаток незавершённого часа рассчитывается по сниженной минутной ставке, полученной из почасового тарифа.

⚠️ Внимание:
Данное правило применяется только к вынужденному завершению аренды по причине разряда батареи и не распространяется на добровольное досрочное завершение аренды пользователем.
```

```text
total ≤ plannedHours × pricePerHour

📌 Важно:
пользователь не может заплатить больше, чем за весь пакет почасового тарифа.
```

---

### 5.5 Overtime

Почасовой тариф действует только в пределах оплаченного периода. После его истечения аренда либо завершается, либо продолжается с автоматическим переходом на поминутный тариф.

```text
total =
  plannedHours × pricePerHour +
  overtimeMinutes × pricePerMinute
```

```text
📌 Примечание:
Если аренда завершается по причине разряда батареи, правило overtime не применяется; используется логика Battery Depletion (Hourly).
```

---

### 5.6 Breakdowns

Вероятность поломки:

```text
~1%
```

Типы поломок:

* `TECHNICAL_BREAKDOWN`

    * без скидок
    * без штрафов

* `USER_DAMAGE`

    * штраф: `1000 ₽`

```text
finalCost = rentalCost + 1000 (если USER_DAMAGE)
```
```text
📌 Примечание:
Поломка может моделироваться вероятностным сценарием
либо фиксироваться как причина завершения аренды.
```
---

### 5.7 Start Rental

Перед началом аренды система проверяет:

* пользователь существует и активен
* самокат существует
* самокат имеет статус `AVAILABLE`
* заряд самоката не ниже минимального допустимого порога
* у пользователя нет другой активной аренды

После успешной проверки:

* создаётся запись `Rental`
* `Rental → ACTIVE`
* `Scooter → RENTED`
* фиксируются:
  * пользователь
  * самокат
  * стартовая точка
  * время начала
  * выбранный тариф

---

### 5.8 Minimum Charge Threshold

Минимально допустимый заряд самоката для начала аренды составляет 20 единиц:

```text
if currentCharge < 20 → rental forbidden

📌 Примечание:
самокат с зарядом ниже порога может быть заряжен или переведён в сервисное состояние, но не может быть выдан пользователю в новую аренду.
```
---
### 5.9 Rental Finish Processing Order

При завершении аренды обработка выполняется в следующем порядке:

1. проверка статуса аренды
2. расчёт фактической длительности
3. валидация километража
4. расчёт допустимой дистанции
5. пересчёт заряда
6. определение причины завершения аренды
7. проверка события поломки
8. расчёт итоговой стоимости
9. обновление статусов аренды и самоката

```text
📌 Примечание:
данный порядок нужен для того, чтобы исключить противоречия между зарядом, километражом, типом завершения и итоговой стоимостью аренды.
```
---

## 6. Manual Finish

Если пользователь не может вернуть самокат в допустимую точку проката, используется ручное завершение аренды:

### Шаг 1 — USER

```text
POST /rentals/{id}/request-manual-finish
```

Система:

* фиксирует фактическое время аренды
* фиксирует стоимость аренды
* фиксирует расход батареи
* завершает пользовательскую фазу аренды

```text
Rental → PENDING_MANAGER_CONFIRMATION
Scooter → RETURN_VERIFICATION_REQUIRED

📌 Важно:
После этого стоимость больше не увеличивается, аренда не продолжается.
```

---

### Шаг 2 — MANAGER (approve / reject)

```text
POST /manager/rentals/{id}/confirm-manual-finish
```

Менеджер может:

✔ APPROVE

* указывает фактическую точку возврата
* подтверждает завершение аренды

```text
Rental → FINISHED
Scooter → AVAILABLE / SERVICE_REQUIRED / MAINTENANCE
```
---
❌ REJECT

* отклоняет запрос

```text
Rental → ACTIVE
Scooter → RENTED

📌 Важно:
При отклонении аренда возвращается в активное состояние, и пользователь обязан завершить её штатным способом.
```


---

## 7. Statuses

### ScooterStatus

* `AVAILABLE`
* `RENTED`
* `SERVICE_REQUIRED`
* `MAINTENANCE`
* `RETURN_VERIFICATION_REQUIRED`

---

### RentalStatus

* `ACTIVE`
* `PENDING_MANAGER_CONFIRMATION`
* `FINISHED`

---

### TerminationReason

* `USER_FINISHED`
* `BATTERY_DEPLETED`
* `TECHNICAL_BREAKDOWN`
* `USER_DAMAGE`
* `MANAGER_CONFIRMED_RETURN`

---

## 8. Maintenance

### Charging

```text
Manager updates charge manually
```

После зарядки:

```text
status → AVAILABLE
```

---

### Repair

```text
status → MAINTENANCE
```

Менеджер:

* выполняет ремонт
* обновляет заряд
* возвращает в `AVAILABLE`

---

## 9. Constraints

* один самокат → одна активная аренда
* один пользователь → одна активная аренда
* нельзя завершить аренду дважды
* нельзя чинить арендованный самокат
* нельзя арендовать при низком заряде
* нельзя вводить отрицательные значения

---

## 10. Architecture

Основное приложение реализуется как модульный монолит:

* `common-module`
* `security-module`
* `auth-module`
* `user-module`
* `fleet-module`
* `rental-module`
* `discount-module`
* `maintenance-module`
* `outbox-module`

Дополнительно используется `notification-service` как внешний микросервис для демонстрации распределённого взаимодействия.

---

## 11. Design Principles

* разделение:

    * время → стоимость
    * дистанция → заряд

* верхние ограничения вместо симуляции движения

* явные статусы вместо неявных переходов

* обработка нештатных ситуаций через manager flow

---

## 12. State Transitions

### Scooter status transitions

* `AVAILABLE → RENTED` — старт аренды
* `RENTED → AVAILABLE` — штатное завершение аренды
* `RENTED → SERVICE_REQUIRED` — завершение аренды при заряде ниже минимально допустимого порога для новой аренды
* `RENTED → MAINTENANCE` — завершение аренды с поломкой
* `RENTED → RETURN_VERIFICATION_REQUIRED` — запрос ручного завершения
* `RETURN_VERIFICATION_REQUIRED → AVAILABLE` — ручное подтверждение менеджером
* `RETURN_VERIFICATION_REQUIRED → SERVICE_REQUIRED` — менеджер подтвердил возврат, но нужна зарядка
* `RETURN_VERIFICATION_REQUIRED → MAINTENANCE` — менеджер подтвердил возврат, но выявлена поломка
* `SERVICE_REQUIRED → AVAILABLE` — зарядка менеджером
* `MAINTENANCE → AVAILABLE` — завершение ремонта менеджером
---

## 13. Configuration Rules

* классы самокатов `(SLOW, BASIC, SPEEDY)` задаются как enum
* статусы и причины завершения задаются как enum
* тарифы, расход заряда и максимально допустимый пробег в час хранятся на уровне `ScooterModel`
* текущее состояние заряда и пробег хранятся на уровне `Scooter`
---

## 14. Out of Scope

В текущую версию проекта не входят:

* построение реальных карт и геозон
* автоматическое определение координат самоката
* автоматическая зарядка без участия менеджера
* динамическое ценообразование
---

