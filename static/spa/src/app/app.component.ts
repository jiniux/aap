import { Component, OnInit } from '@angular/core';
import { CartService } from './cart.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  constructor(private readonly cartService: CartService) {}
  
  ngOnInit(): void {
    this.cartService.
  }

  title = 'aap';
}
