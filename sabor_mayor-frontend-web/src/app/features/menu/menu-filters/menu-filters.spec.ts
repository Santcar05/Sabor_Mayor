import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuFilters } from './menu-filters';

describe('MenuFilters', () => {
  let component: MenuFilters;
  let fixture: ComponentFixture<MenuFilters>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuFilters]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuFilters);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
