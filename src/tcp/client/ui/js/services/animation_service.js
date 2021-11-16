class AnimationService {
    static animateForward(from, to) {
        this.#animateFromToWithClasses(from, to, 'slide-out-forward', 'slide-in-forward')
    }
    static animateBackward(from, to) {
        this.#animateFromToWithClasses(from, to, 'slide-out-backward', 'slide-in-backward')
    }
    static #animateFromToWithClasses(from, to, outClass, inClass) {
        document.body.addEventListener('keydown', this.#disableKeyboardListener)
        document.body.style.pointerEvents = 'none'
        from.classList.add(outClass)
        from.addEventListener('animationend', () => {
            from.hidden = true
            from.classList.remove(outClass)

            to.classList.add(inClass)
            to.hidden = false
            to.addEventListener('animationend', () => {
                to.classList.remove(inClass)
                document.body.removeEventListener('keydown', this.#disableKeyboardListener)
                document.body.style.pointerEvents = 'auto'
            }, { once: true })
        }, { once: true })
    }
    static #disableKeyboardListener(event) {
        event.preventDefault()
        event.stopPropagation()
    }
}

export { AnimationService }
