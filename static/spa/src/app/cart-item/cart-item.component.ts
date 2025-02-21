import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CartItem, CartService } from '../cart.service';
import { Router } from '@angular/router';
import { ToastService } from '../toast.service';
import * as t from "io-ts"
import { StockFormat, StockQuality } from '../../utils/types';
import { emojiFromStockQuality } from '../../utils/emoji-from-stock-quality';

export interface FullCartItem {
  isbn: string;
  stockFormat: t.TypeOf<typeof StockFormat>
  stockQuality: t.TypeOf<typeof StockQuality>
  quantity: number;
  title: string;
  authorNames: string;
  pricing: string;
  coverUrl: string
}

@Component({
  selector: 'app-cart-item',
  standalone: false,
  templateUrl: './cart-item.component.html',
  styleUrl: './cart-item.component.css'
})
export class CartItemComponent implements OnInit {
  @Input() item: FullCartItem = { isbn: '', stockFormat: 'hardcover', stockQuality: 'new', quantity: 0, title: '', authorNames: '', pricing: '', coverUrl: ''};

  constructor(
    private readonly cartService: CartService,
    private readonly toastService: ToastService,
    private readonly router: Router
  ) {}

  public emoji: string = 'smile'
  public emojiColor: string = 'green'
  
  ngOnInit(): void {
    const { color, emojiName } = emojiFromStockQuality(this.item.stockQuality)
    
    this.emojiColor = color
    this.emoji = emojiName
  }

  isEditing = false;
  tempQuantity: number = 0;

  // New flag for removal confirmation
  showRemoveConfirmation = false;
  
  startEditing() {
    this.showRemoveConfirmation = false
    this.tempQuantity = this.item.quantity;
    this.isEditing = true;
  }

  openOverview() {
    this.router.navigate(['/book', this.item.isbn]);
  }

  confirmEdit() {
    if (this.tempQuantity !== this.item.quantity) {
      this.cartService.editQuantity({ 
        isbn: this.item.isbn, 
        stockFormat: this.item.stockFormat, 
        stockQuality: this.item.stockQuality 
      }, this.tempQuantity)
      .subscribe({
        next: (result) => {
          if (result.type === 'success') {
            this.toastService.showSuccess('item-quantity-updated');
          } else if (result.type === 'not_enough_stocks') {
            this.toastService.showError('not-enough-stocks-for-update')
          } else {
            this.toastService.showError('error-updating-quantity');
          }
        },
      })
    }
    this.isEditing = false;
  }

  cancelEdit() {
    this.isEditing = false;
  }

  // Update removal flow: first set confirmation flag
  removeItem() {
    this.isEditing = false;
    if (!this.showRemoveConfirmation) {
      this.showRemoveConfirmation = true;
    }
  }

  confirmRemoval() {
    this.cartService.removeCartItem(this.item.isbn, this.item.stockFormat, this.item.stockQuality)
      .subscribe({
        next: () => {
          this.toastService.showSuccess('item-removed-from-cart');
        },
        error: (error) => {
          this.toastService.showError('error-removing-item-from-cart', error);
        }
      });
    this.showRemoveConfirmation = false;
  }

  cancelRemoval() {
    this.showRemoveConfirmation = false;
  }
}
