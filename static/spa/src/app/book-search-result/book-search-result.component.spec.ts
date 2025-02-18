import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LandingSearchResultComponent } from './book-search-result.component';

describe('LandingSearchResultComponent', () => {
  let component: LandingSearchResultComponent;
  let fixture: ComponentFixture<LandingSearchResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LandingSearchResultComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LandingSearchResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
