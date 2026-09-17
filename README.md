# Rift Clash

A native Android trading card game: draft-free 1v1 duels against an AI opponent,
built with Kotlin and Jetpack Compose.

## Project structure

- `game/` -- the rules engine. Plain Kotlin (no Android/Compose dependency), so it
  builds and unit-tests with just a JDK: `./gradlew :game:test`. Contains card
  definitions, board/hand/hero state, turn structure, combat resolution, and the
  AI opponent's decision logic.
- `app/` -- the Android application. Jetpack Compose UI on top of `game`'s engine,
  plus a `ViewModel` that drives it from taps.

## How to play

- Both players start with a hero at 30 health and a 45-card starter deck (there's
  no deck builder yet -- everyone plays the same pool of cards).
- Mana starts at 1 and grows by 1 every turn, up to 10; it fully refills each turn.
- Play creatures (attack/health) and spells from your hand by spending mana.
  Newly played creatures have summoning sickness and can't attack that turn,
  unless they have **Charge**.
- **Taunt** creatures must be attacked before anything else on that side of the
  board.
- **Divine Shield** absorbs the next instance of damage a creature would take,
  then is used up. **Poisonous** creatures instantly kill anything they damage
  in combat. **Lifesteal** heals its controller's hero for the damage it deals.
  **Silence** strips all keywords from a creature.
- Reduce the enemy hero to 0 health to win. Running out of cards in your deck
  deals escalating fatigue damage instead of drawing.

### Controls
- Tap a card in hand to select it, then tap **Play** (creatures, hero-target/AoE/
  self spells), or tap a creature/hero on the board directly (targeted spells).
- Tap one of your ready creatures to select it as an attacker, then tap an enemy
  creature or the enemy hero to attack.
- Tap **End Turn** when you're done; the AI takes its turn automatically.

## Building

Requires an Android SDK (`compileSdk 35`, `minSdk 26`). Point `local.properties`
at your SDK (`sdk.dir=/path/to/Android/sdk`), then:

```
./gradlew :app:assembleDebug   # builds the APK
./gradlew :game:test           # runs the rules-engine unit tests
```

## What's next

Ideas for expanding this: a deck builder / card collection, a smarter AI
(lookahead instead of pure heuristics), sound/animation polish, and a match
history or campaign mode.
