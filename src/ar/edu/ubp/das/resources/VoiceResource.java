package ar.edu.ubp.das.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import ar.edu.ubp.das.beans.AudioBean;
import ar.edu.ubp.das.beans.Base64Audio;
import ar.edu.ubp.das.logging.MyLogger;

@Path("voice")
public class VoiceResource {
	private Client client;
	private WebTarget target;
	private static final String KEY = "KEY";
	private static final String URI = "https://speech.googleapis.com/v1p1beta1/speech:recognize?key=" + KEY;
	private MyLogger logger;
	
	public VoiceResource() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}

	@POST
	@Path("/recognize")
	@Produces(MediaType.APPLICATION_JSON)
	public Response recognize(Base64Audio b64) {
		System.out.println("/recognize");
		if (b64.getB64audio() == null || b64.getB64audio().isEmpty()) {
			this.logger.log(MyLogger.ERROR, "Petición de reconocimiento de voz con audio faltante");
			return Response.status(Status.BAD_REQUEST).entity("Audio Faltante").build();
		}
		AudioBean audio = new AudioBean();
		audio.setConfig("LINEAR16", 16000, "es-ES");
		audio.setAudio(b64.getB64audio());
		client = ClientBuilder.newClient();
		target = client.target(URI);
		Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(audio, MediaType.APPLICATION_JSON),
				Response.class);
		try {
			String json = res.readEntity(String.class);
			JsonArray results = JsonParser.parseString(json).getAsJsonObject()
										  .getAsJsonArray("results").get(0)
										  .getAsJsonObject().getAsJsonArray("alternatives");
			this.logger.log(MyLogger.INFO, "Petición de reconocimiento de voz exitoso");
			return Response.status(Status.OK).entity(results).build();
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Petición de reconocimiento de voz con error: " + e.getMessage());
			return Response.status(Status.BAD_REQUEST).entity("No se pudo reconocer el audio").build();
		}
	}
}
