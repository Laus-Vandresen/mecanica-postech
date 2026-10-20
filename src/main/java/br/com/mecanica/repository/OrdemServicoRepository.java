package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, UUID> {
}
