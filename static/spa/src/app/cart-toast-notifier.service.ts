import { Injectable } from '@angular/core';
import { CartService } from './cart.service';
import { ToastService } from './toast.service';

@Injectable({
  providedIn: 'root'
})
export class CartToastNotifierService {
  constructor(
    private readonly cartService: CartService, 
    private readonly toastService: ToastService
  ) {}

  public start() {
    this.cartService.events.subscribe(event => {
      if (event.type === 'items-removed') {
        this.toastService.showInfo('some-items-removed-from-cart')
      } else if (event.type === 'items-price-changed') {
        this.toastService.showInfo('some-items-price-changed')
      }
    });
  }
}
