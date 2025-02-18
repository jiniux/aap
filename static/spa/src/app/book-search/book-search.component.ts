import { Component } from '@angular/core';
import { debounceTime, map, Observable, startWith, switchMap } from 'rxjs';
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
export class BookSearchComponent {
  public searchQueryInput: FormControl = new FormControl('');

  public searchResults: BookSearchResults = [];
  public searchLoading: boolean | null = null;

  constructor(
    private readonly bookSearchStateService: BookSearchStateService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) { }

  setResults(results: BookSearchResults) {
    this.searchResults = results;
    this.searchLoading = false;
  }

  setLoading() {
    this.searchLoading = true;
  }

  search() {
    this.bookSearchStateService.searchBook(this.searchQueryInput.value);
  }

  ngOnInit() {
    this.searchResults = []

    this.searchQueryInput
      .valueChanges.subscribe((value: string) => {
        this.searchResults = []
      })

    this.searchQueryInput
      .valueChanges
      .pipe(debounceTime(300))
      .subscribe((_: string) => {
        this.search();

        this.router.navigate([], {queryParams: {q: this.searchQueryInput.value}});
      });

    // Listen to state updates and push new history state
    this.bookSearchStateService.events.subscribe((state) => {
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
