import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../order.service';
import { Observable, catchError, map, of, startWith, switchMap } from 'rxjs';
import { TypeOf } from 'io-ts';
import { FullOrderResult } from '../order.service';

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrl: './order-details.component.css',
  standalone: false
})
export class OrderDetailsComponent implements OnInit {
  order$: Observable<{
    type: 'loading'
  } | {
    type: 'error',
    error: string
  } | {
    type: 'loaded',
    data: TypeOf<typeof FullOrderResult>
  }> = of({ type: 'loading' });

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit() {
    this.order$ = this.route.params.pipe(
      switchMap(params => this.orderService.getOrder(params['id']).pipe(
        map(order => ({ type: 'loaded' as const, data: order })),
        catchError(error => { console.log(error); return of({ 
          type: 'error' as const, 
          error: 'Failed to load order details' 
        })}),
        startWith({ type: 'loading' as const })
      ))
    );
  }
}
