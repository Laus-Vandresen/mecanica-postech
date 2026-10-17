package br.com.mecanica.controller;

import br.com.mecanica.dto.PecaRequest;
import br.com.mecanica.dto.PecaResponse;
import br.com.mecanica.service.PecaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/pecas")
@RequiredArgsConstructor
public class PecaController {
    private final PecaService service;

    @PostMapping
    public ResponseEntity<PecaResponse> criar(@Valid @RequestBody PecaRequest request) {
        var response = service.criar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<PecaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PecaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public PecaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PecaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
