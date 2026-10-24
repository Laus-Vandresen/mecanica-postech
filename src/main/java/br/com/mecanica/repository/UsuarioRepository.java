package br.com.mecanica.repository;

import java.util.Optional;
import java.util.UUID;
import br.com.mecanica.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByLogin(String login);

    boolean existsByLogin(String login);

}
