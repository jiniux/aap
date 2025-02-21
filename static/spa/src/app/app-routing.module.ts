import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BookOverviewComponent } from './book-overview/book-overview.component';
import { BookSearchComponent } from './book-search/book-search.component';
import { CartComponent } from './cart/cart.component';

const routes: Routes = [
  { path: "", component: BookSearchComponent },
  { path: "book/:id", component: BookOverviewComponent },
  { path: "cart", component: CartComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
