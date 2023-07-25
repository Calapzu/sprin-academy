package com.example.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/*
La anotación @Test forma parte de la librería JUnit, y el método assertThat forma parte de la librería AssertJ.
Ambas bibliotecas se importan después de la sentencia package.
Una convención común (pero no un requisito) es utilizar siempre el sufijo Test para las clases de prueba.
Así lo hemos hecho aquí. El nombre completo de la clase CashCardJsonTest nos da una pista sobre la naturaleza de la
prueba que vamos a escribir. Al estilo Test-First, hemos escrito primero una prueba que falla.
Es importante tener primero una prueba que falla para que puedas tener una alta confianza en que lo que hiciste para
arreglar la prueba realmente funcionó. No se preocupe de que la prueba (que afirma que 1 es igual a 42), así como el
nombre del método de prueba, parezcan extraños. Estamos a punto de cambiarlos.
*/

@JsonTest
public class CashCardJsonTest {
    /*
    * La anotación @JsonTest marca CashCardJsonTest como una clase de prueba que utiliza el framework Jackson
    * (que se incluye como parte de Spring). Esto proporciona un amplio soporte de pruebas y análisis JSON.
    * También establece todo el comportamiento relacionado para probar objetos JSON.
    * JacksonTester es una envoltura conveniente para la biblioteca de análisis JSON Jackson.
    * Maneja la serialización y deserialización de objetos JSON.
    * @Autowired es una anotación que indica a Spring que cree un objeto del tipo solicitado.
    * */
    @Autowired
    private JacksonTester<CashCard> json;

    @Test
    public void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);

        //assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");

        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");

        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);

        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");

        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    /*
    * La deserialización es el proceso inverso a la serialización.
    * Transforma los datos de un archivo o flujo de bytes de nuevo en un objeto para su aplicación.
    * Esto hace posible que un objeto serializado en una plataforma sea deserializado en una plataforma diferente.
    * Por ejemplo, tu aplicación cliente puede serializar un objeto en Windows mientras que el backend lo deserializaría en Linux.
    * La serialización y la deserialización trabajan juntas para transformar/crear objetos de datos a/desde un formato portable.
    * El formato más popular para serializar datos es JSON. Escribamos una segunda prueba para deserializar datos
    * de forma que se conviertan de JSON a Java después de que pase la primera prueba.
    * Esta prueba utiliza una técnica de "primero la prueba" en la que escribes a propósito una prueba que falla.
    * En concreto: los valores de id y cantidad no son los esperados.
    * */

    @Test
    public void cashCardDeserializationTest() throws IOException {
        String expected = """
           {
               "id":99,
               "amount":123.45
           }
           """;
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45));
        assertThat(json.parseObject(expected).id()).isEqualTo(99L);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

    @Test
    public void myDirstTest(){
        assertThat(42).isEqualTo(42);
    }
}
