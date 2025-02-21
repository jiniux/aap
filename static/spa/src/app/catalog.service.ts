import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as t from "io-ts"
import { PriceEur, StockFormat, StockQuality } from '../utils/types';
import { map, Observable } from 'rxjs';
import { API_URL } from '../constants';
import { ensureValid } from '../ext/io-ts.ext';

const API_SEARCH_BOOKS = `${API_URL}/books`;
export const BookSearchResultEntry = t.type({
  title: t.string,
  isbn: t.string,
  authors: t.array(
    t.type({
      id: t.string,
      firstName: t.string,
      lastName: t.string
    })
  ),
  publisher: t.type({
    id: t.string,
    name: t.string
  }),
  edition: t.string,
  stocks: t.array(
    t.type({
      format: StockFormat,
      quality: StockQuality,
      priceEur: PriceEur
    })
  ),
  formatPreviewImages: t.array(
    t.type({
      url: t.string,
      format: StockFormat
    })
  )
});

export const BookStocks = t.array(
  t.type({
    format: StockFormat,
    quality: StockQuality,
    priceEur: PriceEur
  })
)

const FullCatalogBookResult = t.type({
  title: t.string,
  isbn: t.string,
  description: t.string,
  publicationYear: t.number,
  edition: t.string,
  authors: t.array(
    t.type({
      id: t.string,
      firstName: t.string,
      lastName: t.string
    })
  ),
  publisher: t.type({
    id: t.string,
    name: t.string
  }),
  stocks: BookStocks,
  formatPreviewImages: t.array(
    t.type({
      url: t.string,
      format: StockFormat
    })
  )
});

export type FullCatalogBookResult = t.TypeOf<typeof FullCatalogBookResult>;

const BookSearchResult = t.array(BookSearchResultEntry);

export type BookSearchResults = t.TypeOf<typeof BookSearchResult>;

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  constructor(private readonly httpClient: HttpClient) { }

  searchBooks(query: string): Observable<BookSearchResults> {
    return this.httpClient.get(API_SEARCH_BOOKS + "?query=" + query, {}).pipe(map((response: any) => {
      return ensureValid(BookSearchResult.decode(response));
    }))
  }

  getBook(isbn: string): Observable<t.TypeOf<typeof FullCatalogBookResult>> {
    return this.httpClient.get(API_SEARCH_BOOKS + "/" + isbn, {}).pipe(map((response: any) => {
      return ensureValid(FullCatalogBookResult.decode(response));
    }))
  }
}
