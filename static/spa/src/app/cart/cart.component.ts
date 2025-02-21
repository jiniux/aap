import { Component, OnInit, OnDestroy } from '@angular/core';
import { CartItem, CartService } from '../cart.service';
import { concatMap, from, map, mergeMap, Observable, share, shareReplay, Subscription, toArray } from 'rxjs';
import { FullCartItem } from '../cart-item/cart-item.component';
import { BookStocks, CatalogService, FullCatalogBookResult } from '../catalog.service';
import * as t from "io-ts"
import { StockFormat, StockQuality } from '../../utils/types';
import Big from 'big.js';

function getSuitableFormatPreviewImageFromFormat(
  format: t.TypeOf<typeof StockFormat>,
  formatPreviewImages: { url: string, format: t.TypeOf<typeof StockFormat> }[]
): string | undefined {
  const formatPreviewImagesByFormat = formatPreviewImages.filter((f) => f.format === format);

  if (formatPreviewImagesByFormat.length > 0) {
    return formatPreviewImagesByFormat[0].url;
  }

  return formatPreviewImages[0]?.url;
}

function findPricingByFormatAndQuality(
  format: t.TypeOf<typeof StockFormat>,
  quality: t.TypeOf<typeof StockQuality>,
  stocks: t.TypeOf<typeof BookStocks>
): Big | undefined {
  const stock = stocks.find((s) => s.format === format && s.quality === quality);
  return stock?.priceEur;
}


@Component({
  selector: 'app-cart',
  standalone: false,
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit, OnDestroy {
  items: FullCartItem[] = [];
  private subscription!: Subscription;

  constructor(
    private readonly cartService: CartService,
    private readonly catalogService: CatalogService
  ) { }

  ngOnInit(): void {
    this.cartService.reloadCart();

    this.subscription = this.cartService.itemsUpdate.pipe(
      mergeMap(items => {
        const bookCache: { [key: string]: Observable<FullCatalogBookResult> } = {}

        return from(items).pipe(
          mergeMap(i => {
            if (!bookCache[i.isbn]) {
              bookCache[i.isbn] = this.catalogService.getBook(i.isbn).pipe(share());
            }

            return bookCache[i.isbn].pipe(
              map(book => ({ item: i, book }))
            );
          }),
          toArray()
        )
      }
      )
    ).subscribe({
      next: (books) => {
        this.items = books.map(({ item, book }) => ({
          authorNames: book.authors.map(a => `${a.firstName} ${a.lastName}`).join(", "),
          coverUrl: getSuitableFormatPreviewImageFromFormat(item.stockFormat, book.formatPreviewImages) ?? "",
          isbn: item.isbn,
          pricing: "€" + item.priceEur.toFixed(2),
          quantity: item.quantity,
          stockFormat: item.stockFormat,
          stockQuality: item.stockQuality,
          title: book.title,
        }))

        this.items.sort((a, b) => a.title.localeCompare(b.title));
      },
      error: (error) => {
        console.error("Error loading cart items", error);
      }
    })
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
