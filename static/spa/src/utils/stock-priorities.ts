import { StockFormat, StockQuality } from "./types"

import * as t from "io-ts"

type StockQualityT = t.TypeOf<typeof StockQuality>
type StockFormatT = t.TypeOf<typeof StockFormat>

export const StockQualityPriority: { [key in StockQualityT]: number } = {
    'new': 0,
    'like-new': 1,
    'very-good': 2,
    'good': 3,
    'acceptable': 4,
    'worn': 5
}

export const StockFormatPriority: { [key in StockFormatT]: number } = {
    'hardcover': 0,
    'paperback': 1,
    'ebook': 2
}

export function cmpQualityByPriority(q1: StockQualityT, q2: StockQualityT): number {
  return StockQualityPriority[q1] - StockQualityPriority[q2]
}

export function cmpFormatByPriority(f1: StockFormatT, f2: StockFormatT): number {
  return StockFormatPriority[f1] - StockFormatPriority[f2]
}

