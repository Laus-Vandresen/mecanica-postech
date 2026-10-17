package br.com.mecanica.service;

import br.com.mecanica.dto.ClienteRequest;
import br.com.mecanica.dto.ClienteResponse;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.repository.*;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository repository;

    public List<ClienteResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(ClienteResponse::from).toList();
    }

    public ClienteResponse buscar(Long id) {
        return ClienteResponse.from(encontrar(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        var entity = new ClienteEntity();
        preencher(entity, request);
        return ClienteResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        var entity = encontrar(id);
        preencher(entity, request);
        return ClienteResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private ClienteEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));
    }

    private void preencher(ClienteEntity entity, ClienteRequest request) {
        entity.setNome(request.nome());
        entity.setCelular(request.celular());
        entity.setEmail(request.email());
        entity.setCpf(request.cpf());
    }
}
