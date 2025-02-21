import { Component, Input } from '@angular/core';
import { CatalogService, FullCatalogBookResult } from '../catalog.service';
import { map, startWith, Subscription } from 'rxjs';
import * as t from "io-ts"
import { StockFormat, StockQuality } from '../../utils/types';
import { ActivatedRoute } from '@angular/router';
import { cmpFormatByPriority, cmpQualityByPriority } from '../../utils/stock-priorities';
import { emojiFromStockQuality, EmojiNameWithColor } from '../../utils/emoji-from-stock-quality';
import * as _ from "lodash"
import { ToastService } from '../toast.service';
import { CartService } from '../cart.service';
import { AuthService } from '@auth0/auth0-angular';

type BookLoadStateResult = {
  title: string;
  isbn: string;
  authors: string;
  publisher: string;
  edition: string;
  description: string;
  publicationYear: number;
  stocks: {
    format: t.TypeOf<typeof StockFormat>;
    quality: t.TypeOf<typeof StockQuality>;
    priceEur: string;
  }[];
  preview: {
    url: string;
    format: t.TypeOf<typeof StockFormat>;
  }[]
}

type BookLoadState = {
  loading: true;
} | {
  loading: false;
  result: BookLoadStateResult;
};

function mapFullCatalogBookResult(result: FullCatalogBookResult): BookLoadStateResult {
  return {
    title: result.title,
    isbn: result.isbn,
    authors: result.authors.map((a) => `${a.firstName} ${a.lastName}`).join(", "),
    publisher: result.publisher.name,
    edition: result.edition,
    description: result.description,
    publicationYear: result.publicationYear,
    stocks: result.stocks.map((s) => ({
      format: s.format,
      quality: s.quality,
      priceEur: s.priceEur.toFixed(2)
    })),
    preview: result.formatPreviewImages.map((p) => ({
      url: "/api" + p.url,
      format: p.format
    }))
  }
}

@Component({
  selector: 'app-book-overview',
  standalone: false,

  templateUrl: './book-overview.component.html',
  styleUrl: './book-overview.component.css'
})
export class BookOverviewComponent {
  public state: BookLoadState = { loading: true };
  public selectedPreviewIndex = 0

  public stockFormats: t.TypeOf<typeof StockFormat>[] = []
  public stockQualitiesForSelectedFormat: { [format in t.TypeOf<typeof StockFormat>]: [t.TypeOf<typeof StockQuality>, string, EmojiNameWithColor][] } = {
    "hardcover": [],
    "paperback": [],
  }

  public topQualityPrices: string[] = []
  public selectedStockFormat: t.TypeOf<typeof StockFormat> = "hardcover"
  public selectedStockQualityIndex: number = 0

  private isbn: string = ""

  private loadBookSubscription: Subscription | null = null
  private authSubscription: Subscription | null = null

  constructor(
    private readonly catalogService: CatalogService, 
    private readonly route: ActivatedRoute,
    private readonly toastService: ToastService,
    private readonly cartService: CartService,
    private readonly authService: AuthService
  ) { }

  public loggedIn = false
  
  updateSelectedStockQualities() {
    if (this.state.loading === false) {
      this.stockQualitiesForSelectedFormat = {
        "hardcover": [],
        "paperback": []
      }

      this.state.result.stocks
        .forEach(s => {
          const pair: [t.TypeOf<typeof StockQuality>, string, EmojiNameWithColor] = [s.quality, s.priceEur, emojiFromStockQuality(s.quality)];
          this.stockQualitiesForSelectedFormat[s.format].push(pair);
        });

      Object.keys(this.stockQualitiesForSelectedFormat).forEach((qualityKey) => {
        this.stockQualitiesForSelectedFormat[qualityKey as t.TypeOf<typeof StockFormat>].sort((a, b) => cmpQualityByPriority(a[0], b[0]))
      })
    }
  }

  public isFormatDropdownOpen: boolean = false;
  public isQualityDropdownOpen: boolean = false;

  public toggleFormatDropdown() {
    this.isQualityDropdownOpen = false
    this.isFormatDropdownOpen = !this.isFormatDropdownOpen;
  }

  public toggleQualityDropdown() {
    this.isFormatDropdownOpen = false
    this.isQualityDropdownOpen = !this.isQualityDropdownOpen;
  }

  private updateItemBeingAdded() {
    this.itemBeingAdded = this.cartService.isItemBeingAdded({
      isbn: this.isbn,
      stockFormat: this.selectedStockFormat,
      stockQuality: this.stockQualitiesForSelectedFormat[this.selectedStockFormat][this.selectedStockQualityIndex][0],
    })
  }

  public selectNewStockFormat(format: t.TypeOf<typeof StockFormat>) {
    this.selectedStockFormat = format;
    this.updateSelectedStockQualities();
    this.selectedStockQualityIndex = 0
    this.isFormatDropdownOpen = false;
    this.updateItemBeingAdded()
  }

  public selectNewStockQuality(index: number) {
    this.selectedStockQualityIndex = index;
    this.updateSelectedStockQualities();
    this.isQualityDropdownOpen = false;
    this.updateItemBeingAdded()
  }

  public itemBeingAdded : boolean = false;

  public addBookToCart() {
    if (!this.loggedIn) {
      return
    }

    if (this.itemBeingAdded) {
      return
    }

    this.itemBeingAdded = true;

    this.cartService.addBookToCart({
      isbn: this.isbn,
      stockFormat: this.selectedStockFormat,
      stockQuality: this.stockQualitiesForSelectedFormat[this.selectedStockFormat][this.selectedStockQualityIndex][0],
      quantity: this.quantity
    }).subscribe((result) => {
      if (result.type === "success") {
        this.toastService.showSuccess("book-added-to-cart")
      } else if (result.type === "not_enough_stocks") {
        this.toastService.showError("not-enough-stocks-when-adding-to-cart")
      } else {
        this.toastService.showError("unknown-error-when-adding-to-cart")
      }
      this.itemBeingAdded = false;
    })
  }

  private loadBook() {
    this.loadBookSubscription = this.catalogService.getBook(this.isbn).pipe(
      map((results) => ({ loading: false, result: mapFullCatalogBookResult(results) })),
      startWith({ loading: true } as BookLoadState)
    ).subscribe((state) => {
      this.state = state;

      if (this.state.loading === false) {
        
        this.stockFormats = _.uniq(this.state.result.stocks.map((s) => s.format))
        this.stockFormats.sort(cmpFormatByPriority)
        this.selectedStockFormat = this.stockFormats[0]

        this.updateSelectedStockQualities()
        this.updateItemBeingAdded()
      }
    })
  }

  quantity = 1;

  increment() {
    this.quantity++;
  }

  decrement() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  ngOnDestroy() {
    this.loadBookSubscription?.unsubscribe()
    this.authSubscription?.unsubscribe()
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.isbn = params.get("id")!
      this.loadBook()
    })

    this.authSubscription = this.authService.isAuthenticated$.subscribe((isAuthenticated) => {
      this.loggedIn = isAuthenticated
    })
  }
}
