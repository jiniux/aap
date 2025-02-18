import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core"
import { HttpClient, provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import { LandingComponent } from './landing/landing.component';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';
import {ReactiveFormsModule} from '@angular/forms';
import { LandingSearchResultComponent } from './book-search-result/book-search-result.component';
import { BookOverviewComponent } from './book-overview/book-overview.component';
import { BookSearchComponent } from './book-search/book-search.component';
import { LoadingProgressBarComponent } from './loading-progress-bar/loading-progress-bar.component';
import { ToastComponent } from './toast/toast.component';
import { ToastListComponent } from './toast-list/toast-list.component';
import { AuthButtonComponent } from './auth-button.component';
import { AuthHttpInterceptor, authHttpInterceptorFn, AuthModule, provideAuth0 } from '@auth0/auth0-angular';

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, "/assets/i18n/", ".json");
}

@NgModule({
  declarations: [
    AppComponent,
    LandingComponent,
    LandingSearchResultComponent,
    BookOverviewComponent,
    BookSearchComponent,
    LoadingProgressBarComponent,
    ToastComponent,
    ToastListComponent,
    AuthButtonComponent,
  ],
  imports: [
    AuthModule.forRoot({
      domain: 'aap-demo.eu.auth0.com',
      clientId: '6YiRY96DuOjglFqapEZlETHCP4YQBhxj',
      authorizationParams: {
        scope: 'openid profile email sync:shopping-cart',
        audience: 'http://localhost:4200/api',
        redirect_uri: window.location.origin
      },
      httpInterceptor: {
        allowedList: [
          {
            uri: '/api/*'
          }
        ]
      },
      cacheLocation: 'localstorage'
    }),
    BrowserModule,
    AppRoutingModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    FontAwesomeModule,
    ReactiveFormsModule
  ],
  providers: [
    AuthHttpInterceptor,
    provideHttpClient(withInterceptors([authHttpInterceptorFn])),
  ],
  bootstrap: [AppComponent]
})
export class AppModule { 
  constructor(translate: TranslateService, library: FaIconLibrary) { 
    translate.setDefaultLang('en');

    library.addIconPacks(fas); 
    library.addIconPacks(far); 

    translate.use('it');
  }
}
