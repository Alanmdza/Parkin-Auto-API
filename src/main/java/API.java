
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.*;

import com.fasterxml.jackson.databind.*;

import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class API implements Runnable{
    public void run() {
        JSONObject jsonObject = new JSONObject(performHttpGet("http://localhost:8180"));
        JSONObject lugares = jsonObject.getJSONObject("lugares");
        enableCORS("*", "*", "*");
        Spark.get("/get", (req, res) -> {
            res.type("application/json"); // Cambia el tipo de contenido a JSON
            return lugares;

        });

        Spark.get("/monitor", (req, res) -> {
            HashMap<String, Object> model = new HashMap<String, Object>();
            model.put("template", "templates/files/index.vtl");
            model.put("title", "Monitorizacion");
            model.put("css", "templates/files/style.vtl");
            model.put("js", "templates/files/script.vtl");
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/layout.vtl"));
        });

        Spark.post("/post", (request, response) -> {
            // Obtiene el cuerpo del POST
            String body = request.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            JSONObject lugar = lugares.getJSONObject(jsonNode.get("lugar").asText());
            lugar.put("patente", jsonNode.get("patente").asText());
            lugar.put("ocupado", jsonNode.get("ocupado"));
            lugares.put(jsonNode.get("lugar").asText(), lugar);
            return "POST recibido con éxito";
        });

    }

    private static String performHttpGet(String url) {
        String response = null;
        try {

            // Abrir conexión
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            // Obtener la respuesta
            int responseCode = connection.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);

            // Leer la respuesta
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(connection.getInputStream());

            // Almacenar la respuesta en el vector "viejo"
            response = jsonNode.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }
}
