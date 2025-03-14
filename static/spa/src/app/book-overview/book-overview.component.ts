import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { CatalogService, FullCatalogBookResult } from '../catalog.service';
import { map, startWith, Subscription } from 'rxjs';
import * as t from "io-ts"
import { BookCategory, StockFormat, StockQuality } from '../../utils/types';
import { ActivatedRoute } from '@angular/router';
import { cmpFormatByPriority, cmpQualityByPriority } from '../../utils/stock-priorities';
import { emojiFromStockQuality, EmojiNameWithColor } from '../../utils/emoji-from-stock-quality';
import * as _ from "lodash"
import { ToastService } from '../toast.service';
import { CartService } from '../cart.service';
import { AuthService } from '@auth0/auth0-angular';
import { getSuitableFormatPreviewImageFromFormat } from '../../utils/most-suitable-preview-image';
import Big from 'big.js';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';

type BookLoadStateResult = {
  title: string;
  isbn: string;
  authors: string;
  publisher: string;
  edition: string;
  description: string;
  categories: t.TypeOf<typeof BookCategory>[];
  publicationYear: number;
  stocks: {
    format: t.TypeOf<typeof StockFormat>;
    quality: t.TypeOf<typeof StockQuality>;
    priceEur: Big;
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


type Stock = BookLoadStateResult['stocks'][0]

function computeBestStock(stocks: Readonly<Stock[]>): Stock {
  const tmp = new Array<Stock>(...stocks)

  if (tmp.length === 1) {
    return tmp[0]
  } 

  tmp.sort((a, b) => {
    const qualityCmp = cmpQualityByPriority(a.quality, b.quality)
    
    if (qualityCmp !== 0) {
      return qualityCmp
    }
    
    const formatCmp = cmpFormatByPriority(a.format, b.format)
    
    if (formatCmp !== 0) {
      return formatCmp
    }

    return a.priceEur.cmp(b.priceEur)
  })
  
  return tmp[0]
}

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
      priceEur: s.priceEur
    })),
    preview: result.formatPreviewImages.map((p) => ({
      url: p.url,
      format: p.format
    })),
    categories: result.categories
  }
}

@Component({
  selector: 'app-book-overview',
  standalone: false,

  templateUrl: './book-overview.component.html',
  styleUrl: './book-overview.component.css'
})
export class BookOverviewComponent {
  @ViewChild('quantitySelector') quantitySelector: ElementRef | undefined;

  public state: BookLoadState = { loading: true };
  public selectedPreviewIndex = 0

  public stockFormats: t.TypeOf<typeof StockFormat>[] = []
  public stockQualitiesForSelectedFormat: { [format in t.TypeOf<typeof StockFormat>]: [t.TypeOf<typeof StockQuality>, string, EmojiNameWithColor][] } = {
    "hardcover": [],
    "paperback": [],
  }

  public currentCoverUrl: string = getSuitableFormatPreviewImageFromFormat(null, [])

  public topQualityPrices: string[] = []
  public selectedStockFormat: t.TypeOf<typeof StockFormat> = "hardcover"
  public selectedStockQualityIndex: number = 0

  private isbn: string = ""

  private loadBookSubscription: Subscription | null = null
  private authSubscription: Subscription | null = null
  
  public quantityForm: FormGroup;
  
  get quantityControl(): FormControl {
    return this.quantityForm.get('quantity') as FormControl;
  }

  constructor(
    private readonly catalogService: CatalogService, 
    private readonly route: ActivatedRoute,
    private readonly toastService: ToastService,
    private readonly cartService: CartService,
    private readonly authService: AuthService,
    private readonly fb: FormBuilder
  ) { 
    // Initialize form with quantity control and validation
    this.quantityForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(999)]]
    });
  }

  public loggedIn = false
  
  updateSelectedStockQualities() {
    if (this.state.loading === false) {
      this.stockQualitiesForSelectedFormat = {
        "hardcover": [],
        "paperback": []
      }

      this.state.result.stocks
        .forEach(s => {
          const pair: [t.TypeOf<typeof StockQuality>, string, EmojiNameWithColor] = [s.quality, s.priceEur.toFixed(2), emojiFromStockQuality(s.quality)];
          this.stockQualitiesForSelectedFormat[s.format].push(pair);
        });

      Object.keys(this.stockQualitiesForSelectedFormat).forEach((qualityKey) => {
        this.stockQualitiesForSelectedFormat[qualityKey as t.TypeOf<typeof StockFormat>].sort((a, b) => cmpQualityByPriority(a[0], b[0]))
      })

      if (this.stockFormats.length > 0) {
        this.currentCoverUrl = getSuitableFormatPreviewImageFromFormat(this.selectedStockFormat, this.state.result.preview)
      } else {
        this.currentCoverUrl = getSuitableFormatPreviewImageFromFormat(null, this.state.result.preview)
      }
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
    if (this.stockQualitiesForSelectedFormat[this.selectedStockFormat]?.length === 0) {
      this.itemBeingAdded = false
      return
    }

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
      quantity: this.quantityForm.get('quantity')!.value
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
        let bestStock = computeBestStock(this.state.result.stocks)

        this.stockFormats = _.uniq(this.state.result.stocks.map((s) => s.format))
        this.stockFormats.sort(cmpFormatByPriority)
        this.selectedStockFormat = bestStock.format

        this.updateSelectedStockQualities()
        this.updateItemBeingAdded()

        this.selectedStockQualityIndex = this.stockQualitiesForSelectedFormat[bestStock.format].findIndex((pair) => pair[0] === bestStock.quality)
      }
    })
  }

  increment() {
    const currentValue = this.quantityForm.get('quantity')!.value;
    this.quantityForm.patchValue({
      quantity: currentValue + 1
    });
  }

  decrement() {
    const currentValue = this.quantityForm.get('quantity')!.value;
    if (currentValue > 1) {
      this.quantityForm.patchValue({
        quantity: currentValue - 1
      });
    }
  }
  
  validateQuantity() {
    const quantityControl = this.quantityForm.get('quantity')!;
    let value = quantityControl.value;
    
    if (isNaN(value) || value < 1) {
      value = 1;
    } else if (value > 999) {
      value = 999;
    }
    
    this.quantityForm.patchValue({
      quantity: value
    });
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
