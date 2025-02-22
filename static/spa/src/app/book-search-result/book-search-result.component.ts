import { Component, Input, OnInit } from '@angular/core';
import { BookSearchResults } from '../catalog.service';
import Big from 'big.js';
import { Router } from '@angular/router';
import { cmpFormatByPriority, cmpQualityByPriority } from '../../utils/stock-priorities';
import { emojiFromStockQuality } from '../../utils/emoji-from-stock-quality';
import { getSuitableFormatPreviewImageFromFormat } from '../../utils/most-suitable-preview-image';

type Stock = BookSearchResults[0]['stocks'][0]
type FormatPreviewImage = BookSearchResults[0]['formatPreviewImages'][0]
type Author = BookSearchResults[0]['authors'][0]
type Publisher = BookSearchResults[0]['publisher']

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

@Component({
  selector: 'app-landing-search-result',
  standalone: false,
  
  templateUrl: './book-search-result.component.html',
  styleUrl: './book-search-result.component.css'
})
export class LandingSearchResultComponent implements OnInit {
  @Input("title") public title: string = ''
  @Input("authors") public authors: Author[] = []
  @Input("publisher") public publisher: Publisher = { id: '', name: '' }
  @Input("publicationYear") public publicationYear: number = 0
  @Input("edition") public edition: string = ''
  @Input("stocks") public stocks: Readonly<Stock[]> = []
  @Input("formatPreviewImages") public formatPreviewImages: Readonly<FormatPreviewImage[]> = []
  @Input("isbn") public isbn: string = ''

  public emoji: string = 'smile'
  public emojiColor: string = 'green'

  constructor(private readonly route: Router) {}

  public summary: {
    authorNames: string,
    publisherName: string,
    coverUrl: string,
    supply: { available: false } | {
      available: true
      formats: string
      qualities: string
      pricing: string,
      hasMoreFormats: boolean,
      hasMoreQualities: boolean
    }
  } = null!

  public openOverview() {
    this.route.navigate(['/book', this.isbn])
  }

  ngOnInit(): void {
    this.summary = {
      authorNames: this.authors.map(author => `${author.firstName} ${author.lastName}`).join(", "),
      publisherName: this.publisher.name,
      coverUrl: getSuitableFormatPreviewImageFromFormat(this.stocks[0]?.format ?? null, this.formatPreviewImages),
      supply: { available: false }
    }

    if (this.stocks.length !== 0) {
      const bestStock = computeBestStock(this.stocks)
      const formats = this.stocks.map(s => s.format)
      const qualities = this.stocks.map(s => s.quality)
      this.summary.coverUrl = getSuitableFormatPreviewImageFromFormat(bestStock.format, this.formatPreviewImages)
      
      this.summary.supply = {
        available: true,
        formats: bestStock.format,
        qualities: bestStock.quality,
        hasMoreFormats: formats.length > 1,
        hasMoreQualities: qualities.length > 1,
        pricing: "€" + bestStock.priceEur.toFixed(2) 
      }

      const { color, emojiName } = emojiFromStockQuality(bestStock.quality)

      this.emojiColor = color
      this.emoji = emojiName
    }
  }
}
