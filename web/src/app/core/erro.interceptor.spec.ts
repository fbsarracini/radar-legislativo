import { HttpErrorResponse } from '@angular/common/http';
import { mensagemDeErro } from './erro.interceptor';

describe('mensagemDeErro', () => {
  it('deve usar a mensagem e os erros do ApiError', () => {
    const erro = new HttpErrorResponse({
      status: 400,
      error: { status: 400, message: 'parâmetro inválido', errors: ['uf: deve ter 2 letras'] },
    });
    expect(mensagemDeErro(erro)).toBe('parâmetro inválido: uf: deve ter 2 letras');
  });

  it('deve explicar quando a API não responde', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain('backend está rodando');
  });

  it('deve explicar o 502 sem corpo como casa fora do ar', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 502 }))).toContain('fora do ar');
  });
});
