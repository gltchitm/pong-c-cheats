import css from 'rollup-plugin-css-only'
import svelte from 'rollup-plugin-svelte'
import { terser } from 'rollup-plugin-terser'

import commonjs from '@rollup/plugin-commonjs'
import resolve from '@rollup/plugin-node-resolve'
import typescript from '@rollup/plugin-typescript'

import sveltePreprocess from 'svelte-preprocess'

export default {
    input: 'src/main.ts',
    output: {
        sourcemap: false,
        format: 'iife',
        name: 'app',
        file: 'public/build/bundle.js'
    },
    plugins: [
        svelte({
            preprocess: sveltePreprocess({ sourceMap: false }),
            compilerOptions: { dev: false }
        }),
        css({ output: 'bundle.css' }),
        resolve({
            browser: true,
            dedupe: ['svelte']
        }),
        commonjs(),
        typescript({
            sourceMap: false,
            inlineSources: false
        }),
        terser()
    ]
}
