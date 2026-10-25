package br.com.mecanica.repository;

import java.util.UUID;
import br.com.mecanica.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<ClienteEntity, UUID> {
    java.util.Optional<ClienteEntity> findByCpf(String cpf);
}
