import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as t from 'io-ts';
import { PriceEur } from '../utils/types';
import { map, Observable } from 'rxjs';
import { ensureValid } from '../ext/io-ts.ext';
import Big from 'big.js';

export const ShipmentCostRequest = t.type({
  country: t.string,
  state: t.string,
  city: t.string,
  street: t.string,
  zipCode: t.string
});

export const ShipmentCostResult = t.type({
  costEur: PriceEur
});

export type ShipmentCostRequest = t.TypeOf<typeof ShipmentCostRequest>;

const API_SHIPMENT_CALCULATE_COST_URL = "/api/shipping/action/calculate-price"

@Injectable({
  providedIn: 'root'
})
export class ShippingService {
  constructor(private readonly httpClient: HttpClient) { }

  getShipmentCost(request: ShipmentCostRequest): Observable<{ costEur: Big }> {
    return this.httpClient.post(API_SHIPMENT_CALCULATE_COST_URL, request)
      .pipe(
        map(result => ensureValid(ShipmentCostResult.decode(result)))
      )
  }
}