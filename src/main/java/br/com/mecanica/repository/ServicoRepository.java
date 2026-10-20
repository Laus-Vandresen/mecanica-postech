package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<ServicoEntity, UUID> {
}
