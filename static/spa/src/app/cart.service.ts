import { Injectable, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, Subject } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import * as t from 'io-ts';
import { API_URL } from '../constants';
import { ensureValid } from '../ext/io-ts.ext';
import { PriceEur, StockFormat, StockQuality } from '../utils/types';

import * as m from 'async-mutex'
import _, { join } from 'lodash';

interface AddItemToShoppingCartRequestItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
}

interface AddItemToShoppingCartRequest {
  item: AddItemToShoppingCartRequestItem;
}

const SyncShoppingCartResultItem = t.type({
  isbn: t.string,
  stockFormat: StockFormat,
  stockQuality: StockQuality,
  quantity: t.number
});

type SyncShoppingCartResultItem = t.TypeOf<typeof SyncShoppingCartResultItem>;

const SyncShoppingCartResultRemovedItem = t.type({
  isbn: t.string,
  stockFormat: StockFormat,
  stockQuality: StockQuality
});

type SyncShoppingCartResultRemovedItem = t.TypeOf<typeof SyncShoppingCartResultRemovedItem>;

const SyncShoppingCartResultPriceChangedItem = t.type({
  isbn: t.string,
  stockFormat: StockFormat,
  stockQuality: StockQuality,
  oldPrice: PriceEur,
  newPrice: PriceEur
});

type SyncShoppingCartResultPriceChangedItem = t.TypeOf<typeof SyncShoppingCartResultPriceChangedItem>;

const SyncShoppingCartResult = t.type({
  items: t.array(SyncShoppingCartResultItem),
  removedItems: t.array(SyncShoppingCartResultRemovedItem),
  priceChangedItems: t.array(SyncShoppingCartResultPriceChangedItem),
});

type SyncShoppingCartResult = t.TypeOf<typeof SyncShoppingCartResult>;

const API_SHOPPING_CART_ADD_ITEM_URL = `${API_URL}/shopping-cart/action/add-item`;
const API_SHOPPING_CART_REMOVE_ITEM_URL = `${API_URL}/shopping-cart/action/remove-item`;

interface CartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
}

interface CartItemKey {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
}

interface RemovedCartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
}

interface PriceChangedCartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  oldPrice: t.TypeOf<typeof PriceEur>;
  newPrice: t.TypeOf<typeof PriceEur>;
}

type AddCartItemResult = {
  type: 'success'
} | {
  type: 'not_enough_stocks'
} | {
  type: 'unkown_error'
}

const LOCAL_STORAGE_CART_ITEMS_KEY = 'cartItems';

type CartEvent = {
  type: 'items-added',
  item: Readonly<CartItem[]>
} | {
  type: 'items-removed',
  item: Readonly<RemovedCartItem[]>
} | {
  type: 'items-price-changed',
  item: Readonly<PriceChangedCartItem[]>
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  constructor(private http: HttpClient) {
    this.loadCartFromLocalStorage();
  }

  private items: CartItem[] = [];

  private subject: Subject<CartEvent> = new Subject();

  public get events() { return this.subject.asObservable(); }

  private priceChangedItems: { [key: string]: { oldPrice: Big.Big, newPrice: Big.Big } } = {}
  private removedItems: Set<string> = new Set()

  private mutex = m
  private itemsBeingAdded: Set<string> = new Set()

  private loadCartFromLocalStorage() {
    const items = localStorage.getItem(LOCAL_STORAGE_CART_ITEMS_KEY);
    if (items !== null) {
      this.items = JSON.parse(items);
    }
  }

  private saveCartToLocalStorage() {
    localStorage.setItem(LOCAL_STORAGE_CART_ITEMS_KEY, JSON.stringify(this.items));
  }

  public isItemBeingAdded(itemKey: CartItemKey): boolean {
    return this.itemsBeingAdded.has(join([itemKey.isbn, itemKey.stockFormat, itemKey.stockQuality], ','))
  }

  public reloadCart() {
  
  }

  public addBookToCart(item: CartItem): Observable<AddCartItemResult> {
    const request: AddItemToShoppingCartRequest = {
      item: {
        isbn: item.isbn,
        stockFormat: item.stockFormat,
        stockQuality: item.stockQuality,
        quantity: item.quantity
      }
    };

    const key = join([item.isbn, item.stockFormat, item.stockQuality], ',')

    this.itemsBeingAdded.add(key)

    return this.http.post<AddCartItemResult>(API_SHOPPING_CART_ADD_ITEM_URL, request).pipe(
      map(res => {
        const result = ensureValid(SyncShoppingCartResult.decode(res));

        this.processNewItems(result.items);
        this.processRemovedItems(result.removedItems);
        this.processPriceChangedItems(result.priceChangedItems);

        this.saveCartToLocalStorage();
        
        this.itemsBeingAdded.delete(key)

        return { type: 'success' as const };
      }),
      catchError(({error}) => {
        this.itemsBeingAdded.delete(key)

        if (error !== undefined) {
          if (error.code === 'STOCK_QUANTITY_NOT_AVAILABLE') {
            return of({ type: 'not_enough_stocks' as const });
          }
        }
        
        return of({ type: 'unkown_error' as const });
      })
    );
  }

  private processNewItems(newItems: SyncShoppingCartResultItem[]) {
    this.subject.next({ type: 'items-added', item: newItems });
    this.items = newItems
  }

  private processRemovedItems(removedItems: SyncShoppingCartResultRemovedItem[]) {
    this.subject.next({ type: 'items-removed', item: removedItems });

    removedItems.forEach(removedItem => {
      this.removedItems.add(removedItem.isbn);
    })
  }

  private processPriceChangedItems(priceChangedItems: SyncShoppingCartResultPriceChangedItem[]) {
    priceChangedItems.forEach(priceChangedItem => {
      const key = join([priceChangedItem.isbn, priceChangedItem.stockFormat, priceChangedItem.stockQuality], ',')
      const mappedPriceChangedItem = this.priceChangedItems[key]

      if (mappedPriceChangedItem === undefined) {
        this.priceChangedItems[key] = { oldPrice: priceChangedItem.oldPrice, newPrice: priceChangedItem.newPrice }
      } else {
        mappedPriceChangedItem.newPrice = priceChangedItem.newPrice
      }
    })
  }
}
