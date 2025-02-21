import { Component, Input, OnInit } from '@angular/core';
import { OrderSummary } from '../order.service';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-order-summary',
  standalone: false,
  
  templateUrl: './order-summary.component.html',
  styleUrl: './order-summary.component.css'
})
export class OrderSummaryComponent implements OnInit {
  constructor(
    private readonly translationService: TranslateService,
    private readonly router: Router
  ) {

  }

  @Input() summary!: OrderSummary;

  public normalizedPlacedAt: string = ''

  ngOnInit(): void {
    this.normalizedPlacedAt = this.summary.placeAt.toLocal().setLocale(this.translationService.currentLang).toLocaleString({
      dateStyle: 'full',
      timeStyle: 'short'
    })  
  }

  onSeeDetails(): void {
    this.router.navigate(['/orders/', this.summary.id])
  }
}
