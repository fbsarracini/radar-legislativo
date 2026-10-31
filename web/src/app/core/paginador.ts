import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

/** Textos do mat-paginator em português. */
@Injectable()
export class PaginadorEmPortugues extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Por página';
  override nextPageLabel = 'Próxima página';
  override previousPageLabel = 'Página anterior';
  override firstPageLabel = 'Primeira página';
  override lastPageLabel = 'Última página';
  override getRangeLabel = (pagina: number, tamanho: number, total: number): string => {
    if (total === 0) return '0 de 0';
    const inicio = pagina * tamanho + 1;
    const fim = Math.min(total, (pagina + 1) * tamanho);
    return `${inicio}–${fim} de ${total}`;
  };
}
