package com.example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.util.List;
import java.util.Optional;

//¿Qué esperamos que ocurra cuando volvamos a ejecutar las pruebas?
//
//esperado: 200 OK
// pero fue 404 NOT_FOUND
//¡Mismo resultado! ¿Por qué?
//
//A pesar del nombre, CashCardController no es realmente un Controlador Web de Spring; es sólo una clase con Controller en el nombre. Por tanto, no está "escuchando" nuestras peticiones HTTP. Así que lo siguiente que tenemos que hacer es decirle a Spring que haga que el Controlador esté disponible como Controlador Web para gestionar las peticiones a las URLs cashcards/*.
//
//Traducción realizada con la versión gratuita del traductor www.DeepL.com/Translator

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    //Comprender las anotaciones de Spring Web.
    //
    //Revisemos nuestras anotaciones.
    //
    // @RestController
    //Esto le dice a Spring que esta clase es un Componente de tipo RestController y capaz de manejar peticiones HTTP.
    //
    // @RequestMapping("/tarjetas")
    //Es un complemento de @RestController que indica qué dirección deben tener las peticiones para acceder a este Controlador.
    //
    // @GetMapping("/{requestedId}")
    // public ResponseEntity<String> findById() {...}
    //@GetMapping marca un método como método manejador. Las peticiones GET que coincidan con cashcards/{requestedID}
    // serán gestionadas por este método.
    private CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")

    //Para ello, en primer lugar hacer que el controlador consciente de la variable de ruta que estamos presentando
    // mediante la adición de la anotación @PathVariable al argumento del método controlador.
    //@PathVariable hace que Spring Web conozca el requestedId suministrado en la petición HTTP.
    // Ahora está disponible para que lo utilicemos en nuestro método handler.

    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        Optional<CashCard> cashCardOPtional = cashCardRepository.findById(requestedId);
        if (cashCardOPtional.isPresent()) {
            return ResponseEntity.ok(cashCardOPtional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //Esta línea en CashCardController.createCashCard es engañosamente simple:
    //
    //CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
    //Como hemos aprendido en lecciones y laboratorios anteriores,
    // CrudRepository de Spring Data proporciona métodos que permiten crear, leer,
    // actualizar y eliminar datos de un almacén de datos. cashCardRepository.save(newCashCardRequest)
    // hace exactamente lo que dice: nos guarda una nueva CashCard, y devuelve el objeto guardado con un id único
    // proporcionado por la base de datos. ¡Increíble!

    //Entender los otros cambios a CashCardController
    //Nuestro CashCardController ahora implementa la entrada y los resultados esperados de un HTTP POST.
    //
    //createCashCard(@RequestBody CashCard newCashCardRequest, ...)
    //A diferencia del GET que añadimos antes, el POST espera un "cuerpo" de solicitud.
    // Éste contiene los datos enviados a la API. Spring Web deserializará los datos en una CashCard para nosotros.
    //
    //URI locationOfNewCashCard = ucb
    //   .path("cashcards/{id}")
    //   .buildAndExpand(savedCashCard.id())
    //   .toUri();
    //Esto construye un URI para la CashCard recién creada. Este es el URI que el emisor de la llamada puede utilizar
    // para OBTENER la CashCard recién creada.
    //
    //Tenga en cuenta que se utiliza savedCashCard.id como identificador, lo que coincide con la especificación del
    // punto final GET de cashcards/<CashCard.id>.
    //
    //¿De dónde procede UriComponentsBuilder?
    //
    //Hemos podido añadir UriComponentsBuilder ucb como argumento de método a este método POST handler y se ha pasado
    // automáticamente. ¿Cómo? Fue inyectado desde nuestro ya familiar amigo, el Contenedor IoC de Spring.
    // ¡Gracias, Spring Web!
    //
    //return ResponseEntity.created(locationOfNewCashCard).build();
    //Finalmente, devolvemos 201 CREATED con la cabecera Location correcta.
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest,
                                                UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();

        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    //Comprender el código de paginación.
    //
    //findAll(Pageable pageable)
    //Pageable es otro objeto que Spring Web pone a nuestra disposición.
    // Como hemos especificado los parámetros URI de page=0&size=1, pageable contendrá los valores que necesitamos.
    //
    //PageRequest.of(
    //  pageable.getPageNumber(),
    //  pageable.getPageSize()
    //));
    //PageRequest es una implementación Java Bean básica de Pageable. Las cosas que quieren una implementación
    // de paginación y ordenación a menudo soportan esto, como algunos tipos de Repositorios de Datos de Spring.
    //
    //¿Soporta ya nuestro CashCardRepository paginación y ordenación? Averigüémoslo.

    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize()));
        return ResponseEntity.ok(page.getContent());
    }
}
