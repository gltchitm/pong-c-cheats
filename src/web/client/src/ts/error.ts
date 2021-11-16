import { get, writable } from 'svelte/store'
import { communicator } from './communicator'
import { view, View } from './view'

const errorMessage = writable('')

const error = (message: string) => {
    get(communicator)?.close()
    view.set(View.Welcome)
    errorMessage.set(message)
}

export { errorMessage, error }
