# Imported Scaffold modes

The gameplay modes from the supplied root `Scaffold.java` are adapted to KRS's
Minecraft 26.2 input, rotation, collision, inventory and placement APIs. The supplied
script credits **mko0763**. That reference file is left unchanged and is not compiled.

## Selecting modes

Open **Player > Scaffold > Mode**. Existing **Normal** and **Telly** modes and their
saved names remain available. The imported choices are:

| Mode | Ported behavior |
| --- | --- |
| God Bridge | Backward/diagonal aiming, stable face selection and sprint-angle policy |
| Telly Short | Takeoff delay, airborne turns and diagonal placement prediction |
| Telly Long | Alternating jump phases, queued placements, easing and retry handling |
| Snap | Placement-time aiming with a safe return toward the camera heading |
| Rush | Forward planning, optional sprint hops and rotation-verified placement chains |
| Ninja | Stable bridge heading, predictive edge sneaking and configurable release delays |
| Breezily | Backward heading and alternating fractional strafe input |
| Snap+ | Hypixel-style aim search and post-placement rotation recovery |
| Hypixel | Normal, Telly, Hypixel, Breezily and God Bridge styles |
| Hop | Sprint, Low, Boost, Burst and Adaptive movement profiles |

The Hypixel group also includes **None / Vanilla / Legit / NCP / Universal** sprint
policies. As in the supplied script, **Hypixel Style = Hypixel** uses Legit sprint
semantics regardless of that group's sprint selector. KRS's global **No Sprint**
still takes precedence.

**Source Keep Y** supplies **Off / Telly A / Telly B** independently of native KRS
KeepY. Mode-specific options, including Ninja conditions, Breezily timing, Rush
chains, Hypixel Telly timing and Hop speed, appear only where relevant. All 35
imported behavior settings participate in normal KRS config save/load.

The native Rotation Mode, Snap Rotation, KeepY and Down settings are hidden and do
not override imported mode behavior. Native block selection, offhand support,
item rendering/spoofing, block counter, timers and the optional Edge Safe setting
remain shared. Imported placement always checks the final rotation against the
actual target face, even if a native config previously selected None/No Hit Check.

Telly Short/Long react to jumping; enable **Source Keep Y = Telly A or Telly B**
for the supplied automatic jumping cycle. Rush Sprint Hop, Hypixel Telly/Auto Jump,
and Hop have their own jump behavior.

## Tower modes

All 21 source tower profiles are available in **Tower Mode**:

| Supplied name | KRS selector |
| --- | --- |
| Vanilla | **Source Vanilla** |
| Hypixel | **Source Hypixel** |
| Sync, Apex, Sync Burst, Sync Early Burst | Same names |
| Raven 1 Tick, Raven Prediction | Same names |
| OnGround, OnGround Early, OnGround Pulse, OnGround Prediction | Same names |
| Yuri NCP, Raven Pull Sync, Raven Pull | Same names |
| Raven Predict Early, Raven Predict Turbo | Same names |
| Raven Prediction Swift, Raven Prediction Rapid | Same names |
| Polar, Intave | Same names |

KRS's original **Vanilla / NCP / Hypixel** tower entries retain their old behavior
and default. The two `Source` aliases avoid changing existing configs. Source
Vanilla uses normal jump motion, not the native KRS Vanilla Tower Speed setting.

Enable **Tower**, hold jump and remain stationary to tower in an imported bridge
mode. Movement plus jump stays a bridging jump, matching the reference script.
Native Normal/Telly retain the **Tower When** controls. Source towers reset on
release, depletion, world/player changes, or mode changes; non-vanilla profiles
start from real collision support. OnGround-family flags affect movement packets,
not the player's local ground state.

## Adaptations and verification limits

- These are behavior ports, not a compatibility layer for the original Raven/1.8
  scripting environment. KRS supplies the placement search and rotation manager.
- Ninja uses a collision-aware one-step edge predictor rather than Raven's player
  simulator. Tower support and headroom checks use modern collision shapes.
- The Raven font/HUD editor, source mode hotkeys, debug overlays, ice selection,
  renderer reflection and invalid-use autoclick packet path were not copied.
  They are separate UI/inventory features, not additional bridge/tower modes.
- Imported actions pause in GUIs/blocked input and clean up rotations, sneak state,
  slot spoofing and timing when disabled or out of blocks.
- No guarantee is made about server acceptance or anti-cheat compatibility.

### Automated checks

Use **JDK 25** (the existing project requirement):

```powershell
.\gradlew.bat scaffoldModeTest --no-daemon
.\gradlew.bat build --no-daemon
```

`scaffoldModeTest` is a dependency-free headless check, also run by `check`/`build`.
It covers the complete catalogs, source/native tower dispatch, jump ownership,
settings registration and uniqueness, real KRS JSON config round-trips, opposing
keys, camera-relative movement math, and preservation of action flags.

The implementation was compiled and the packaged build passed with **372 headless
assertions**. This does **not** establish in-game placement or movement correctness.

### In-game smoke test still required

Use a local test world and ordinary full blocks first:

1. Check Normal/Telly and the three original KRS tower entries against the old config.
2. Try each imported bridge mode walking straight, diagonally, and while jumping.
3. Try every Hypixel style/sprint policy, Hop profile, and both Source Keep Y profiles.
4. Try each source tower stationary, then release jump; also check ceilings, partial
   support, running out of blocks, and rejected/removed support blocks.
5. Switch modes while enabled, open/close a GUI, change worlds, and disable while
   sneaking/towering. Confirm no stuck input, rotation, timer or inventory slot.
6. Check both states of Movement Fix and optional Edge Safe settings. Confirm
   Breezily's weave and Snap's rotations do not redirect intended travel.

Implementation: `Scaffold.java`, `ScaffoldModeController.java`, and
`ScaffoldTowerController.java` under
`src/client/java/com/instrumentalist/krs/hacks/features/player/`; keyboard integration
is in `src/client/java/com/instrumentalist/mixin/injector/KeyboardInputMixin.java`.
