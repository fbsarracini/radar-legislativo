import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './api.models';

/** Traduz o ApiError do backend numa mensagem curta para quem está usando. */
export function mensagemDeErro(erro: HttpErrorResponse): string {
  if (erro.status === 0) return 'Sem conexão com a API. O backend está rodando?';
  const corpo = erro.error as Partial<ApiError> | null;
  const detalhe = corpo?.errors?.length ? `: ${corpo.errors.join('; ')}` : '';
  if (corpo?.message) return `${corpo.message}${detalhe}`;
  if (erro.status === 502)
    return 'A API de uma casa legislativa está fora do ar. Tente de novo em instantes.';
  return `Erro ${erro.status} ao falar com a API.`;
}

/** Mostra o erro num snackbar e repassa: a tela ainda decide o que exibir no lugar. */
export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  return next(req).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status !== 404) {
        snackBar.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
      return throwError(() => erro);
    }),
  );
};
