import Big from "big.js";
import * as t from "io-ts"

import { DateTime } from "luxon"
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
});

export const OrderState = t.keyof({
    "processing-payment": null,
    "payment-failed": null,
    "confirmed": null,
    "shipped": null,
    "delivered": null,
    "waiting-confirmation": null
});

export const PaymentState = t.keyof({
    "pending": null,
    "completed": null,
    "errored-unknown": null,
    "errored-not-enough-funds": null
})

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

export const ISO8601Date = new t.Type<DateTime, string, unknown>("ISO8601Date",
    (input: unknown): input is DateTime => input instanceof DateTime,
    (input, context) => {
        if (typeof input !== "string") {
            return t.failure(input, context);
        }

        const date = DateTime.fromISO(input)

        if (date.isValid) {
            return t.success(date);
        } else {
            return t.failure(input, context);
        }
    },
    (a) => a.toISO() ?? '');  