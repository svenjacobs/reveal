# Migration Guide

## From 4.x to 5.0

Version 5 changes how the reveal overlay is rendered: instead of being hoisted to a `RevealCanvas`
at the top of the Compose hierarchy, `Reveal` now renders its overlay in a full screen popup
attached to whichever window it is composed in. This fixes the reveal effect being drawn beneath a
`ModalBottomSheet` or `Dialog` ([#100](https://github.com/svenjacobs/reveal/issues/100)) and removes
the need to manually correct misplaced effects for `ComposeView` nested in legacy Android view
hierarchies.

As a consequence, `RevealCanvas` and the whole `reveal-compat-android` artifact are no longer
needed and have been removed.

### Remove `RevealCanvas`

`RevealCanvas`, `RevealCanvasState` and `rememberRevealCanvasState()` have been removed. `Reveal` no
longer takes a `revealCanvasState` argument.

Before:

```kotlin
@Composable
fun App() {
    val revealCanvasState = rememberRevealCanvasState()

    RevealCanvas(
        modifier = Modifier.fillMaxSize(),
        revealCanvasState = revealCanvasState,
    ) {
        MainScreen(revealCanvasState = revealCanvasState)
    }
}

@Composable
fun MainScreen(revealCanvasState: RevealCanvasState) {
    val revealState = rememberRevealState()

    Reveal(
        revealCanvasState = revealCanvasState,
        revealState = revealState,
    ) {
        // Contents
    }
}
```

After:

```kotlin
@Composable
fun App() {
    MainScreen()
}

@Composable
fun MainScreen() {
    val revealState = rememberRevealState()

    Reveal(
        revealState = revealState,
    ) {
        // Contents
    }
}
```

If your app has multiple `Reveal` instances across different screens, simply remove the
`revealCanvasState` argument from each of them; no other coordination between them is necessary.

### Remove `reveal-compat-android`

The `reveal-compat-android` artifact, `FullscreenRevealOverlayInserter`,
`InPlaceRevealOverlayInserter`, `RevealOverlayInserter` and the `overlayInserter` argument of the
(now removed) `RevealCanvas` no longer exist. Remove the dependency:

```kotlin
dependencies {
    implementation("com.svenjacobs.reveal:reveal-compat-android:$REVEAL_VERSION") // remove this line
}
```

If you were using `FullscreenRevealOverlayInserter(revealableOffset = ...)` to correct a misplaced
effect, this is no longer necessary — the offset between the composition and the window is now
measured automatically. See [FAQ.md](FAQ.md) for details.

### Reveal effect now works inside `ModalBottomSheet` and `Dialog`

Previously the overlay was always drawn beneath modal bottom sheets and dialogs, since they render
into their own window. `Reveal` now renders its overlay in a popup attached to whichever window it
is composed in, so it works correctly as long as `Reveal` (and its revealables) are placed *inside*
the sheet's or dialog's content, with its own `RevealState`:

```kotlin
ModalBottomSheet(onDismissRequest = { /* ... */ }) {
    val revealState = rememberRevealState()

    Reveal(revealState = revealState) {
        // Sheet contents
    }
}
```

A `Reveal` placed *outside* the sheet or dialog still cannot reveal items *inside* it, since they
live in different windows.

### Behavioural changes

- **Clicks anywhere on screen now reach the overlay.** Previously the overlay only intercepted
  clicks within the bounds of the `Reveal` composable itself. Since the overlay is now a full screen
  popup, `onOverlayClick` and `onRevealableClick` fire for clicks anywhere on screen while the effect
  is visible.
- **`OnClick.Passthrough` is Android-only.** On Android, a revealable using `OnClick.Passthrough`
  makes the overlay popup momentarily non-touchable so the click reaches the app below. On iOS,
  Desktop and Web, popups always occupy their own input layer and cannot forward touches to layers
  below, so `OnClick.Passthrough` has no effect on those platforms.
- **UI tests see an additional semantics root.** Since the overlay now renders in a popup, Compose UI
  tests have (at least) two semantics roots while the effect is visible: the app's own root and the
  popup's root. `composeTestRule.onRoot()` becomes ambiguous in this situation; query specific nodes
  with `onNodeWithTag(...)` / `onNodeWithText(...)` instead, which search across all roots. If you
  need "root" bounds for an assertion, use `onNodeWithTag("overlay")` (Reveal's own overlay hit-test
  node) instead of `onRoot()`.
