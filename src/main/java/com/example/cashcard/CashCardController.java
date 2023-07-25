package com.example.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{requestedId}")
    //Para ello, en primer lugar hacer que el controlador consciente de la variable de ruta que estamos presentando
    // mediante la adición de la anotación @PathVariable al argumento del método controlador.
    //@PathVariable hace que Spring Web conozca el requestedId suministrado en la petición HTTP.
    // Ahora está disponible para que lo utilicemos en nuestro método handler.
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId){
        if (requestedId.equals(99L)) {
            CashCard cashCard = new CashCard(99L, 123.45);
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
