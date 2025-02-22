import { StockFormat } from "./types";
import * as t from 'io-ts';

export function getSuitableFormatPreviewImageFromFormat(
    format: t.TypeOf<typeof StockFormat> | null,
    formatPreviewImages: readonly { url: string, format: t.TypeOf<typeof StockFormat> }[]
): string  {
    if (format === null) {
        if (formatPreviewImages.length > 0) {
            return "/api/" + formatPreviewImages[0].url;
        } else {
            return "/assets/book_not_found.jpg";
        }
    }

    const formatPreviewImagesByFormat = formatPreviewImages.filter((f) => f.format === format);

    if (formatPreviewImagesByFormat.length > 0) {
        return "/api/" + formatPreviewImagesByFormat[0].url;
    }

    return "/assets/book_not_found.jpg";
}