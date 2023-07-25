package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/*
* Comprender la prueba.
* Entendamos varios elementos importantes de esta prueba.
* @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT).
* Esto iniciará nuestra aplicación Spring Boot y la hará disponible para que nuestro test realice peticiones a ella.
* @Autowired
* TestRestTemplate restTemplate;
* Hemos pedido a Spring que inyecte un ayudante de test que nos permita realizar peticiones HTTP a la aplicación que se
* ejecuta localmente.
* Ten en cuenta que aunque @Autowired es una forma de inyección de dependencias de Spring, es mejor utilizarlo sólo en tests.
* Hablaremos de ello con más detalle más adelante.
* ResponseEntity<String> response = restTemplate.getForEntity("/tarjetas/99", String.class);
* Aquí utilizamos restTemplate para realizar una petición HTTP GET al endpoint /cashcards/99 de nuestra aplicación.
* restTemplate devolverá una ResponseEntity, que hemos capturado en una variable que hemos llamado response.
* ResponseEntity es otro útil objeto de Spring que proporciona valiosa información sobre lo que ha ocurrido con nuestra petición.
* Utilizaremos esta información a lo largo de nuestras pruebas en este curso.
* assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); Podemos inspeccionar muchos aspectos de la respuesta,
* incluyendo el código de Estado de Respuesta HTTP, que esperamos que sea 200 OK.
* */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	/*
	*Comprender el fracaso de la prueba.
	* Como ya hemos explicado, esperábamos que nuestra prueba fallara.
	* ¿Por qué falla debido a un inesperado código de respuesta HTTP 404 NOT_FOUND?
	* Respuesta: como no hemos indicado a Spring Web cómo manejar GET cashcards/99,
	* Spring Web está respondiendo automáticamente que el endpoint es NOT_FOUND.
	* Gracias por encargarse de ello, Spring Web.
	 */

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		//Comprender las adiciones.
		//
		// DocumentContext documentContext = JsonPath.parse(response.getBody());
		//Esto convierte la cadena de respuesta en un objeto JSON con muchos métodos de ayuda.
		//
		// Número id = documentContext.read("$.id");
		// assertThat(id).isNotNull();
		//Esperamos que cuando solicitemos una tarjeta de débito con id 99 se devuelva un objeto JSON con algo en el campo id.
		// Por ahora aseguremos que el id no es nulo.

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}


	//Entienda la prueba.
	//
	//CashCard newCashCard = new CashCard(null, 250.00);
	//La base de datos creará y gestionará todos los valores únicos de CashCard.id por nosotros.
	// No debemos proporcionar ninguno.
	//
	//restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
	//Esto es muy similar a restTemplate.getForEntity, pero también debemos proporcionar los datos de newCashCard
	// para la nueva CashCard.
	//
	//Además, y a diferencia de restTemplate.getForEntity, no esperamos que se nos devuelva una CashCard,
	// por lo que esperamos un cuerpo de respuesta Void.

	//Comprenda las actualizaciones de las pruebas.
	//
	//Hemos realizado bastantes cambios. Repasémoslos.
	//
	//assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	//Según la especificación oficial
	//
	//el servidor de origen DEBERÍA enviar una respuesta 201 (Creado) ...
	//
	//Ahora esperamos que el código de estado de respuesta HTTP sea 201 CREATED,
	// que es semánticamente correcto si nuestra API crea una nueva CashCard a partir de nuestra solicitud.
	//
	//URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
	//La especificación oficial sigue indicando lo siguiente
	//
	//enviar una respuesta 201 (Created) que contenga un campo de cabecera Location que
	// proporcione un identificador para el recurso primario creado ...
	//
	//En otras palabras, cuando una solicitud POST resulta en la creación exitosa de un recurso, como una nueva CashCard,
	// la respuesta debe incluir información sobre cómo recuperar ese recurso.
	// Lo haremos proporcionando un URI en una cabecera de respuesta llamada "Location".
	//
	//Tenga en cuenta que URI es la entidad correcta aquí y no una URL; una URL es un tipo de URI,
	// mientras que una URI es más genérica.
	//
	//ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
	//assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	//Por último, utilizaremos la información de la cabecera Location para obtener la tarjeta CashCard recién creada.
	@Test
	//@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Add assertions such as these
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}
}
