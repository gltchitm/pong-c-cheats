class ModalService {
    static #modal

    static init() {
        this.#modal = new bootstrap.Modal(document.querySelector('#modal'), { backdrop: 'static', keyboard: false })
    }

    static alert(title, message) {
        document.querySelector('#modalTitle').innerText = title
        document.querySelector('#modalBody').innerText = message
        document.querySelector('#modalFooter').innerHTML = ''

        return new Promise(res => {
            const okButton = document.createElement('button')
            okButton.classList = 'btn btn-primary'

            okButton.innerText = 'OK'

            document.querySelector('#modalFooter').appendChild(okButton)

            okButton.addEventListener('click', () => {
                this.#modal.hide()
                res()
            })

            document.activeElement.blur()
            this.#disableKeyboardUntilModalShown()
            this.#modal.show()
        })
    }
    static confirm(title, message) {
        document.querySelector('#modalTitle').innerText = title
        document.querySelector('#modalBody').innerText = message
        document.querySelector('#modalFooter').innerHTML = ''

        return new Promise(res => {
            const yesButton = document.createElement('button')
            const noButton = document.createElement('button')

            yesButton.classList = 'btn btn-primary'
            noButton.classList = 'btn btn-secondary'

            yesButton.innerText = 'Yes'
            noButton.innerText = 'No'

            document.querySelector('#modalFooter').appendChild(noButton)
            document.querySelector('#modalFooter').appendChild(yesButton)

            yesButton.addEventListener('click', () => {
                this.#modal.hide()
                res(true)
            })
            noButton.addEventListener('click', () => {
                this.#modal.hide()
                res(false)
            })

            document.activeElement.blur()
            this.#disableKeyboardUntilModalShown()
            this.#modal.show()
        })
    }
    static changeScorePrompt(player) {
        const title = `Change ${player[0].toUpperCase() + player.slice(1)} Score`
        document.querySelector('#modalTitle').innerText = title
        document.querySelector('#modalBody').innerHTML = ''
        document.querySelector('#modalFooter').innerHTML = ''

        const scoreInput = document.createElement('input')
        scoreInput.classList = 'form-control'
        scoreInput.type = 'number'
        scoreInput.min = 0
        scoreInput.max = 999_999
        scoreInput.maxLength = 6

        document.querySelector('#modalBody').appendChild(scoreInput)

        return new Promise(res => {
            const cancelButton = document.createElement('button')
            const confirmButton = document.createElement('button')

            cancelButton.classList = 'btn btn-secondary'
            confirmButton.classList = 'btn btn-primary'

            cancelButton.innerText = 'Cancel'
            confirmButton.innerText = 'Confirm'

            cancelButton.addEventListener('click', () => {
                this.#modal.hide()
                res(null)
            })
            confirmButton.addEventListener('click', () => {
                this.#modal.hide()

                const newScore = Number(scoreInput.value)
                if (
                    /[^0-9]/.test(scoreInput.value) ||
                    !scoreInput.value.length ||
                    newScore > 999_999 ||
                    newScore < 0
                ) {
                    res(null)
                } else {
                    res(newScore)
                }
            })

            document.querySelector('#modalFooter').appendChild(cancelButton)
            document.querySelector('#modalFooter').appendChild(confirmButton)

            document.activeElement.blur()
            this.#disableKeyboardUntilModalShown()
            this.#modal.show()
        })
    }
    static #disableKeyboardListener(event) {
        event.preventDefault()
        event.stopPropagation()
    }
    static #disableKeyboardUntilModalShown() {
        document.body.addEventListener('keydown', this.#disableKeyboardListener)
        document.querySelector('#modal').addEventListener('shown.bs.modal', () => {
            document.body.removeEventListener('keydown', this.#disableKeyboardListener)
        }, { once: true })
    }
}

export { ModalService }
