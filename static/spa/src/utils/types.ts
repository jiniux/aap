import Big from "big.js";
import * as t from "io-ts"

export const StockQuality = t.keyof({
    "new": null,
    "like-new": null,
    "very-good": null,
    "good": null,
    "acceptable": null,
    "worn": null
});

export const StockFormat = t.keyof({
    "hardcover": null,
    "paperback": null,
    "ebook": null
});

export const PriceEur = new t.Type<Big, string, unknown>("PriceEur",
    (input: unknown): input is Big => input instanceof Big,
    (input, context) => {
        if (typeof input !== "string") {
            return t.failure(input, context);
        }
        try {
            return t.success(new Big(input));
        } catch (e) {
            return t.failure(input, context);
        }
    },
    (a) => a.toString());
