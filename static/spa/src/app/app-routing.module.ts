import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BookOverviewComponent } from './book-overview/book-overview.component';
import { BookSearchComponent } from './book-search/book-search.component';
import { CartComponent } from './cart/cart.component';
import { OrdersComponent } from './orders/orders.component';
import { OrderDetailsComponent } from './order-details/order-details.component';

const routes: Routes = [
  { path: "", component: BookSearchComponent },
  { path: "book/:id", component: BookOverviewComponent },
  { path: "cart", component: CartComponent },
  { path: "orders", component: OrdersComponent},
  { path: "orders/:id", component: OrderDetailsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
