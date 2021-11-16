<script lang="ts">
    import { onMount } from 'svelte'

    import { error } from '../ts/error'
    import { leftScore, rightScore } from '../ts/score'
    import { communicator, DaemonPackets } from '../ts/communicator'

    let changingScoreFor = null
    let score = 0
    let scoreIsInvalid = false

    const changeLeftScore = async () => {
        if (score && score > 0 && score <= 999_999) {
            await $communicator.changeLeftScore(score)
            score = 0
            changingScoreFor = null
        } else {
            scoreIsInvalid = true
        }
    }
    const changeRightScore = async () => {
        if (score && score > 0 && score <= 999_999) {
            await $communicator.changeRightScore(score)
            score = 0
            changingScoreFor = null
        } else {
            scoreIsInvalid = true
        }
    }
    const detach = async () => {
        await $communicator.detach()
        window.location.reload()
    }

    onMount(() => {
        const updateScores = async () => {
            if (!changingScoreFor) {
                const leftScorePacket = await $communicator.getLeftScore()
                const rightScorePacket = await $communicator.getRightScore()

                if (
                    leftScorePacket.error === DaemonPackets.ClientboundPackets.NotAttached ||
                    rightScorePacket.error === DaemonPackets.ClientboundPackets.NotAttached
                ) {
                    error('The daemon lost connection to Pong C. Pong C Cheats Web will now disconnect.')
                } else if (
                    leftScorePacket.error !== DaemonPackets.ClientboundPackets.LeftScore ||
                    rightScorePacket.error !== DaemonPackets.ClientboundPackets.RightScore
                ) {
                    error('An error occurred when refreshing scores. Pong C Cheats Web will now disconnect.')
                } else {
                    $leftScore = leftScorePacket.score
                    $rightScore = rightScorePacket.score
                    setTimeout(updateScores, 50)
                }
            } else {
                setTimeout(updateScores, 50)
            }
        }

        updateScores()
    })
</script>

<div class="flex mb-5">
    <div class="mr-5 flex align-items-center flex-col">
        <h2 class="text-2xl text-center font-bold">Left Player Score</h2>
        <p class="text-lg text-center mb-2">{$leftScore}</p>
        <button
            class="bg-gray-600 hover:bg-gray-700 font-bold py-1 px-2 rounded transition focus:ring ring-gray-500"
            on:click={() => changingScoreFor = 'left'}
        >
            Change Score
        </button>
    </div>
    <div class="flex align-items-center flex-col">
        <h2 class="text-2xl text-center font-bold">Right Player Score</h2>
        <p class="text-lg text-center mb-2">{$rightScore}</p>
        <button
            class="bg-gray-600 hover:bg-gray-700 font-bold py-1 px-2 rounded transition focus:ring ring-gray-500"
            on:click={() => changingScoreFor = 'right'}
        >
            Change Score
        </button>
    </div>
</div>
<button
    class="bg-gray-600 hover:bg-gray-700 font-bold py-1 px-2 rounded transition focus:ring ring-gray-500"
    on:click={detach}
>
    Detach
</button>
{#if changingScoreFor}
    <div class="fixed h-screen w-screen animated top-0 left-0 flex items-center justify-center z-10 text-black">
        <div class="absolute bg-black opacity-50 h-full w-full z-0"></div>
        <div class="max-w-lg w-full p-5 relative bg-white mx-auto my-auto rounded-lg">
            <div class="text-center p-5 flex-auto justify-center">
                <h2 class="text-xl font-bold pb-4">
                    Change {changingScoreFor[0].toUpperCase() + changingScoreFor.slice(1)} Score
                </h2>
                <p class="text-md text-gray-500 pb-3">
                    Enter the new score for the {changingScoreFor} player.
                </p>
                <div>
                    <input
                        type="number"
                        placeholder="Score"
                        class="border shadow py-2 rounded-md text-center focus:ring ring-blue-300 outline-none"
                        max="999999"
                        min="0"
                        bind:value={score}
                    />
                    {#if scoreIsInvalid}
                        <small class="pt-1 block text-red-600">Score is invalid.</small>
                    {/if}
                </div>
            </div>
            <div class="p-3 text-center">
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
                    on:click={() => changingScoreFor === 'left' ? changeLeftScore() : changeRightScore()}
                >
                    Continue
                </button>
                <button
                    class="
                        bg-red-600
                        text-white
                        hover:bg-red-800
                        font-bold
                        py-2
                        px-4
                        rounded-md
                        shadow-md
                        hover:shadow-sm
                        transition
                        focus:ring
                        ring-red-400
                    "
                    on:click={() => changingScoreFor = null}
                >
                    Cancel
                </button>
            </div>
        </div>
    </div>
{/if}
