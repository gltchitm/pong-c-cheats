<script lang="ts">
    import { createEventDispatcher } from 'svelte'

    export let tokenIsIncorrect: boolean

    const dispatch = createEventDispatcher()

    let token: string
</script>

<div class="fixed h-screen w-screen animated top-0 left-0 flex items-center justify-center z-10">
    <div class="absolute bg-black opacity-50 h-full w-full z-0"></div>
    <div class="max-w-lg w-full p-5 relative bg-white mx-auto my-auto rounded-lg">
        <div class="text-center p-5 flex-auto justify-center">
            <h2 class="text-xl font-bold pb-4">Attach to Pong C</h2>
            <p class="text-md text-gray-500 pb-3">
                Enter the code displayed on screen to attach to Pong C.
            </p>
            <div>
                <input
                    type="text"
                    placeholder="Token"
                    class="border shadow py-2 rounded-md text-center focus:ring ring-blue-300 outline-none"
                    maxlength="6"
                    bind:value={token}
                />
                {#if tokenIsIncorrect}
                    <small class="pt-1 block text-red-600">Incorrect token.</small>
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
                on:click={() => dispatch('submit', token)}
            >
                Continue
            </button>
        </div>
    </div>
</div>
