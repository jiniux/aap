import { Component, OnInit } from '@angular/core';
import { OrderService, OrderSummary } from '../order.service';

@Component({
  selector: 'app-orders',
  standalone: false,
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  public orders: OrderSummary[] = [];

  constructor(private readonly orderService: OrderService) {
  }

  ngOnInit(): void {
    this.orderService.getOrdersSummaries().subscribe({
      next: (res) => {
        this.orders = res.orders;
      }
    });
  }
}
