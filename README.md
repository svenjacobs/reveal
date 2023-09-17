Reveal effect (also known as coach mark, onboarding, tutorial, walkthrough, etc.) with a beautiful
API for [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

![Demonstration](./assets/demo.gif)

## Terminology

| Term        | Description                                                                                |
|-------------|--------------------------------------------------------------------------------------------|
| Revealable  | An element which is revealed on the screen.                                                |
| Reveal area | The area which is revealed around the revealable. Usually with a slight padding.           |
| Overlay     | The overlay which greys out all contents except revealable. Can contain explanatory items. |

## Getting started

### Installation

The minimum supported Android SDK is 21 (Android 5.0), which is a requirement of Jetpack Compose.
Add Reveal as a dependency to your project. It's available on Maven Central.

```kotlin
dependencies {
    implementation("com.svenjacobs.reveal:reveal-core:$REVEAL_VERSION")
}
```

#### Artifacts

| Name                    | Description                                                                 |
|-------------------------|-----------------------------------------------------------------------------|
| `reveal-core`           | Contains core classes. You need this 🙂                                     |
| `reveal-shapes`         | Additional shapes for explanatory items                                     |
| `reveal-compat-android` | Compatibility utilities for Android targets with a mixed View/Compose setup |

### Compose

There are two significant composables:

First there is `RevealCanvas`, which is responsible for rendering the effect. There should only be
one `RevealCanvas` instance in the Compose hierarchy and it should be at a top or the topmost
position of the tree in order to ensure that the effect is rendered "full screen" above all other
elements.

Second the `Reveal` composable is responsible for registration of and interaction with revealable
items. There can be many `Reveal` instance and they have a direct relation to the `RevealCanvas`
instance. Usually there should be at most one `Reveal` per "screen" of an application.

```kotlin
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val revealCanvasState = rememberRevealCanvasState()

    // Single instance that should be at the top of the Compose hierarchy
    RevealCanvas(
        modifier = modifier.fillMaxSize(),
        revealCanvasState = revealCanvasState,
    ) {
        val revealState = rememberRevealState()

        // Usually one instance per screen
        Reveal(
            revealCanvasState = revealCanvasState,
            revealState = revealState,
            onRevealableClick = {},
            onOverlayClick = {},
        ) {
            // Contents
        }
    }
}
```

Inside `Reveal` specify revealable items via the `revealable` modifier.

```kotlin
enum class Keys { HelloWorld }

Column {
    Text(
        modifier = Modifier.revealable(key = Keys.HelloWorld),
        text = "Hello world",
    )
}
```

Now launch the reveal effect via `revealState.reveal(Keys.HelloWorld)`.

Nice, you just launched your first reveal effect. But what is missing is some explanatory item like
text or image next to the reveal area. So let's add one.

Explanatory items are specified via `overlayContent` of the `Reveal` composable.

```kotlin
Reveal(
    overlayContent = { key ->
        when (key) {
            Keys.HelloWorld -> {
                Surface(
                    modifier = Modifier
                        .align(horizontalArrangement = RevealOverlayArrangement.Horizontal.Start)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                ) {
                    Text("This is an explanation")
                }
            }
        }
    }
) {
    // Contents
}
```

The scope of the overlay content composable provides `align()` modifiers to align the item either to
the start, top, end or bottom of the reveal area.

`Reveal` provides two click listeners: `onRevealableClick` is called when the reveal area is clicked
with the key of the current revealable as the first argument. `onOverlayClick` is called when the
overlay is clicked somewhere, also with the key argument. Use any of these click listeners to reveal
the next item, for example for some kind of tutorial, or to hide the effect via
`revealState.hide()`.

That's it for now. For more details have a look at the [demo application](./demo-android) and the
JavaDoc. The library is well documented 😉

## Frequently Asked Questions

See [FAQ.md](FAQ.md)
