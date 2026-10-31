package br.com.radarlegislativo.proposicao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColetaRepository extends JpaRepository<Coleta, Long> {

    Optional<Coleta> findFirstByFonteAndStatusOrderByAteDesc(String fonte, Coleta.Status status);

    List<Coleta> findTop20ByOrderByIniciadaEmDesc();
}
