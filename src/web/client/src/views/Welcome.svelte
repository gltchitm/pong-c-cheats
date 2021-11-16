<script>
    import { createEventDispatcher } from 'svelte'

    const dispatch = createEventDispatcher()
</script>

<h1 class="text-5xl mb-3 text-center">Pong C Cheats</h1>
<p class="text-xl mb-4 text-center">Start Pong C and press Attach</p>
<button
    class="
        bg-gray-600
        hover:bg-gray-700
        font-bold
        py-2
        px-4
        rounded
        shadow-md
        hover:shadow-sm
        transition
        focus:ring
        ring-gray-500
    "
    on:click={() => dispatch('attach')}
>
    Attach
</button>
