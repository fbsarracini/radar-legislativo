import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  it('deve mostrar a navegação entre feed e candidatos', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const links = [...(fixture.nativeElement as HTMLElement).querySelectorAll('nav a')];
    expect(links.map((a) => a.getAttribute('href'))).toEqual(['/proposicoes', '/candidatos']);
  });
});
