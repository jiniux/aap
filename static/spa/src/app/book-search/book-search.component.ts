import { Component, OnDestroy } from '@angular/core';
import { debounceTime, map, Observable, startWith, Subscription, switchMap } from 'rxjs';
import { BookSearchResultEntry, BookSearchResults, CatalogService } from '../catalog.service';
import { FormControl } from '@angular/forms';
import { BookSearchStateService } from '../book-search-state.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'search',
  standalone: false,

  templateUrl: './book-search.component.html',
  styleUrl: './book-search.component.css'
})
export class BookSearchComponent implements OnDestroy {
  public searchQueryInput: FormControl = new FormControl('');

  public searchResults: BookSearchResults = { currentPage: 0, totalPages: 0, entries: []};
  public searchLoading: boolean | null = null;

  private bookSearchStateEventsSubscription: Subscription | null = null;

  constructor(
    private readonly bookSearchStateService: BookSearchStateService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) { }


  ngOnDestroy(): void {
    this.bookSearchStateEventsSubscription?.unsubscribe();
  }

  setResults(results: BookSearchResults) {
    this.searchResults = results;
    this.searchLoading = false;
  }

  setLoading() {
    this.searchLoading = true;
  }

  search(page: number = 0) {
    this.bookSearchStateService.searchBook(this.searchQueryInput.value, page);
  }

  goToNextPage() {
    if (this.searchResults.currentPage < this.searchResults.totalPages - 1) {
      this.search(this.searchResults.currentPage + 1);
    }
  }

  goToPreviousPage() {
    if (this.searchResults.currentPage > 0) {
      this.search(this.searchResults.currentPage - 1);
    }
  }

  ngOnInit() {
    this.searchResults = { currentPage: 0, totalPages: 0, entries: [] };

    this.searchQueryInput
      .valueChanges.subscribe((value: string) => {
        this.searchResults = { currentPage: 0, totalPages: 0, entries: [] };
      })

    this.searchQueryInput
      .valueChanges
      .pipe(debounceTime(300))
      .subscribe((_: string) => {
        this.search(0); // Reset to first page on new search

        this.router.navigate([], {queryParams: {q: this.searchQueryInput.value}});
      });

    // Listen to state updates and push new history state
    this.bookSearchStateEventsSubscription = this.bookSearchStateService.events.subscribe((state) => {
      if (state.loading) {
        this.setLoading();
      } else {
        this.setResults(state.results);
      }
    });

    const query = this.route.snapshot.queryParamMap.get('q') || '';

    if (this.searchQueryInput.value !== query || query === '') {
      this.searchQueryInput.setValue(query);
    }
  }
}
