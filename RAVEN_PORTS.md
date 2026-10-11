# Raven source ports to KRS 26.2

## Scope and source fidelity

The read-only root references are `Scaffold.java`, `Displace.java` and `ChatBypass.java`.
The compiled ports live under `src/client/java/com/instrumentalist/krs/hacks/features/`.
No source reference file was changed. Modules are disabled by default.

The goal is to retain supplied algorithms, constants, branches and event phases,
not to provide byte-for-byte Java/class/protocol identity between Raven/1.8 and 26.2.
External Raven implementations are not present, so full behavioral identity cannot
be honestly claimed. The checks below deliberately distinguish isolated source
parity from native API, live-world and server behavior.

## Scaffold

See [Scaffold mode/settings notes](SCAFFOLD_PORT.md).

The prior summarized controller and native imported-placement search have been
replaced. The source planner, face sampling, rotation grids, Ninja logic, Rush
warming/chains, KeepY stages, long-Telly queue/retry/verification, Hop profiles,
source inventory selection and packet filtering are translated into `RavenScaffoldCore`.

`ScaffoldTowerState` keeps the source state transitions independently testable.
Additional collision-ground/headroom/position guards from the first port were removed.
Source tower session handling occurs in pre-update; motion occurs post-input.

Missing helper: **Raven's `Simulation.create()` implementation**. The detached
simulation adapter is explicitly not certified equivalent. Raven's native
rotation, raycast, interaction metadata and placement APIs also require mappings.
The source-specific HUD/editor/highlight/debug presentation is not ported.

## Displace — Combat category

Native module: `combat/Displace.java`.

- Static and Dynamic angle selection; fixed Left/Right and yaw offset.
- Original ring/void scanning and dynamic three-lane scoring with wall checks.
- Per-target ten-tick delay windows, alternating displacement/compensation, and
  a queued attack-key click when leaving the displacement tick.
- Temporary movement correction, without changing the Movement Fix setting.
- Optional outbound blink released at the next game-tick boundary.
- Original arrow dimensions and 250-ms fade, submitted through 26.2's renderer.
- Knockback and whitelist conditions retain the supplied **OR** behavior: either
  disabled condition passes, so one enabled condition alone does not restrict use.

`Whitelisted items` is a KRS text setting containing registry IDs, for example:

```text
minecraft:diamond_sword, minecraft:stick
```

Comma, semicolon and whitespace separators are supported. The Raven item-list GUI
and any undocumented metadata matching are not supplied.

### Displace API boundaries

Raven **`CombatTargeting.findClosestTarget`** is not supplied. This port selects
loaded non-local alive players within nine metres, using KRS eye-to-bounding-box
distance and enabled KRS AntiBot/Teams filters. Equal-distance iteration order is
retained. Exact Raven filters/range/tie handling cannot be verified.

Knockback uses the native main-hand enchantment registry; void/collision scans use
modern world-height bounds. Rendering uses native feature/gizmo submission rather
than unavailable immediate-mode GL. Native rasterization can differ.

The blink queue belongs only to Displace and is pinned to the current connection,
world, player and play listener. It discards stale-session packets, retains send
completion callbacks, and is bounded at 2048 packets. These are modern lifecycle
adaptations; the external Raven LagRequest implementation was not supplied.

## Chat Bypass — Player category

Native module: `player/ChatBypass.java`. Setting: **Only filter known words**, true
by default. `ChatBypassText` contains the JDK-only text transform.

The source word/command lists, duplicates, case behavior, vowel code points,
first-match handling, original lowercase match string, Java split/trim behavior
and command allowlist are retained. All allowed command arguments are transformed,
**including recipient names**, because the source does that; this can change the
recipient or make a command fail. Nonallowlisted commands are left alone.

Unlike 1.8, modern chat/commands are signed. `ChatBypassNetworkMixin` transforms
`ClientPacketListener.sendChat`, `sendCommand`, and `sendUnattendedCommand` inputs
**before parsing/signing**. It does not edit, replay or forge signed packets.
The signature-required unattended-command confirmation keeps the original draft
so later submission does not transform it twice. Existing client-side Chat Commands
handling remains upstream.

No chat messages were sent during verification.

## Reproducible verification

Use JDK 25, as required by the existing build:

```powershell
.\gradlew.bat scaffoldModeTest --no-daemon
.\gradlew.bat build --no-daemon
```

The `scaffoldModeTest` task is included in `check` and `build`. Root reference files
must remain available for the differential tests. Generated reference fixtures and
logs stay in ignored `build/scaffold-test-run/`; they are not production modules.

| Check | Actual result |
| --- | --- |
| Client Java compilation | Passed |
| All 21 tower algorithms vs extracted source methods | 42,000 scenarios passed, including support-query order |
| 16 isolated Scaffold math/queue methods vs source | 5,000 stateful steps passed with controlled randomness |
| ChatBypass vs the source packet handler with API doubles | 3,414 cases passed, both settings and three default locales |
| Displace window/yaw methods vs extracted source | 621 cases passed |
| Displace item-condition truth table and arrow geometry/fade | Passed |
| Native settings/defaults and JSON config round-trips | Passed |
| New mixin targets against actual 26.2 bytecode | Nine injection contracts passed |
| Module registration and shared input-phase order | Passed |
| Packaged Gradle build / access widener validation | Passed |

The arithmetic tests compile actual source method bodies, not rewritten expected
algorithms. API-only doubles supply inputs and record outputs; they do not emulate
the Minecraft world. This does not establish complete planner, simulator, lag-handler,
renderer, signing-exchange or in-game parity. Descriptor validation is not a live
Mixin application test. Gradle still reports its pre-existing deprecation warnings.

### Remaining manual checks

1. Follow the local-world checklist in `SCAFFOLD_PORT.md`, including live mode changes,
   inventory depletion, GUI pause, rejected placements and tower-mode transitions.
2. For Displace, test static/dynamic directions, target changes, delay boundaries,
   both item conditions together, arrow fade, and blink release/disconnect handling.
3. For Chat Bypass, use benign text with known-only disabled and an allowed message
   command in a local server. Confirm recipient changes and signed-chat acceptance.
4. Confirm no stuck movement/rotation ownership after disable or world replacement.

No client/server was launched for these checks. Provide Raven's **Simulation**,
**CombatTargeting**, **RotationHelper/client rotation API**, and **LagRequest/lag-handler**
implementations to remove the remaining external-helper parity uncertainty.
