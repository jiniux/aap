import { StockFormat, StockQuality } from "./types";

import * as t from "io-ts"

type StockQualityT = t.TypeOf<typeof StockQuality>

export type EmojiNameWithColor = { emojiName : string, color: string }

export function emojiFromStockQuality(q: StockQualityT): EmojiNameWithColor {
    switch (q) {
        case "new": return { emojiName: "star", color: "darkgreen" };
        case "like-new": return { emojiName: "smile", color: "green" };
        case "very-good": return { emojiName: "smile", color: "lightgreen" };
        case "good": return { emojiName: "smile", color: "yellow" };
        case "acceptable": return { emojiName: "meh", color: "orange" };
        case "worn": return { emojiName: "frown", color: "red" };
    }
}