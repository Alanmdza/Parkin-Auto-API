
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public class Main {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException, JSONException {
       
        JSONObject jsonObject = new JSONObject(performHttpGet("http://localhost:8180"));
        JSONObject lugares = jsonObject.getJSONObject("lugares");
        enableCORS("*", "*", "*");
        Spark.get("/get", (req, res) -> {
            res.type("application/json"); // Cambia el tipo de contenido a JSON
            return lugares;

        });

        //Lanzamos el thread del trigger
        Trigger trigger = Trigger.getInstance();
        Thread thread = new Thread(trigger);
        thread.start();


        //--------------------------------------------------------------------------Routes

        Spark.get("/monitor", (req, res) -> {
            HashMap<String, Object> model = new HashMap<String, Object>();
            model.put("template", "templates/files/index.vtl");
            model.put("title", "Monitorizacion");
            model.put("css", "templates/files/style.vtl");
            model.put("js", "templates/files/script.vtl");
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/layout.vtl"));
        });

          Spark.get("/login", (req, res) -> {
            HashMap<String, Object> model = new HashMap<String, Object>();
            model.put("template", "templates/filestemplatelogin/index.vtl");
            model.put("title", "Login");
            model.put("css", "templates/filestemplatelogin/style.vtl");
            model.put("js", "templates/filestemplatelogin/script.vtl");
            if (req.queryParams("error") != null && req.queryParams("error").equals("1")) {
                model.put("error", "Credenciales incorrectas. Inténtalo de nuevo.");
            }
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/layout.vtl"));
        });

        

//Modificar acá todo lo del admin??
          Spark.get("/admin", (req, res) -> {
            HashMap<String, Object> model = new HashMap<String, Object>();
            model.put("template", "templates/files/index.vtl");
            model.put("title", "Monitorizacion");
            model.put("css", "templates/files/style.vtl");
            model.put("js", "templates/files/script.vtl");
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/layout.vtl"));
        });
        
        Spark.get("/chequeo", (req, res) -> { 
             String nombreArchivo = "src\\main\\java\\usser.txt";
            Map<String, String> usuarios = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Dividir la línea en usuario y contraseña (suponiendo que están separados por coma u otro carácter)
                String[] partes = linea.split(",");
                if (partes.length == 2) {
                    String usuario = partes[0].trim();
                    String contrasena = partes[1].trim();
                    usuarios.put(usuario, contrasena);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (usuarios.containsKey(req.queryParams("user")) && usuarios.get(req.queryParams("user")).equals(req.queryParams("pass"))) {
            res.redirect("/admin");
        } else {
           res.redirect("/login?error=1");
        }
            return null;
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






    //-----------------------------------------------------------------------------------------------Métodos
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
