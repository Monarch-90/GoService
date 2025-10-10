# --- Шаг 3: Выбор Клиента ---

### Архитектурное примечание

Этот пакет намеренно оставлен пустым.

UI для этого шага (`ClientSelectorFragment`) является частью модуля `:feature_clients`, так как он полностью инкапсулирует в себе
всю логику, связанную с отображением и выбором клиентов.

Модуль `:feature_schedule` получает этот фрагмент через абстрактный интерфейс `ClientSelectorProvider` (из модуля `:navigation`),
не имея прямой зависимости от `:feature_clients`.

**Где искать код:**

* **Фрагмент:** `feature_clients/src/main/java/.../selector/ui/ClientSelectorFragment.kt`
* **Контракт:** `navigation/src/main/java/.../navigation/ClientSelectorProvider.kt`
* **Реализация:** `feature_clients/src/main/java/.../selector/ClientSelectorProviderImpl.kt`