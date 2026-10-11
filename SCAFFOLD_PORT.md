# Imported Scaffold modes

The earlier summarized implementation has been replaced by direct translations of
the supplied gameplay methods in `RavenScaffoldCore.java`. It now owns the source
placement search, queues, aim verification and input phases, instead of feeding
preferred angles into KRS's native scaffold search. The supplied script credits
**mko0763**. Root `Scaffold.java` remains unchanged and is not part of the client build.

This is **not binary/byte-for-byte Raven compatibility**: Minecraft 26.2 has different
APIs, packets and physics, and several external Raven helpers were not supplied.
See the exact verification boundary below and [the Raven port notes](RAVEN_PORTS.md).

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
semantics regardless of that group's sprint selector. Native KRS **No Sprint**
does not override the source policies in imported modes.

**Source Keep Y** supplies **Off / Telly A / Telly B** independently of native KRS
KeepY. Mode-specific options, including Ninja conditions, Breezily timing, Rush
chains, Hypixel Telly timing and Hop speed, appear only where relevant. There are
41 imported behavior settings plus 13 source mode/KeepY keybinds, all registered
with KRS's settings system. Source auto-swap, ice threshold, click timing, silent
swing and item-spoof controls are now included.

Native rotation, search, timer, picker, No Sprint, Edge Safe, KeepY and Down settings
are hidden and do not override imported behavior. Source modes use main-hand
placement and the supplied backward hotbar traversal/eligibility predicates,
not KRS's offhand or intelligent-picker extensions. KRS's HUD remains in use;
the source counter includes all 36 inventory slots, as in the supplied script.

Planning, rotation and placement run before input generation; source pre-input
changes run before movement correction; tower/Hop motion and prediction capture
run post-input. Source-quantized rotations are not snapped a second time to KRS's
mouse grid. Temporary movement-fix ownership does not toggle the user's module.

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

With native **Normal/Telly**, KRS's **Vanilla / NCP / Hypixel** tower names retain
their legacy meaning. With an imported bridge mode, **Vanilla/Hypixel** select the
supplied source sequences, also available through their explicit `Source` aliases.
The extra NCP entry remains the legacy KRS sequence, not a source tower profile.
Source Vanilla leaves vanilla jump motion intact and does not use Tower Speed.

Enable **Tower**, hold jump and remain stationary to tower in an imported bridge
mode. Movement plus jump stays a bridging jump, matching the reference script.
Native Normal/Telly retain **Tower When**. Source session resets, motion-only resets,
mode switching, support-name checks and branch ordering follow the reference.
The previous additional ground-start/headroom/position-validation gates were removed.
Some source sequences can therefore launch without confirmed support; they are not
safety-checked replacements. OnGround flags affect packets, not local ground state.

## Adaptations and verification limits

- `RavenScaffoldCore` retains the original gameplay method names and source ranges,
  including eight queue storage cells, three populated entries, cursor/retry/skip
  state, four-tick verification and rollback handling.
- **Raven `Simulation.create()` is missing.** The isolated `RavenScaffoldSimulation`
  replacement follows the supplied calls with detached walking/collision prediction;
  it cannot establish identical fluid, climbing, step-up or pose behavior. The original
  fallback paths remain, but this dependency is still a parity gap.
- Raycasts, placement acceptance, silent rotation scheduling, legacy block metadata
  and inventory prediction require explicit native adapters. Modern block shapes,
  reach enforcement and packet protocol cannot be made byte-identical to 1.8.
- The old direction-255 C08 click maps to sequenced modern use-item. The old packet's
  sentinel position and embedded stack have no direct modern equivalent.
- Raven fonts, HUD editor, placement highlights and debug overlays remain omitted.
  Source gameplay hotkeys and ice selection are now included.
- Source Disable In GUI controls the source pause; KRS's blocked-input/LookTP handling
  and world/player identity cleanup remain integration boundaries.
- No guarantee is made about server acceptance or anti-cheat compatibility.

### Automated checks

Use **JDK 25** (the existing project requirement):

```powershell
.\gradlew.bat scaffoldModeTest --no-daemon
.\gradlew.bat build --no-daemon
```

`scaffoldModeTest` runs with the existing project dependencies and is included in
`check`/`build`. It compiles selected unchanged methods from the root reference
against API-only test doubles, then compares the translated algorithms.

- 42,000 cases across all 21 tower sequences: persistent state, motion/position
  outputs, spoof flags and support-query order.
- 5,000 stateful steps across 16 extracted source/translated Scaffold methods:
  quantization, easing, controlled jitter, movement angles and queue construction.
- Native catalog, setting/key registration and JSON save/load checks.
- New mixin descriptors and invocation targets checked against actual 26.2 bytecode.

These checks pass. They do **not** execute the entire Scaffold planner against a
live world, prove simulator equivalence, or prove mixins apply in a running client.

### In-game smoke test still required

Use a local test world and ordinary full blocks first:

1. Check Normal/Telly and the three original KRS tower entries against the old config.
2. Try each imported bridge mode walking straight, diagonally, and while jumping.
3. Try every Hypixel style/sprint policy, Hop profile, and both Source Keep Y profiles.
4. Try each source tower stationary, then release jump; also check ceilings, partial
   support, running out of blocks, and rejected/removed support blocks.
5. Switch modes while enabled, open/close a GUI, change worlds, and disable while
   sneaking/towering. Confirm no stuck input, rotation, timer or inventory slot.
6. Check both states of Movement Fix: source ownership should be temporary, and
   Ninja/grounded Breezily should use the source's correction bypass.

Implementation: `Scaffold.java`, `ScaffoldModeController.java`, `RavenScaffoldCore.java`,
`RavenScaffoldAdapter.java`, `RavenScaffoldSimulation.java`, `ScaffoldTowerController.java`,
`ScaffoldTowerState.java`, and `ScaffoldTowerSupport.java` under
`src/client/java/com/instrumentalist/krs/hacks/features/player/`; keyboard integration
is in `src/client/java/com/instrumentalist/mixin/injector/KeyboardInputMixin.java`.
