package br.com.mecanica.repository;

import java.util.Optional;
import java.util.UUID;
import br.com.mecanica.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.*;

public interface VeiculoRepository extends JpaRepository<VeiculoEntity, UUID> {

    @Query("select v from VeiculoEntity v where v.id = :id")
    Optional<VeiculoEntity> buscarParaVincular(@org.springframework.data.repository.query.Param("id") UUID id);

}
