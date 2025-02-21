import { Component, OnInit } from '@angular/core';
import { CartService } from './cart.service';
import { Router } from '@angular/router';
import { CartToastNotifierService } from './cart-toast-notifier.service';
import { AuthService } from '@auth0/auth0-angular';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  constructor(
    private readonly cartService: CartService, 
    private readonly cartToastNotifierService: CartToastNotifierService,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}
  
  ngOnInit(): void {
    this.authService.isAuthenticated$.subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.cartToastNotifierService.start();
        this.cartService.reloadCart();
      }
    })
  }

  navigateToHome() {
    this.router.navigate(['/']);
  }

  title = 'aap';
}
