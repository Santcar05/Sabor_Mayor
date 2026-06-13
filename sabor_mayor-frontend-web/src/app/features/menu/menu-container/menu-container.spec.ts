import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuContainer } from './menu-container';

describe('MenuContainer', () => {
  let component: MenuContainer;
  let fixture: ComponentFixture<MenuContainer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MenuContainer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuContainer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
