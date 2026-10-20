# Анимации и переходы

Вопросы раздела — про то, как движется UI: property-анимации, MotionLayout и переходы между экранами.

### Property-анимации

- `ObjectAnimator` — меняет свойство View по имени: `alpha`, `translationX/Y`, `scaleX/Y`, `rotation`
- `ValueAnimator` — генерирует значения по времени; вы сами применяете их в `onAnimationUpdate`
- `view.animate()` → `ViewPropertyAnimator`: компактный набор одновременных свойств
- Прерванная property-анимация оставляет честное состояние — в отличие от tween

### Tween — legacy

- `AlphaAnimation/TranslateAnimation` — анимируют отрисовку, а не свойства: касания «щёлкают» по старому месту
- На новых экранах не используем; объяснение разницы — частый собесовый вопрос

### MotionLayout

- Потомок `ConstraintLayout`; сцена описывается в `MotionScene` XML: начальный/конечный ConstraintSet, `OnSwipe`/`OnClick`, ключевые кадры
- Кейсы: collapsing toolbar, drag-to-reveal, onboarding

### Переходы

- `TransitionManager.beginDelayedTransition` + `ChangeBounds` — авто-анимация изменений раскладки
- Activity/Fragment транзишены и shared elements — сквозной переход («карточка → детализация»)
- `AnimatedVectorDrawable` — анимированные иконки

### Питфоллы и собеседование
- «Чем property-анимация отличается от View (tween)?» — главный вопрос, идёт через возможность касаний
- «Что даёт MotionLayout по сравнению с ручными AnimatorSet?» — декларативность и жесты
- Помните о производительности: анимируйте alpha/translation (без перерасчёта layout), а не margins/width
