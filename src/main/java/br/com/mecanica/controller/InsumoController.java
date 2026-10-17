package br.com.mecanica.controller;

import br.com.mecanica.dto.InsumoRequest;
import br.com.mecanica.dto.InsumoResponse;
import br.com.mecanica.service.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/insumos")
@RequiredArgsConstructor
public class InsumoController {
    private final InsumoService service;

    @PostMapping
    public ResponseEntity<InsumoResponse> criar(@Valid @RequestBody InsumoRequest request) {
        var response = service.criar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<InsumoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InsumoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public InsumoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
