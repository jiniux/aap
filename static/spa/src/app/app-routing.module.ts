import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from './landing/landing.component';
import { BookOverviewComponent } from './book-overview/book-overview.component';
import { BookSearchComponent } from './book-search/book-search.component';

const routes: Routes = [
  {
    path: "",
    component: LandingComponent, children: [
      { path: "", component: BookSearchComponent },
      { path: "book/:id", component: BookOverviewComponent },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
