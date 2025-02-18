import { Injectable } from '@angular/core';
import { CartService } from './cart.service';

@Injectable({
  providedIn: 'root'
})
export class CartToastNotifierService {
  constructor(cartService: CartService) {
    cartService.events.subscribe(event => {
      if (event.type === 'items-added') {
        
      }
    });
  }
}
