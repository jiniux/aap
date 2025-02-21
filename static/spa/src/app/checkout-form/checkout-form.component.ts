import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import Big from 'big.js';
import * as countries from 'countries-list';
import { CartService } from '../cart.service';
import { debounceTime, Subscription, filter, map, mergeMap, distinctUntilChanged } from 'rxjs';
import { ShippingService } from '../shipment.service';
import _ from "lodash"
import { CheckoutService } from '../checkout.service';
import { ToastService } from '../toast.service';

@Component({
  selector: 'app-checkout-form',
  standalone: false,
  templateUrl: './checkout-form.component.html',
  styleUrl: './checkout-form.component.css'
})
export class CheckoutFormComponent implements OnInit, OnDestroy {
  checkoutForm: FormGroup = new FormGroup({});
  cartValue: Big = new Big(0);
  shipmentCost: Big = new Big(0);
  total: Big = new Big(0);
  countryList: any;

  private itemsUpdateSubscription: Subscription | undefined;
  private shipmentCostSubscription: Subscription | undefined;

  constructor(
    private readonly fb: FormBuilder, 
    private readonly cartService: CartService,
    private readonly shippingService: ShippingService,
    private readonly checkoutService: CheckoutService,
    private readonly toastService: ToastService
  ) {
    this.countryList = Object.entries(countries.countries).map(([code, country]) => ({
      code,
      name: country.native || country.name
    })).sort((a, b) => a.name.localeCompare(b.name));
  }

  ngOnDestroy(): void {
    this.itemsUpdateSubscription?.unsubscribe()
    this.shipmentCostSubscription?.unsubscribe()
  }

  clearForm(): void {
    this.checkoutForm.reset();
  }

  ngOnInit(): void {
    this.itemsUpdateSubscription = this.cartService.itemsUpdate.subscribe({
      next: (items) => {
        this.cartValue = items.reduce((acc, item) => acc.plus(item.priceEur.mul(item.quantity)), new Big(0));
        this.total = this.cartValue.plus(this.shipmentCost);
        this.emptyCart = items.length === 0
      }
    })

    this.checkoutForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.pattern('^[a-zA-Z]+$')]],
      lastName: ['', [Validators.required, Validators.pattern('^[a-zA-Z]+$')]],
      address: ['', Validators.required],
      city: ['', Validators.required],
      zipCode: ['', Validators.required],
      provinceState: ['', Validators.required],
      country: ['', Validators.required],
      creditCard: ['', [Validators.required, Validators.pattern('^[0-9]{16}$')]],
      creditCardTenant: ['', [Validators.required, Validators.pattern('^[a-zA-Z ]+$')]],
      creditCardCVC: ['', [Validators.required, Validators.pattern('^[0-9]{3,4}$')]],
      creditCardExp: ['', [Validators.required, Validators.pattern('^(0[1-9]|1[0-2])\/[0-9]{2}$')]]
    });

    this.shipmentCostSubscription = this.checkoutForm.valueChanges
      .pipe(
        debounceTime(500),
        filter(() => (this.checkoutForm.get('country')?.valid 
          && this.checkoutForm.get('city')?.valid 
          && this.checkoutForm.get('zipCode')?.valid 
          && this.checkoutForm.get('provinceState')?.valid 
          && this.checkoutForm.get('address')?.valid) ?? false),
        map((r) => ({
          country: r.country,
          city: r.city,
          zipCode: r.zipCode,
          provinceState: r.provinceState,
          address: r.address
        })),
        distinctUntilChanged(_.isEqual),
        mergeMap((r) => {
          const countryCode = countries.getCountryCode(r.country) || 'IT';

          return this.shippingService.getShipmentCost({
            country: countryCode.toUpperCase(), 
            state: r.provinceState,
            city: r.city,
            street: r.address,
            zipCode: r.zipCode,
          });
        }),
      )
      .subscribe({
        next: (r) => {
          this.shipmentCost = r.costEur;
          this.total = this.cartValue.plus(this.shipmentCost);
        }
      })
  }

  public emptyCart : boolean = false

  submitForm(): void {
    if (this.checkoutService.checkingOut) {
      return;
    }

    if (this.checkoutForm.valid) {
      const country = this.checkoutForm.get('country')?.value ?? 'IT';
      const countryCode = countries.getCountryCode(country) || 'IT';

      this.checkoutService.checkout(
        this.cartService.getItems(),
        {
          type: "credit_card",
          number: this.checkoutForm.get('creditCard')?.value,
          validMonth: Number(this.checkoutForm.get('creditCardExp')?.value.split('/')[0]),
          validYear: Number(this.checkoutForm.get('creditCardExp')?.value.split('/')[1]),
          address: this.checkoutForm.get('address')?.value,
          tenant: this.checkoutForm.get('creditCardTenant')?.value,
          csc: Number(this.checkoutForm.get('creditCardCVC')?.value)
        },
        {
          country: countryCode,
          state: this.checkoutForm.get('provinceState')?.value,
          city: this.checkoutForm.get('city')?.value,
          street: this.checkoutForm.get('address')?.value,
          zipCode: this.checkoutForm.get('zipCode')?.value,
          recipientName: `${this.checkoutForm.get('firstName')?.value} ${this.checkoutForm.get('lastName')?.value}`
        },
        this.shipmentCost,
        this.cartService.cartVersion
      ).subscribe(result => {
        switch (result.type) {
          case 'success':
            this.toastService.showSuccess("checkout-success")
            this.clearForm()
            break
          case 'success_but_cart_not_cleared':
            this.toastService.showSuccess("checkout-success-but-cart-not-cleared")
            this.clearForm()
            break
          case 'not_enough_stocks':
            this.toastService.showError("checkout-error-stocks-changed")
            break
          case 'other_error':
            this.toastService.showError("checkout-generic-error")
            break
          case 'items_price_changed':
            this.toastService.showError("checkout-error-price-changed")
            break
          case 'stock_not_on_sale':
            this.toastService.showError("checkout-error-stocks-changed")
            break
          case 'shipment_cost_changed':
            this.toastService.showError("checkout-error-shipment-cost-changed")
            break
        }

        this.cartService.reloadCart()
      })
    }
  }
}
