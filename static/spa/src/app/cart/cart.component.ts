import { Component, OnInit, OnDestroy } from '@angular/core';
import { CartItem, CartService } from '../cart.service';
import { concatMap, forkJoin, from, map, merge, mergeMap, Observable, share, shareReplay, Subscription, toArray } from 'rxjs';
import { FullCartItem } from '../cart-item/cart-item.component';
import { BookStocks, CatalogService, FullCatalogBookResult } from '../catalog.service';
import * as t from "io-ts"
import { StockFormat, StockQuality } from '../../utils/types';
import Big from 'big.js';
import { getSuitableFormatPreviewImageFromFormat } from '../../utils/most-suitable-preview-image';
import { TranslateService } from '@ngx-translate/core';

function findPricingByFormatAndQuality(
  format: t.TypeOf<typeof StockFormat>,
  quality: t.TypeOf<typeof StockQuality>,
  stocks: t.TypeOf<typeof BookStocks>
): Big | undefined {
  const stock = stocks.find((s) => s.format === format && s.quality === quality);
  return stock?.priceEur;
}

interface PriceChangedNoticeItem {
  title: string
  authors: string
  newPrice: string
  oldPrice: string
  quality: string
  format: string
}


interface ItemRemovedNoticeItem {
  title: string
  authors: string
  quality: string
  format: string
}

@Component({
  selector: 'app-cart',
  standalone: false,
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit, OnDestroy {
  items: FullCartItem[] = [];

  private subscription: Subscription | null = null;
  private priceChangedSubscription: Subscription | null = null;
  private removedItemsSubscription: Subscription | null = null;

  priceChangedNoticeItems: PriceChangedNoticeItem[] = [];
  itemsRemovedNoticeItems: ItemRemovedNoticeItem[] = [];

  constructor(
    private readonly cartService: CartService,
    private readonly catalogService: CatalogService,
    private readonly translateService: TranslateService
  ) { }

  public clearPriceChangedNotice() {
    this.cartService.clearPriceChangedItems();
  }

  public clearRemovedItemsNotice() {
    this.cartService.clearRemovedItems();
  }

  ngOnInit(): void {
    this.cartService.reloadCart();

    this.priceChangedSubscription = this.cartService.priceChangedItemsUpdate.pipe(
      mergeMap(items => {
        const bookCache: { [key: string]: Observable<FullCatalogBookResult> } = {}

        return from(items).pipe(
          mergeMap(i => {
            if (!bookCache[i.isbn]) {
              bookCache[i.isbn] = this.catalogService.getBook(i.isbn).pipe(share());
            }

            const translationsPipe = this.translateService.get(['stock-format.' + i.stockFormat, 'stock-quality.' + i.stockQuality])
              .pipe(
                map(r => ({ format: r['stock-format.' + i.stockFormat], quality: r['stock-quality.' + i.stockQuality] }))
              )

            return forkJoin([bookCache[i.isbn], translationsPipe]).pipe(
              map(book => ({ item: i, book: book[0], translations: book[1] as { [k: string]: string } }))
            );
          }),
          toArray()
        )
      }
      )).subscribe({
        next: (books) => {
          this.priceChangedNoticeItems = books.map(({ item, book, translations }) => {
            return {
              title: book.title,
              authors: book.authors.map(a => `${a.firstName} ${a.lastName}`).sort().join(", "),
              newPrice: item.newPriceEur.toFixed(2),
              oldPrice: item.oldPriceEur.toFixed(2),
              quality: translations["quality"].toLowerCase(),
              format: translations["format"].toLowerCase()
            }
          })

          this.priceChangedNoticeItems.sort((a, b) => a.title.localeCompare(b.title));
        }
      })

    this.removedItemsSubscription = this.cartService.removedItemsUpdate.pipe(
      mergeMap(items => {
        const bookCache: { [key: string]: Observable<FullCatalogBookResult> } = {}

        return from(items).pipe(
          mergeMap(i => {
            if (!bookCache[i.isbn]) {
              bookCache[i.isbn] = this.catalogService.getBook(i.isbn).pipe(share());
            }

            const translationsPipe = this.translateService.get(['stock-format.' + i.stockFormat, 'stock-quality.' + i.stockQuality])
              .pipe(
                map(r => ({ format: r['stock-format.' + i.stockFormat], quality: r['stock-quality.' + i.stockQuality] }))
              )

            return forkJoin([bookCache[i.isbn], translationsPipe]).pipe(
              map(book => ({ item: i, book: book[0], translations: book[1] as { [k: string]: string } }))
            );
          }),
          toArray()
        )
      }
      )).subscribe({
        next: (books) => {
          this.itemsRemovedNoticeItems = books.map(({ item, book, translations }) => {
            return {
              title: book.title,
              authors: book.authors.map(a => `${a.firstName} ${a.lastName}`).sort().join(", "),
              quality: translations["quality"].toLowerCase(),
              format: translations["format"].toLowerCase()
            }
          })

          this.priceChangedNoticeItems.sort((a, b) => a.title.localeCompare(b.title));
        }
      })

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
          authorNames: book.authors.map(a => `${a.firstName} ${a.lastName}`).sort().join(", "),
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
    this.priceChangedSubscription?.unsubscribe();
    this.removedItemsSubscription?.unsubscribe();
  }
}
