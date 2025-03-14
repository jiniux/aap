import { Component, OnInit, HostListener, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@auth0/auth0-angular';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-tooltip-bar',
  standalone: false,
  
  templateUrl: './tooltip-bar.component.html',
  styleUrl: './tooltip-bar.component.css'
})
export class TooltipBarComponent implements OnInit {
  loggedIn = false
  userFirstName = ''
  showLogoutMenu = false;
  currentLang = 'it'; // Default language

  constructor(
    private readonly authService: AuthService,
    private readonly eRef: ElementRef,
    private readonly router: Router,
    private readonly translateService: TranslateService
  ) {}

  openCart() {
    this.router.navigate(['/cart']);
  }

  userClickedUserSection() {
    if (!this.loggedIn) {
      this.authService.loginWithRedirect()
    } else {
      this.showLogoutMenu = !this.showLogoutMenu;
    }
  }

  logout() {
    this.authService.logout({ logoutParams: { returnTo: window.location.origin } });
  }

  openOrders() {
    this.router.navigate(['/orders']);
  }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.showLogoutMenu = false;
    }
  }

  toggleLanguage() {
    const newLang = this.currentLang === 'it' ? 'en' : 'it';
    this.translateService.use(newLang);
    this.currentLang = newLang;
    localStorage.setItem('language', newLang);
  }

  ngOnInit(): void {
    this.authService.isAuthenticated$.subscribe((isAuthenticated) => {
      this.loggedIn = isAuthenticated;
    });

    this.authService.user$.subscribe((user) => {
      if (user !== null && user !== undefined) {
        this.userFirstName = user.given_name ?? user.name ?? user.nickname ?? user.email ?? 'unknown';
      }
    });
    
    // Load language from localStorage
    const storedLang = localStorage.getItem('language');
    if (storedLang) {
      this.translateService.use(storedLang);
      this.currentLang = storedLang;
    } else {
      this.currentLang = this.translateService.currentLang;
    }
  }
}
