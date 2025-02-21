import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import Big from 'big.js';
import * as t from 'io-ts'
import { StockFormat, StockQuality } from '../utils/types';
import * as m from 'async-mutex'
import { catchError, from, map, mergeMap, Observable, of, share } from 'rxjs';
import { ensureValid } from '../ext/io-ts.ext';

const API_CHECKOUT_URL = '/api/order/place'

export interface CreditCardPaymentStrategy {
  type: 'credit_card';
  number: string;
  validMonth: number;
  validYear: number;
  address: string;
  tenant: string;
  csc: number;
}

export interface CreditCardPaymentStrategy {
  type: 'credit_card';
  number: string;
  validMonth: number;
  validYear: number;
  address: string;
  tenant: string;
  csc: number;
}

export type PaymentStrategy = CreditCardPaymentStrategy;

export type CheckoutItem = {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
  priceEur: Big;
}

interface CheckoutItemRequestPart {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
  priceEur: string;
}

export interface CheckoutAddress {
  country: string;
  state: string;
  city: string;
  street: string;
  zipCode: string;
  recipientName: string;
}

function checkoutItemToRequestPart(item: CheckoutItem): CheckoutItemRequestPart {
  return {
    isbn: item.isbn,
    stockFormat: item.stockFormat,
    stockQuality: item.stockQuality,
    quantity: item.quantity,
    priceEur: item.priceEur.toFixed(2)
  }
}

export type CheckoutResult = {
  type: 'success';
} | {
  type: 'success_but_cart_not_cleared'
} | {
  type: 'not_enough_stocks';
} | {
  type: 'other_error';
} | {
  type: 'stock_not_on_sale'
} | {
  type: 'items_price_changed'
} | {
  type: 'shipment_cost_changed'
}

const CheckoutResult = t.type({
  cartCleared: t.boolean
})

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {
  constructor(
    private readonly httpClient: HttpClient
  ) { }

  private mutex: m.Mutex = new m.Mutex();

  private _checkingOut = false;

  public get checkingOut() {
    return this._checkingOut
  }

  public checkout(items: CheckoutItem[], paymentStrategy: PaymentStrategy, address: CheckoutAddress, shipmentCost: Big, cartVersion: number) : Observable<CheckoutResult> {
    return from(this.mutex.acquire().then(release => {
      this._checkingOut = true

      const observable = this.httpClient.post(API_CHECKOUT_URL, {
        paymentStrategy,
        items: items.map(checkoutItemToRequestPart),
        address,
        shipmentCost: shipmentCost.toFixed(2),
        cartVersion
      })
      .pipe(
        map((r) => {
          const checkoutResult = ensureValid(CheckoutResult.decode(r))
          if (checkoutResult.cartCleared) {
            return { type: 'success' as const } as CheckoutResult
          } else {
            return { type: 'success_but_cart_not_cleared' as const } as CheckoutResult
          }
        }),
        catchError(({ error }) => {
          if (error.code === 'NOT_ENOUGH_ITEMS_IN_STOCK') {
            return of({ type: 'not_enough_stocks' as const } as CheckoutResult)
          } else if (error.code === 'BOOK_NOT_FOUND' || error.code === 'STOCK_NOT_ON_SALE') {
            return of({ type: 'stock_not_on_sale' as const } as CheckoutResult)
          } else if (error.code === 'ITEMS_PRICE_CHANGED') {
            return of({ type: 'items_price_changed' as const } as CheckoutResult)
          } else if (error.status === 'SHIPMENT_COST_CHANGED') {
            return of({ type: 'shipment_cost_changed' as const } as CheckoutResult)
          } else {
            return of({ type: 'other_error' as const } as CheckoutResult)
          }
        }),
        share()
      )

      observable.subscribe({
        complete: () => {
          this._checkingOut = false
          release()
        },
        error: () => {
          this._checkingOut = false
          release()
        }
      })

      return observable
    })).pipe(mergeMap(x => x))
  }
}
