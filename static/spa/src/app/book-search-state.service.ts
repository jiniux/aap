import { Injectable } from '@angular/core';
import { CatalogService, BookSearchResults } from './catalog.service';
import { map, startWith, Subject, Subscription } from 'rxjs';

type BookSearchState = {
  loading: true;
} | {
  loading: false;
  results: BookSearchResults
}

@Injectable({
  providedIn: 'root'
})
export class BookSearchStateService {
  private subject = new Subject<BookSearchState>()
  private searchSubscription: Subscription | null = null;

  constructor(private readonly catalogService: CatalogService) { }

  public searchBook(query: string, page: number = 0) {
    this.searchSubscription?.unsubscribe();

    this.searchSubscription = this.catalogService.searchBooks(query, page)
      .pipe(
        map((results) => ({ loading: false, results } as BookSearchState)),
        startWith({ loading: true } as BookSearchState)
      )
      .subscribe((results) => {
        this.subject.next(results);

        if (!results.loading) {
          this._snapshot = results.results;
        }
      });
  }

  private _snapshot: BookSearchResults = { currentPage: 0, totalPages: 0, entries: [] };

  public get snapshot () {
    return this._snapshot;
  }

  public get events() {
    return this.subject.asObservable();
  }
}
