import { ModalService } from './services/modal_service.js'
import { LoadingService } from './services/loading_service.js'
import { AnimationService } from './services/animation_service.js'

ModalService.init()

let serverAddress = null
let authServerAddress = null
let loginToken = null
let scoreUpdateCompleteResolve = null
let scoreUpdateComplete = new Promise(resolve => {
    scoreUpdateCompleteResolve = resolve
})
let stopAfterScoreUpdate = false

const waitForScoreUpdateToComplete = async () => {
    stopAfterScoreUpdate = true
    await scoreUpdateComplete
}
document.querySelector('#welcomeProceedToLogin').addEventListener('click', async () => {
    const isValidUrl = url => {
        try {
            return new URL(url).href === url
        } catch {
            return false
        }
    }

    serverAddress = document.querySelector('#welcomeServerAddress').value
    authServerAddress = document.querySelector('#welcomeAuthServerAddress').value

    if (!isValidUrl(`http://${serverAddress}:13950/`)) {
        await ModalService.alert('Server Address Invalid', 'Server address is invalid!')
        return
    } else if (!isValidUrl(`http://${authServerAddress}:13949/`)) {
        await ModalService.alert('Auth Server Address Invalid', 'Auth server address is invalid!')
        return
    }

    const userTrustsServers = await ModalService.confirm(
        'Are you sure?',
        `Only proceed if you absolutely trust these servers:\n- ${serverAddress}:13950\n- ${authServerAddress}:13949`
    )

    if (!userTrustsServers) {
        return
    }

    document.querySelector('#loginLoggingInTo').innerText = authServerAddress + ':13949'
    AnimationService.animateForward(
        document.querySelector('#welcome'),
        document.querySelector('#login')
    )
})

document.querySelector('#loginLogin').addEventListener('click', () => {
    const username = document.querySelector('#loginUsername').value
    const password = document.querySelector('#loginPassword').value

    document.querySelector('#loginPassword').value = null

    LoadingService.startLoading()

    fetch(`http://${authServerAddress}:13949/api/client/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username,
            password
        })
    }).then(response => {
        response.json().then(async ({ token, message }) => {
            if (response.ok) {
                try {
                    LoadingService.startLoading()

                    loginToken = token

                    await ClientService.init(serverAddress)
                } catch ({ message }) {
                    ModalService.alert('Login Failed', message)
                }
            } else {
                ModalService.alert('Login Failed', message)
            }

            LoadingService.stopLoading()
        })
    }).catch(({ message }) => {
        ModalService.alert('Login Failed', message)
        LoadingService.stopLoading()
    })
})
document.querySelector('#loginBack').addEventListener('click', () => {
    document.querySelector('#loginUsername').value = null
    document.querySelector('#loginPassword').value = null
    AnimationService.animateBackward(
        document.querySelector('#login'),
        document.querySelector('#welcome')
    )
})
document.querySelector('#waitingToAttachAttach').addEventListener('click', () => {
    LoadingService.startLoading('Make sure Pong C is not waiting at the main menu.')
    ClientService.attach()
})
document.querySelector('#waitingToAttachLogout').addEventListener('click', () => {
    LoadingService.startLoading()
    ClientService.disconnect()
})
document.querySelector('#controlPanelChangeLeftScore').addEventListener('click', async () => {
    LoadingService.startLoading()

    await waitForScoreUpdateToComplete()

    LoadingService.stopLoading()

    const newScore = await ModalService.changeScorePrompt('left')
    if (newScore) {
        LoadingService.startLoading()
        ClientService.changeLeftScore(newScore)
    } else {
        ClientService.getLeftScore()
    }
})
document.querySelector('#controlPanelChangeRightScore').addEventListener('click', async () => {
    LoadingService.startLoading()

    await waitForScoreUpdateToComplete()

    LoadingService.stopLoading()

    const newScore = await ModalService.changeScorePrompt('Right')
    if (newScore) {
        LoadingService.startLoading()
        ClientService.changeRightScore(newScore)
    } else {
        ClientService.getLeftScore()
    }
})
document.querySelector('#controlPanelDetach').addEventListener('click', async () => {
    LoadingService.startLoading()

    await waitForScoreUpdateToComplete()

    ClientService.detach()
})

ClientService.events.on('initialized', () => {
    ClientService.login(loginToken)
})
ClientService.events.on('authenticated', () => {
    document.querySelector('#loginUsername').value = null
    document.querySelector('#waitingToAttachLoggedInOn').innerText = serverAddress + ':13950'
    AnimationService.animateForward(
        document.querySelector('#login'),
        document.querySelector('#waitingToAttach')
    )
})
ClientService.events.on('attached', async error => {
    if (error) {
        LoadingService.stopLoading()
        ModalService.alert('Attach Failed', error)
    } else {
        ClientService.getLeftScore()
        await scoreUpdateComplete
        LoadingService.stopLoading()
        AnimationService.animateForward(
            document.querySelector('#waitingToAttach'),
            document.querySelector('#controlPanel')
        )
    }
})
ClientService.events.on('leftScore', async (error, score) => {
    if (error) {
        await ModalService.alert('Update Scores Failed', error)
        LoadingService.stopLoading()
        AnimationService.animateBackward(
            document.querySelector('#controlPanel'),
            document.querySelector('#waitingToAttach')
        )
    } else {
        document.querySelector('#controlPanelLeftScore').innerText = score
        ClientService.getRightScore()
    }
})
ClientService.events.on('rightScore', async (error, score) => {
    if (error) {
        await ModalService.alert('Update Scores Failed', error)
        LoadingService.stopLoading()
        AnimationService.animateBackward(
            document.querySelector('#controlPanel'),
            document.querySelector('#waitingToAttach')
        )
    } else {
        document.querySelector('#controlPanelRightScore').innerText = score

        scoreUpdateCompleteResolve()
        scoreUpdateComplete = new Promise(resolve => {
            scoreUpdateCompleteResolve = resolve
        })

        if (stopAfterScoreUpdate) {
            stopAfterScoreUpdate = false
        } else {
            ClientService.getLeftScore()
        }
    }
})
ClientService.events.on('scoreChanged', async error => {
    if (error) {
        await ModalService.alert('Change Score Failed', error)
        LoadingService.stopLoading()
        AnimationService.animateBackward(
            document.querySelector('#controlPanel'),
            document.querySelector('#waitingToAttach')
        )
    } else {
        LoadingService.stopLoading()
        ClientService.getLeftScore()
    }
})
ClientService.events.on('detached', () => {
    LoadingService.stopLoading()
    AnimationService.animateBackward(
        document.querySelector('#controlPanel'),
        document.querySelector('#waitingToAttach')
    )
})
ClientService.events.on('disconnected', async reason => {
    LoadingService.stopLoading()

    if (reason) {
        await ModalService.alert('Disconnected', reason)
    }

    const from = document.querySelector('.view:not([hidden])')

    if (from.id === 'login') {
        return
    }

    AnimationService.animateBackward(
        from,
        document.querySelector('#login')
    )
})
