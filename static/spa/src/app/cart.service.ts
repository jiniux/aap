import { Injectable, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { from, Observable, of, Subject } from 'rxjs';
import { catchError, map, mergeMap, startWith } from 'rxjs/operators';
import * as t from 'io-ts';
import { API_URL } from '../constants';
import { ensureValid } from '../ext/io-ts.ext';
import { PriceEur, StockFormat, StockQuality } from '../utils/types';

import * as m from 'async-mutex'
import _, { flatMap, join } from 'lodash';
import Big from 'big.js';

interface AddItemToShoppingCartRequestItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
}

interface RemoveItemFromShoppingCartRequestItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
}

interface RemoveItemFromShoppingCartRequest {
  item: RemoveItemFromShoppingCartRequestItem;
}

interface AddItemToShoppingCartRequest {
  item: AddItemToShoppingCartRequestItem;
}

const SyncShoppingCartResultItem = t.type({
  isbn: t.string,
  stockFormat: StockFormat,
  stockQuality: StockQuality,
  quantity: t.number,
  priceEur: PriceEur
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
  oldPriceEur: PriceEur,
  newPriceEur: PriceEur
});

type SyncShoppingCartResultPriceChangedItem = t.TypeOf<typeof SyncShoppingCartResultPriceChangedItem>;

const SyncShoppingCartResult = t.type({
  items: t.array(SyncShoppingCartResultItem),
  removedItems: t.array(SyncShoppingCartResultRemovedItem),
  priceChangedItems: t.array(SyncShoppingCartResultPriceChangedItem),
  version: t.number
});

type SyncShoppingCartResult = t.TypeOf<typeof SyncShoppingCartResult>;

const API_SHOPPING_CART_SYNC = `${API_URL}/shopping-cart/sync`;
const API_SHOPPING_CART_ADD_ITEM_URL = `${API_URL}/shopping-cart/action/add-item`;
const API_SHOPPING_CART_EDIT_ITEM_URL = `${API_URL}/shopping-cart/action/edit-item`;
const API_SHOPPING_CART_REMOVE_ITEM_URL = `${API_URL}/shopping-cart/action/remove-item`;

export interface CartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
  priceEur: Big
}

export interface CartItemWithoutPrice {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  quantity: number;
}

export interface CartItemKey {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
}

export interface RemovedCartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
}

export interface PriceChangedCartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>;
  stockQuality: t.TypeOf<typeof StockQuality>;
  oldPriceEur: t.TypeOf<typeof PriceEur>;
  newPriceEur: t.TypeOf<typeof PriceEur>;
}

type AddCartItemResult = {
  type: 'success'
} | {
  type: 'not_enough_stocks'
} | {
  type: 'unkown_error'
}

type RemoveItemFromCartResult = {
  type: 'success'
} | {
  type: 'unkown_error'
}

type UpdateCartItemResult = {
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

const LocalStorageCart = t.type({
  items: t.array(SyncShoppingCartResultItem),
  removedItems: t.array(SyncShoppingCartResultRemovedItem),
  priceChangedItems: t.array(SyncShoppingCartResultPriceChangedItem)
});

@Injectable({
  providedIn: 'root'
})
export class CartService {
  constructor(private http: HttpClient) {
    this.loadCartFromLocalStorage();
  }

  private items: CartItem[] = [];
  private _cartVersion: number = 0;

  public get cartVersion() { return this._cartVersion; }

  private subject: Subject<CartEvent> = new Subject();
  private itemsUpdateSubject: Subject<CartItem[]> = new Subject();

  private priceChangedItemsUpdateSubject: Subject<PriceChangedCartItem[]> = new Subject();
  private removedItemsUpdateSubject: Subject<RemovedCartItem[]> = new Subject();

  public get priceChangedItemsUpdate() { 
    return this.priceChangedItemsUpdateSubject.pipe(startWith(this.priceChangedItems));
  }
  public get removedItemsUpdate() {
     return this.removedItemsUpdateSubject.pipe(startWith(this.removedItems))
  }

  public get events() { return this.subject.asObservable(); }
  public get itemsUpdate() { return this.itemsUpdateSubject.asObservable(); }

  private _priceChangedItems: { [key: string]: { oldPrice: Big.Big, newPrice: Big.Big } } = {}
  private _removedItems: Set<string> = new Set()

  public get priceChangedItems() {
    return Object.keys(this._priceChangedItems).map(key => {
      const [isbn, stockFormat, stockQuality] = key.split(',')
      return {
        isbn,
        stockFormat: stockFormat as t.TypeOf<typeof StockFormat>,
        stockQuality: stockQuality as t.TypeOf<typeof StockQuality>,
        oldPriceEur: this._priceChangedItems[key].oldPrice,
        newPriceEur: this._priceChangedItems[key].newPrice
      }
    })
  }

  public get removedItems() {
    return [...this._removedItems].map(key => {
      const [isbn, stockFormat, stockQuality] = key.split(',')
      return {
        isbn,
        stockFormat: stockFormat as t.TypeOf<typeof StockFormat>,
        stockQuality: stockQuality as t.TypeOf<typeof StockQuality>
      }
    })
  }

  private mutex = new m.Mutex()
  private itemsBeingAdded: Set<string> = new Set()

  private loadCartFromLocalStorage() {
    const json = localStorage.getItem(LOCAL_STORAGE_CART_ITEMS_KEY);
    const { items, removedItems, priceChangedItems } = ensureValid(LocalStorageCart.decode(JSON.parse(json ?? '{}')));
    if (items !== null || items !== undefined) {
      this.processNewItems(items);
    }
    if (removedItems !== null || removedItems !== undefined) {
      this.processRemovedItems(removedItems);
    }
    if (priceChangedItems !== null || priceChangedItems !== undefined) {
      this.processPriceChangedItems(priceChangedItems);
    }
  }

  public clearPriceChangedItems() {
    this._priceChangedItems = {}
    this.saveCartToLocalStorage()
    this.priceChangedItemsUpdateSubject.next(this.priceChangedItems)
  }

  public clearRemovedItems() {
    this._removedItems.clear()
    this.saveCartToLocalStorage()
    this.removedItemsUpdateSubject.next(this.removedItems)
  }

  private saveCartToLocalStorage() {
    const cart = LocalStorageCart.encode({ items: this.items, removedItems: this.removedItems, priceChangedItems: this.priceChangedItems })
    localStorage.setItem(LOCAL_STORAGE_CART_ITEMS_KEY, JSON.stringify(cart));
  }

  public isItemBeingAdded(itemKey: CartItemKey): boolean {
    return this.itemsBeingAdded.has(join([itemKey.isbn, itemKey.stockFormat, itemKey.stockQuality], ','))
  }

  public reloadCart() {
    this.mutex.acquire().then(release => {
      this.http.get<SyncShoppingCartResult>(API_SHOPPING_CART_SYNC)
        .subscribe({
          next: res => {
            const result = ensureValid(SyncShoppingCartResult.decode(res));
            this._cartVersion = result.version

            this.processNewItems(result.items);
            this.processRemovedItems(result.removedItems);
            this.processPriceChangedItems(result.priceChangedItems);

            this.saveCartToLocalStorage();

            release()
          },
          error: () => {
            release()
          }
        })
    })
  }

  public addBookToCart(item: CartItemWithoutPrice): Observable<AddCartItemResult> {
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

    return from(this.mutex.acquire().then(release => {
      return this.http.post<AddCartItemResult>(API_SHOPPING_CART_ADD_ITEM_URL, request, {}).pipe(
        map(res => {
          const result = ensureValid(SyncShoppingCartResult.decode(res));
          this._cartVersion = result.version

          this.processNewItems(result.items);
          this.processRemovedItems(result.removedItems);
          this.processPriceChangedItems(result.priceChangedItems);

          this.saveCartToLocalStorage();

          this.itemsBeingAdded.delete(key)
          release()

          return { type: 'success' as const };
        }),
        catchError(({ error }) => {
          this.itemsBeingAdded.delete(key)
          release()

          if (error !== undefined && error !== null) {
            if (error.code === 'STOCK_QUANTITY_NOT_AVAILABLE') {
              return of({ type: 'not_enough_stocks' as const });
            }
          }

          return of({ type: 'unkown_error' as const });
        })
      );
    })).pipe(
      mergeMap((x: Observable<AddCartItemResult>) => x)
    )
  }

  public removeCartItem(isbn: string, stockFormat: t.TypeOf<typeof StockFormat>, stockQuality: t.TypeOf<typeof StockQuality>): Observable<RemoveItemFromCartResult> {
    const request: RemoveItemFromShoppingCartRequest = {
      item: {
        isbn: isbn,
        stockFormat,
        stockQuality,
      }
    };

    return from(this.mutex.acquire().then(release => {
      return this.http.post(`${API_SHOPPING_CART_REMOVE_ITEM_URL}`, request).pipe(
        map(res => {
          const result = ensureValid(SyncShoppingCartResult.decode(res));
          this._cartVersion = result.version

          this.processNewItems(result.items);
          this.processRemovedItems(result.removedItems);
          this.processPriceChangedItems(result.priceChangedItems);

          this.saveCartToLocalStorage();

          release()

          return { type: 'success' as const };
        }),
        catchError(({ error }) => {
          console.log(error)
          release()

          return of({ type: 'unkown_error' as const });
        })
      );
    })).pipe(
      mergeMap((x: Observable<RemoveItemFromCartResult>) => x)
    )
  }

  public getItems(): CartItem[] {
    return [...this.items];
  }

  public editQuantity(itemKey: CartItemKey, quantity: number): Observable<UpdateCartItemResult> {
    return from(this.mutex.acquire().then(release => {
      return this.http.post<SyncShoppingCartResult>(API_SHOPPING_CART_EDIT_ITEM_URL, {
        item: {
          isbn: itemKey.isbn,
          stockFormat: itemKey.stockFormat,
          stockQuality: itemKey.stockQuality,
          quantity: quantity
        }
      }).pipe(
        map(res => {
          const result = ensureValid(SyncShoppingCartResult.decode(res));
          this._cartVersion = result.version

          this.processNewItems(result.items);
          this.processRemovedItems(result.removedItems);
          this.processPriceChangedItems(result.priceChangedItems);

          this.saveCartToLocalStorage();

          release()

          return { type: 'success' as const };
        }),
        catchError(({ error }) => {
          release()

          if (error !== undefined && error !== null) {
            if (error.code === 'STOCK_QUANTITY_NOT_AVAILABLE') {
              return of({ type: 'not_enough_stocks' as const });
            }
          }

          return of({ type: 'unkown_error' as const });
        })
      );
    })).pipe(
      mergeMap((x: Observable<UpdateCartItemResult>) => x)
    )
  }

  private removeNotExistingPriceChangedItems() {
    const existingItems = new Set(this.items.map(item => join([item.isbn, item.stockFormat, item.stockQuality], ',')))

    for (const key in this._priceChangedItems) {
      if (!existingItems.has(key)) {
        delete this._priceChangedItems[key]
      }
    }

    this.priceChangedItemsUpdateSubject.next(this.priceChangedItems)
  }

  private processNewItems(newItems: SyncShoppingCartResultItem[]) {
    this.subject.next({ type: 'items-added', item: newItems });
    this.itemsUpdateSubject.next(newItems);
    this.items = newItems
    this.removeNotExistingPriceChangedItems()
  }

  private processRemovedItems(removedItems: SyncShoppingCartResultRemovedItem[]) {
    if (removedItems.length > 0) {
      this.removedItemsUpdateSubject.next(removedItems);
      this.subject.next({ type: 'items-removed', item: removedItems });
    }

    removedItems.forEach(removedItem => {
      this._removedItems.add(join([removedItem.isbn, removedItem.stockFormat, removedItem.stockQuality], ','))
    })
  }

  private processPriceChangedItems(priceChangedItems: SyncShoppingCartResultPriceChangedItem[]) {
    if (priceChangedItems.length > 0) {
      this.subject.next({ type: 'items-price-changed', item: priceChangedItems });
      this.priceChangedItemsUpdateSubject.next(priceChangedItems)
    }

    priceChangedItems.forEach(priceChangedItem => {
      const key = join([priceChangedItem.isbn, priceChangedItem.stockFormat, priceChangedItem.stockQuality], ',')
      const mappedPriceChangedItem = this._priceChangedItems[key]

      if (mappedPriceChangedItem === undefined) {
        this._priceChangedItems[key] = { oldPrice: priceChangedItem.oldPriceEur, newPrice: priceChangedItem.newPriceEur }
      } else {
        mappedPriceChangedItem.newPrice = priceChangedItem.newPriceEur
      }
    })
  }
}
