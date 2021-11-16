import { writable } from "svelte/store"

enum View {
    Welcome,
    EnterToken,
    Greeting,
    Attaching,
    Authenticating,
    ControlPanel
}

const view = writable(View.Welcome)

export { View, view }
