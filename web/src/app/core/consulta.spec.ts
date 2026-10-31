import { convertToParamMap } from '@angular/router';
import { paginaDaRota, paramsDaRota, semVazios } from './consulta';

describe('consulta', () => {
  it('deve tirar da URL os filtros vazios e aparar o texto', () => {
    expect(
      semVazios({
        uf: 'SP',
        partido: '  ',
        autor: ' rosario ',
        tipo: [],
        fonte: null,
        novas: false,
        pagina: 0,
      }),
    ).toEqual({ uf: 'SP', autor: 'rosario', novas: false, pagina: 0 });
  });

  it('deve repassar à API só as chaves conhecidas, inclusive as repetidas', () => {
    const rota = convertToParamMap({
      tipo: ['PL', 'PEC'],
      uf: 'SP',
      novas: 'true',
      lixo: 'x',
      autor: '',
    });
    expect(paramsDaRota(rota, ['tipo', 'uf', 'autor']).toString()).toBe('tipo=PL&tipo=PEC&uf=SP');
  });

  it('deve ler a página da URL e cair na primeira quando vier lixo', () => {
    expect(paginaDaRota(convertToParamMap({ pagina: '3' }))).toBe(3);
    expect(paginaDaRota(convertToParamMap({ pagina: '-1' }))).toBe(0);
    expect(paginaDaRota(convertToParamMap({ pagina: 'abc' }))).toBe(0);
    expect(paginaDaRota(convertToParamMap({}))).toBe(0);
  });
});
