import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuSearch } from './menu-search';

describe('MenuSearch', () => {
  let component: MenuSearch;
  let fixture: ComponentFixture<MenuSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuSearch]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuSearch);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
