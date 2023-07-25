package com.example.cashcard;

import org.springframework.http.ResponseEntity;

//¿Qué esperamos que ocurra cuando volvamos a ejecutar las pruebas?
//
//esperado: 200 OK
// pero fue 404 NOT_FOUND
//¡Mismo resultado! ¿Por qué?
//
//A pesar del nombre, CashCardController no es realmente un Controlador Web de Spring; es sólo una clase con Controller en el nombre. Por tanto, no está "escuchando" nuestras peticiones HTTP. Así que lo siguiente que tenemos que hacer es decirle a Spring que haga que el Controlador esté disponible como Controlador Web para gestionar las peticiones a las URLs cashcards/*.
//
//Traducción realizada con la versión gratuita del traductor www.DeepL.com/Translator


public class CashCardController {

    public ResponseEntity<String> findById(){
        return ResponseEntity.ok("{}");
    }
}
