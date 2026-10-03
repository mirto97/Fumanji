import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Pawn } from './pawn';

describe('Pawn', () => {
  let component: Pawn;
  let fixture: ComponentFixture<Pawn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pawn],
    }).compileComponents();

    fixture = TestBed.createComponent(Pawn);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
