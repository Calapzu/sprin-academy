package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/1000", String.class);

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
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00, "sarah1");
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Add assertions such as these
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}

	//Comprender la prueba.
	//
	//documentContext.read("$.length()");
	//...
	//documentContext.read("$..id");
	//...
	//documentContext.read("$..amount");
	//¡Echa un vistazo a estas nuevas expresiones JsonPath!
	//
	//documentContext.read("$.length()") calcula la longitud de la matriz.
	//
	//.read("$..id") recupera la lista de todos los valores de id devueltos, mientras que .read("$..amount")
	// recoge todos los importes devueltos.
	//
	//Para aprender más sobre JsonPath, un buen lugar para empezar es aquí en la documentación de JsonPath.
	//
	//assertThat(...).containsExactlyInAnyOrder(...)
	//No hemos garantizado el orden de la lista de CashCard -- salen en el orden que la base de datos decida devolverlas.
	// Como no especificamos el orden, containsExactlyInAnyOrder(...)
	// afirma que aunque la lista debe contener todo lo que afirmamos, el orden no importa.
	@Test
	void shouldReturnAllCashCardsWhenListIsRequested() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
	}

	//Comprenda la prueba.
	//
	//El URI que estamos solicitando contiene información tanto de paginación como de
	// ordenación: /tarjetas?page=0&size=1&sort=cantidad,desc
	//
	//page=0: Obtiene la primera página. Los índices de página empiezan en 0.
	//size=1: Cada página tiene tamaño 1.
	//sort=importe,desc
	//La extracción de datos (¡usando más JSONPath!) y las aserciones que la acompañan esperan que la
	// tarjeta Cash Card devuelta sea la de 150,00 dólares.
	//
	//¿Crees que la prueba pasará? Antes de ejecutarla, intenta averiguar si pasará o no. Si crees que no pasará,
	// ¿dónde crees que estará el fallo?
	@Test
	void shouldReturnASortedPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}

	@Test
	void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth("sarah1", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-owns-no-cards", "qrs456")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/102", String.class); // kumar2's data
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	//Momento de aprendizaje: ¿qué pasa con restTemplate.exchange()?
	//¿Te has dado cuenta de que no estamos utilizando RestTemplate de la misma forma que en nuestras pruebas anteriores?
	//
	//Todas las demás pruebas utilizan métodos RestTemplate.xyzForEntity() como getForEntity() y postForEntity().
	//
	//Entonces, ¿por qué no seguimos el mismo patrón utilizando putForEntity()?
	//
	//Respuesta: putForEntity() no existe. Lee más al respecto aquí en el tema de GitHub sobre el tema.
	//
	//Afortunadamente RestTemplate soporta múltiples formas de interactuar con las APIs REST, como RestTemplate.exchange().
	//
	//Conozcamos ahora RestTemplate.exchange().
	//
	//Entender RestTemplate.exchange().
	//
	//Entendamos mejor lo que ocurre.
	//
	//El método exchange() es una versión más general de los métodos xyzForEntity() que hemos utilizado en otras pruebas:
	// exchange() requiere que se suministren como parámetros el verbo y la entidad de la petición
	// (el cuerpo de la petición).
	//
	//Utilizando getForEntity() como ejemplo, puede imaginarse que las dos líneas de código siguientes consiguen
	// }el mismo objetivo:
	//
	//.exchange("/tarjetas/99", HttpMethod.GET, new HttpEntity(null), String.class);
	//
	//La línea anterior es funcionalmente equivalente a la siguiente
	//
	//.getForEntity("/tarjetas/99", String.class);
	//
	//Ahora vamos a explicar el código de prueba.
	//
	//Primero creamos la HttpEntity que necesita el método exchange():
	//
	//HttpEntity<CashCard> request = new HttpEntity<>(existingCashCard);
	//A continuación, llamamos a exchange(), que envía una solicitud PUT para el ID de destino de 99 y los
	// datos actualizados de Cash Card:
	//.exchange("/tarjetas/99", HttpMethod.PUT, request, Void.class);


	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotExist() {
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	//¿Por qué no utilizar RestTemplate.delete()?
	//Observe que estamos utilizando RestTemplate.exchange() a pesar de que RestTemplate proporciona un método que
	// parece que podríamos utilizar: RestTemplate.delete(). Sin embargo, veamos la firma:
	//
	//public class RestTemplate ... {
	//    public void delete(String url, Object... uriVariables)
	//Los otros métodos que hemos estado usando (como getForEntity() e exchange()) devuelven una ResponseEntity,
	// pero delete() no. En su lugar, es un método void. ¿A qué se debe esto?
	//
	//El framework Spring Web proporciona el método delete() como una conveniencia, pero viene con algunas suposiciones:
	//
	//Una respuesta a una petición DELETE no tendrá cuerpo.
	//Al cliente no debería importarle el código de respuesta a menos que sea un error, en cuyo caso lanzará una excepción.
	//Dadas estas suposiciones, no se necesita un valor de retorno de delete().
	//
	//Pero la segunda suposición hace que delete() no sea adecuada para nosotros: Necesitamos la ResponseEntity
	// para hacer una afirmación sobre el código de estado. Así que no usaremos el método de conveniencia,
	// sino que usaremos el método más general: exchange().
	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("kumar2", "xyz789")
				.getForEntity("/cashcards/102", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
