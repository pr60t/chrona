# CHRONA Component Manifest — Batch 1 Inventory

Every component in the CHRONA Design System is delivered as an editable, layer-grouped vector asset on a `360dp` component base with documented responsive scaling to `412 × 915dp` screen artboards (`372dp` card width with `20dp` side margins).

| Component | SVG File | Layer / Group ID | Dimensions (360dp / 412dp) | Supported States |
| :--- | :--- | :--- | :--- | :--- |
| Top Application Bar | `navigation.svg` | `#top-app-bar-360`, `#top-app-bar-412` | `360×64dp` / `412×64dp` | Default, Back-enabled |
| Three-Dot Overflow Menu | `navigation.svg` | `#component-overflow-menu` | `220×156dp` (`r=18dp`) | Settings, Screensaver, About |
| Bottom Navigation (5-Item) | `navigation.svg` | `#layer-bottom-nav-5-states` | `360×80dp` / `412×80dp` | All 5 active states (Alarms, World Clock, Timer, Stopwatch, Bedtime) |
| Selected Navigation Pill | `navigation.svg` | `#nav-item-*` | `56×32dp` (`r=16dp`) | Selected (`#2A384D`), Unselected |
| Primary Pill Button | `buttons.svg` | `#layer-primary-buttons` | `360×52dp` / `372×52dp` | Enabled, Pressed, Disabled |
| Secondary Button | `buttons.svg` | `#layer-secondary-buttons` | `220×48dp` (`r=24dp`) | Enabled, Active, Disabled |
| Icon Button | `buttons.svg` | `#layer-icon-buttons-toggles` | `48×48dp` (`r=24dp`) | Default, Selected, Pressed, Disabled |
| Search Field | `buttons.svg` | `#layer-search-fields` | `360×52dp` / `372×52dp` | Enabled, Active/Focused, Disabled |
| Timezone Card (With Image) | `cards.svg` | `#layer-timezone-card-with-image` | `360×80dp` / `372×80dp` | Default, Active (`80×64dp` tile + `4dp` strip) |
| Timezone Card (No Image) | `cards.svg` | `#layer-timezone-card-no-image` | `360×76dp` / `372×76dp` | Default, Selected, Disabled |
| Timezone Color Strip | `cards.svg` | `#card-tz-*` | `4×64dp` (`r=2dp`) | 8 persistent pastel slots |
| Map Pin | `dialogs.svg` | `#layer-overlay-specs` | `9dp` dot / `26dp` halo | Default, Selected Halo |
| Weather Card | `cards.svg` | `#card-weather` | `360×104dp` / `372×104dp` | Default |
| Time Comparison Card | `cards.svg` | `#card-time-comparison` | `372×156dp` (`r=20dp`) | Active overlap window |
| Alarm Card | `cards.svg` | `#card-alarm-active`, `#card-alarm-disabled` | `360×124dp` / `372×124dp` | Active/Enabled, Disabled/Off |
| Timer Card | `cards.svg` | `#card-timer-running` | `360×124dp` / `372×124dp` | Running, Paused |
| Settings Row | `cards.svg` | `#component-settings-row` | `360×72dp` / `372×72dp` | Enabled, Active switch |
| Toggle (M3 Switch) | `buttons.svg` | `#toggle-*` | `52×32dp` (`r=16dp`) | Active/On, Inactive/Off, Disabled |
| Time Picker | `dialogs.svg` | `#component-time-picker` | `360×420dp` (`r=28dp`) | Hour selected, Minute selected, AM/PM |
| Weekday Selector | `buttons.svg` | `#component-weekday-selector` | `40×40dp` pills (`r=20dp`) | Selected (`#93A8C7`), Unselected |
| Bottom Sheet | `dialogs.svg` | `#component-bottom-sheet` | `360dp` / `412×420dp` (`r=28dp`) | Expanded modal drawer |
| Dialog | `dialogs.svg` | `#component-dialog-confirmation` | `360dp` / `492×220dp` (`r=28dp`) | Destructive & standard confirmation |
| Confirmation Message | `dialogs.svg` | `#component-dialog-confirmation` | `360dp` / `372×54dp` (`r=16dp`) | Success & timezone confirmation snackbars |
