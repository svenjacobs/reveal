## Reveal effect is misplaced

As of version 5, Reveal automatically measures the offset between the composable hierarchy and the
window and applies it to the reveal area, so this should no longer occur, including for `ComposeView`
nested in a legacy Android view hierarchy. If you still observe this on version 5+, please file an
issue with a reproduction.

## Reveal effect doesn't work inside a `ModalBottomSheet` or `Dialog`

`ModalBottomSheet` and `Dialog` render into their own window. Reveal's overlay always attaches to the
same window as the `Reveal` composable itself, so `Reveal` (and its revealables) must be placed
*inside* the sheet's or dialog's content, with its own `RevealState`, rather than around it:

```kotlin
ModalBottomSheet(onDismissRequest = { /* ... */ }) {
    val revealState = rememberRevealState()

    Reveal(revealState = revealState) {
        // Sheet contents
    }
}
```

A `Reveal` placed outside the sheet or dialog cannot reveal items inside it, since they live in
different windows and there is no supported way to bridge the two.
