package es.um.sisdist.backend.grpc.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.sisdist.backend.grpc.TicketRequest;
import es.um.sisdist.backend.grpc.TicketResponse;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;

    public GrpcServiceImpl(Logger logger) 
    {
		super();
		this.logger = logger;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) 
	{
		logger.info("Recived PING request, value = " + request.getV());
		responseObserver.onNext(PingResponse.newBuilder().setV(request.getV()).build());
		responseObserver.onCompleted();
	}


	private void healthCheck() {
		System.out.println("Ha llegado al health check correctamente");
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest aliveCheckRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://ssdd-llamachat:5020/healthcheck")) 
		.GET()
		.build();

		HttpResponse<Void> aliveCheckResponse;
		try {
			aliveCheckResponse = client.send(aliveCheckRequest, HttpResponse.BodyHandlers.discarding());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int status = aliveCheckResponse.statusCode();

		while( status == 204 ){

			try {
				aliveCheckResponse = client.send(aliveCheckRequest, HttpResponse.BodyHandlers.discarding());
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			status = aliveCheckResponse.statusCode();
		}
	}

	@Override
	public void preguntarLlama(PromptRequest request, StreamObserver<TicketResponse> responseObserver) 
	{
		System.out.println("Llega hasta el impl de gRPC");
		String userPrompt = request.getPromptRequest();
		String userId = request.getIdUser();

		// Se comprueban el JWT del usuario, etc.


		//

		HttpClient client = HttpClient.newHttpClient();
		String jsonBody = "{\"prompt\": \"" + userPrompt + "\"}";
		
		// Intentamos enviar la petición de un nuevo prompt al servidor Llama:

		HttpRequest promptRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://ssdd-llamachat:5020/prompt")) 
		.header("Content-Type", "application/json")
		.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
		.build();

		HttpResponse<String> promptResponse;
		try {
			// Comprobamos si el servicio Llama está disponible.
			healthCheck();
			promptResponse = client.send(promptRequest, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		int promptStatus = promptResponse.statusCode();

		// Processing:
		if ( promptStatus == 102 ) {

			// Está ocupado.
			responseObserver.onNext(TicketResponse.newBuilder().setStatus(String.valueOf("BUSY")).build());
			responseObserver.onCompleted();
			return;
		}

		// Formato Inválido.
		if (promptStatus == 415) {
			
			responseObserver.onNext(TicketResponse.newBuilder().setStatus(String.valueOf("FORMATO INVALIDO")).build());
			responseObserver.onCompleted();
			return;
		}

		// Todo ha ido bien y el nuevo prompt ha sido aceptado dandonos el nuevo ticket:
		if (promptStatus == 202) {

			String localizacion = promptResponse.headers().firstValue("Location").toString();
			String ticket = localizacion.substring(localizacion.lastIndexOf("/") + 1);

			responseObserver.onNext(TicketResponse.newBuilder().setStatus(String.valueOf("ACEPTADO")).setTicketResponse(ticket).build());
			responseObserver.onCompleted();
			return;
		}

		// En caso de llegar aquí, cerramos el flujo.
		responseObserver.onCompleted();
	}

@Override
	public void consultaTicket(TicketRequest request, StreamObserver<PromptResponse> responseObserver) 
	{
		String id_user = request.getIdUser();
		String ticket = request.getTicketRequest();
		if (ticket != null) {
			ticket = ticket.replace("]", "").replace("[", "").trim();
		}
		// Se comprueban el JWT del usuario, etc.


		//

		HttpClient client = HttpClient.newHttpClient();

		HttpRequest consultaTicketRequest = HttpRequest.newBuilder()
		.uri(URI.create("http://ssdd-llamachat:5020/response/" + ticket)) 
		.GET()
		.build();

		HttpResponse<String> consultaTicketResponse;
		try {
			healthCheck();
			consultaTicketResponse = client.send(consultaTicketRequest, HttpResponse.BodyHandlers.ofString());
		
		
			int statusConsulta = consultaTicketResponse.statusCode();

			// Se ha obtenido respuesta.
			if (statusConsulta == 200) {
				String respuestaLlama = consultaTicketResponse.body();
				responseObserver.onNext(PromptResponse.newBuilder().setStatus("READY").setResponse(respuestaLlama).build());
				responseObserver.onCompleted();
				return;
			}

			// Está ocupado
			if (statusConsulta == 102 ) {

				responseObserver.onNext(PromptResponse.newBuilder().setStatus("BUSY").build());
				responseObserver.onCompleted();
				return;
			}

			if (statusConsulta == 404) {

				responseObserver.onNext(PromptResponse.newBuilder().setStatus("TOKEN INVALIDO").build());
				responseObserver.onCompleted();
				return;
			}
			responseObserver.onCompleted();
		} catch (IOException | InterruptedException e) {
		String errorMsg = e.getMessage();
			System.err.println("Error en gRPC consultaTicket: " + errorMsg);
			
			if (errorMsg != null && errorMsg.contains("no bytes")) {
				System.err.println("Interceptado error de no bytes -> Traduciendo a BUSY para que el front espere");
				responseObserver.onNext(PromptResponse.newBuilder().setStatus("BUSY").build());
			} else {
				responseObserver.onNext(PromptResponse.newBuilder().setStatus("ERROR").build());
			}
		} finally {
			responseObserver.onCompleted();
		}
	}
}