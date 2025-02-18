import { isLeft, isRight } from "fp-ts/lib/Either"
import { PathReporter } from 'io-ts/lib/PathReporter'
import * as t from "io-ts"

export class ValidationError extends Error {
    constructor(public readonly errors: t.Errors) {
        super(`Validation failed: ${PathReporter.report(t.failures(errors)).join(", ")}`)
    }
}

export function ensureValid<A>(t: t.Validation<A>) {
    if (isRight(t)) {
        return t.right
    } else {
        throw new ValidationError(t.left)
    }
}