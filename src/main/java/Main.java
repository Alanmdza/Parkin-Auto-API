
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import spark.Spark;


public class Main {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException, JSONException {
        JSONObject jsonObject = new JSONObject(performHttpGet("http://localhost:8180"));
        JSONObject lugares = jsonObject.getJSONObject("lugares");

        Spark.get("/get", (req, res) -> {
            res.type("application/json"); // Cambia el tipo de contenido a JSON
            return lugares;
            
        });


        Spark.post("/post", (request, response) -> {
            // Obtiene el cuerpo del POST
            String body = request.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);
            System.out.println(jsonNode.get("lugar").asText());
            JSONObject lugar = lugares.getJSONObject(jsonNode.get("lugar").asText());
            lugar.put("patente", jsonNode.get("patente").asText());
            lugar.put("ocupado", jsonNode.get("ocupado").asText());
            lugares.put(jsonNode.get("lugar").asText(), lugar);
            // JsonNode lugarJson = objectMapper.readTree(jsonText);
            // lugares.put("nombre_del_lugar", lugarJson);
            System.out.println(lugar);
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
}
