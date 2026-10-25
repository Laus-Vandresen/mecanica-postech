package br.com.mecanica.repository;

import br.com.mecanica.entity.OrdemServicoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    @Query("""
            SELECT ordem FROM OrdemServicoEntity ordem
            LEFT JOIN ordem.cliente cliente
            WHERE (:clienteId IS NULL OR cliente.id = :clienteId)
              AND (:veiculoId IS NULL OR ordem.veiculo.id = :veiculoId)
              AND (:status IS NULL OR ordem.status = :status)
              AND (:fila IS NULL
                   OR (:fila = 'DIAGNOSTICO' AND ordem.status = 'RECEBIDA')
                   OR (:fila = 'EXECUCAO' AND ordem.status = 'AGUARDANDO_APROVACAO'
                       AND ordem.orcamentoAprovado = TRUE)
                   OR (:fila = 'FINALIZACAO' AND ordem.status = 'EM_EXECUCAO'
                       AND ordem.dataConclusaoExecucao IS NOT NULL))
            """)
    List<OrdemServicoEntity> buscarComFiltros(
            @Param("clienteId") UUID clienteId,
            @Param("veiculoId") UUID veiculoId,
            @Param("status") String status,
            @Param("fila") String fila,
            Pageable paginacao);

    List<OrdemServicoEntity> findAllByDataFechamentoIsNotNull();
}
