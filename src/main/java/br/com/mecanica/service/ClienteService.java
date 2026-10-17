package br.com.mecanica.service;

import br.com.mecanica.entity.mapper.ClienteEntityMapper;
import br.com.mecanica.domain.entity.Cliente;
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

    public List<Cliente> listar() {
        return repository.findAll(Sort.by("id")).stream().map(ClienteEntityMapper::toDomain).toList();
    }

    public Cliente buscar(Long id) {
        return ClienteEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        return ClienteEntityMapper.toDomain(repository.saveAndFlush(ClienteEntityMapper.toEntity(cliente)));
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente cliente) {
        var entity = encontrar(id);
        ClienteEntityMapper.updateEntity(cliente, entity);
        return ClienteEntityMapper.toDomain(repository.saveAndFlush(entity));
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
}
