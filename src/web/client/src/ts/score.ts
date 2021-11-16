import { writable } from 'svelte/store'

const leftScore = writable(0)
const rightScore = writable(0)

export { leftScore, rightScore }
