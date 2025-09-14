package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.Libro;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
public class CatalogoController {

    private final CatalogoService catalogoService;

    @Autowired
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(
            summary = "Obtener un libro por ID",
            description = "Devuelve la información de un libro específico mediante su ID. "
                    + "Disponible para usuarios y bibliotecarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Libro.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el libro", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_USER')")
    public Libro obtenerLibro(
            @Parameter(description = "ID del libro a consultar", required = true, example = "LIB123")
            @PathVariable String id) {
        return catalogoService.obtenerLibro(new LibroId(id));
    }

    @Operation(
            summary = "Verificar disponibilidad de un libro",
            description = "Permite a un bibliotecario verificar si un libro está disponible para préstamo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró el libro", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ROLE_LIBRARIAN)", content = @Content)
    })
    @GetMapping("/{id}/disponible")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public boolean isLibroDisponible(
            @Parameter(description = "ID del libro a verificar", required = true, example = "LIB123")
            @PathVariable String id) {
        Libro libro = catalogoService.obtenerLibro(new LibroId(id));
        return libro != null && libro.isDisponible();
    }

    @Operation(
            summary = "Actualizar disponibilidad de un libro",
            description = "Permite a un bibliotecario actualizar el estado de disponibilidad de un libro específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ROLE_LIBRARIAN)", content = @Content)
    })
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public void actualizarDisponibilidad(
            @Parameter(description = "ID del libro a actualizar", required = true, example = "LIB123")
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado de disponibilidad (true = disponible, false = no disponible)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Disponible", value = "true"),
                                    @ExampleObject(name = "No disponible", value = "false")
                            })
            )
            @RequestBody boolean disponible) {
        catalogoService.actualizarDisponibilidad(new LibroId(id), disponible);
    }

    @Operation(
            summary = "Buscar libros por criterio",
            description = "Permite buscar libros en el catálogo mediante un criterio (ejemplo: título, autor, género)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Libro.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_USER')")
    public List<Libro> buscarLibros(
            @Parameter(description = "Texto a buscar en el catálogo", required = true, example = "Cien años de soledad")
            @RequestParam String criterio) {
        return catalogoService.buscarLibros(criterio);
    }
}
