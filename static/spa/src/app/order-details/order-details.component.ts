import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../order.service';
import { Observable, Subscription, catchError, filter, map, merge, of, share, shareReplay, startWith, switchMap } from 'rxjs';
import { TypeOf } from 'io-ts';
import { FullOrderResult } from '../order.service';
import { DateTime } from 'luxon';
import { TranslateService } from '@ngx-translate/core';
import { StockFormat, StockQuality } from '../../utils/types';
import * as t from 'io-ts';
import { CatalogService, FullCatalogBookResult } from '../catalog.service';
import { getSuitableFormatPreviewImageFromFormat } from '../../utils/most-suitable-preview-image';
import { emojiFromStockQuality } from '../../utils/emoji-from-stock-quality';

type PaymentAdditionalDetails = {
  type: 'credit-card',
  data: {
    number: string,
    tenant: string,
    expiration: string
  }
} | {
  type: 'none'
}


export interface FullOrderItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>
  stockQuality: t.TypeOf<typeof StockQuality>
  quantity: number;
  title: string;
  authorNames: string;
  pricing: string;
  coverUrl: string
}

interface PartialOrderItem {
  isbn: string,
  stockFormat: t.TypeOf<typeof StockFormat>,
  stockQuality: t.TypeOf<typeof StockQuality>,
  quantity: number,
  priceEur: string
}
@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrl: './order-details.component.css',
  standalone: false
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
  public state: {
    type: 'loading'
  } | {
    type: 'error'
  } | {
    type: 'loaded',
    data: TypeOf<typeof FullOrderResult>,
    normalizedDate: string,
    paymentAdditionalDetails: PaymentAdditionalDetails,
  } = { type: 'loading' }

  public loadedItems: ({ 
    type: "full", 
    data: FullOrderItem,
    emoji: string,
    emojiColor: string
  } | { 
    type: "partial", 
    data: PartialOrderItem,
    emoji: string,
    emojiColor: string
  })[] = []
  
  private subscription: Subscription | null = null
  private loadItemsSubscription: Subscription | null = null

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private translateService: TranslateService,
    private catalogService: CatalogService
  ) {}
  
  ngOnDestroy(): void {
    this.subscription?.unsubscribe()
    this.loadItemsSubscription?.unsubscribe();
  }

  private updateBookDetails(items: t.TypeOf<typeof FullOrderResult>['items']) {
    this.loadedItems = []
    this.loadItemsSubscription?.unsubscribe();

    const bookCache: Map<string, Observable<FullCatalogBookResult | null>> = new Map();

    const observables = items.map(item => {
      if (!bookCache.has(item.isbn)) {
        const book$ = this.catalogService.getBook(item.isbn).pipe(
          share(),
          catchError(error => { 
            console.log(error);
            return of(null);
          })
        );
        bookCache.set(item.isbn, book$);
      }

      return bookCache.get(item.isbn)!.pipe(
        map(book => ({ data: book, item })),
      );
    });
    
    this.loadItemsSubscription = merge(...observables).subscribe({
      next: result => {
        const { emojiName, color } = emojiFromStockQuality(result.item.stockQuality);

        if (result.data === null) {
          this.loadedItems.push({
            type: 'partial',
            data: {
              isbn: result.item.isbn,
              stockFormat: result.item.stockFormat,
              stockQuality: result.item.stockQuality,
              quantity: result.item.quantity,
              priceEur: result.item.priceEur.toFixed(2)
            },
            emoji: emojiName,
            emojiColor: color
          });

          return
        } 

        this.loadedItems.push({
          type: 'full',
          data:  {
            isbn: result.item.isbn,
            stockFormat: result.item.stockFormat,
            stockQuality: result.item.stockQuality,
            quantity: result.item.quantity,
            title: result.data.title,
            authorNames: result.data.authors.map(author => `${author.firstName} ${author.lastName}`).sort().join(', '),
            pricing: result.item.priceEur.toFixed(2),
            coverUrl: getSuitableFormatPreviewImageFromFormat(result.item.stockFormat, result.data.formatPreviewImages) ?? ''
          },
          emoji: emojiName,
          emojiColor: color
        })
      },
      complete: () => {
        this.loadedItems.sort((a, b) => a.data.isbn.localeCompare(b.data.isbn));
      }
    })
  }

  ngOnInit() {
    this.subscription = this.route.params.pipe(
      switchMap(params => this.orderService.getOrder(params['id']).pipe(
        map(order => ({ type: 'loaded' as const, data: order })),
        catchError(error => { console.log(error); return of({ 
          type: 'error' as const, 
        })}),
        startWith({ type: 'loading' as const })
      ))
    ).subscribe({
      next: state => {
        if (state.type === 'loaded') {
          let paymentAdditionalDetails: PaymentAdditionalDetails = { type: 'none' }

          if (state.data.payment.method === 'credit-card') {
            paymentAdditionalDetails = {
              type: 'credit-card',
              data: {
                number: state.data.payment.additionalInfo.number,
                expiration: state.data.payment.additionalInfo.expiration,
                tenant: state.data.payment.additionalInfo.tenant
              }
            }
          }
          
          this.state = {
            type: 'loaded',
            data: state.data,
            normalizedDate: state.data.placeAt.setLocale(this.translateService.currentLang).toLocal().toLocaleString({ dateStyle: "full", timeStyle: "short" }),
            paymentAdditionalDetails
          }

          this.updateBookDetails(state.data.items);
        }
      },
      error: error => this.state = { type: 'error' }
    });
  }
}
