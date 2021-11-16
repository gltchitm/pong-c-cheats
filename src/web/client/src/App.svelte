<script lang="ts">
    import Welcome from './views/Welcome.svelte'
    import Greeting from './views/Greeting.svelte'
    import Attaching from './views/Attaching.svelte'
    import EnterToken from './views/EnterToken.svelte'
    import ControlPanel from './views/ControlPanel.svelte'
    import Authenticating from './views/Authenticating.svelte'

    import { View, view } from './ts/view'
    import { error, errorMessage } from './ts/error'
    import { leftScore, rightScore } from './ts/score'
    import { ServerPackets, DaemonPackets, Communicator, communicator } from './ts/communicator'

    let tokenIsIncorrect = false

    const greet = async () => {
        $view = View.Greeting

        $communicator = new Communicator()

        try {
            await $communicator.waitForOpen()
        } catch {
            error('Cannot connect to the server.')
            return
        }

        switch ((await $communicator.hello()).id) {
            case ServerPackets.ClientboundPackets.Ok:
                $view = View.EnterToken
                break
            case ServerPackets.ClientboundPackets.Busy:
                error('The daemon is busy.')
                break
            default:
                error('Received an unexpected response packet.')
                break
        }
    }

    const attach = async () => {
        switch ((await $communicator.attach()).error) {
            case DaemonPackets.ClientboundPackets.Ok:
                const leftScorePacket = await $communicator.getLeftScore()
                const rightScorePacket = await $communicator.getRightScore()

                if (
                    leftScorePacket.error !== DaemonPackets.ClientboundPackets.LeftScore ||
                    rightScorePacket.error !== DaemonPackets.ClientboundPackets.RightScore
                ) {
                    error('An error occurred fetching the initial scores. Pong C Cheats Web will now disconnect.')
                } else {
                    $leftScore = leftScorePacket.score
                    $rightScore = rightScorePacket.score
                    $view = View.ControlPanel
                }
                break
            case DaemonPackets.ClientboundPackets.Busy:
                error('The daemon is busy.')
                break
            case DaemonPackets.ClientboundPackets.NotFound:
                error('Make sure Pong C is running.')
                break
            default:
                error('Received an unexpected response packet.')
                break
        }
    }

    const authenticate = async (token: string) => {
        $view = View.Authenticating

        switch ((await $communicator.authenticate(token)).id) {
            case ServerPackets.ClientboundPackets.Ok:
                $view = View.Attaching
                attach()
                break
            case ServerPackets.ClientboundPackets.IncorrectToken:
                tokenIsIncorrect = true
                $view = View.EnterToken
                break
            case ServerPackets.ClientboundPackets.TooManyAttempts:
                error('The server shut down because of too many incorrect tokens. You must manually restart it.')
                break
            default:
                error('Received an unexpected response packet.')
                break
        }
    }
</script>


<main class="bg-gray-800 text-gray-200 h-screen w-screen flex items-center justify-center flex-col">
    {#if $view === View.Welcome}
        <Welcome on:attach={greet} />
    {:else if $view === View.Greeting}
        <Greeting />
    {:else if $view === View.Authenticating}
        <Authenticating />
    {:else if $view === View.Attaching}
        <Attaching />
    {:else if $view === View.ControlPanel}
        <ControlPanel />
    {/if}
</main>

{#if $errorMessage}
    <div class="fixed h-screen w-screen animated top-0 left-0 flex items-center justify-center z-10">
        <div class="absolute bg-black opacity-50 h-full w-full z-0"></div>
        <div class="max-w-lg w-full p-5 relative bg-white mx-auto my-auto rounded-lg">
            <div class="text-center p-5 flex-auto justify-center">
                <h2 class="text-xl font-bold pb-4">Error</h2>
                <p class="text-md text-gray-500">{$errorMessage}</p>
            </div>
            <div class="pt-1 pb-3 text-center">
                <button
                    class="
                        bg-blue-500
                        text-white
                        hover:bg-blue-700
                        font-bold
                        py-2
                        px-4
                        rounded-md
                        shadow-md
                        hover:shadow-sm
                        transition
                        focus:ring
                        ring-blue-300
                        mr-3
                    "
                    on:click={() => window.location.reload()}
                >
                    Close
                </button>
            </div>
        </div>
    </div>
{/if}

{#if $view === View.EnterToken}
    <EnterToken {tokenIsIncorrect} on:submit={({ detail }) => authenticate(detail)} />
{/if}
