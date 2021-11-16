class LoadingService {
    static startLoading(hint) {
        if (hint) {
            document.querySelector('#loadingHint').classList.remove('d-none')
            document.querySelector('#loadingHint').innerText = hint
        } else {
            document.querySelector('#loadingHint').classList.add('d-none')
        }
        document.activeElement.blur()
        document.body.addEventListener('keydown', this.#disableKeyboardListener)
        document.querySelector('#loading').hidden = false
    }
    static stopLoading() {
        document.body.removeEventListener('keydown', this.#disableKeyboardListener)
        document.querySelector('#loading').hidden = true
    }
    static #disableKeyboardListener(event) {
        event.preventDefault()
        event.stopPropagation()
    }
}

export { LoadingService }
