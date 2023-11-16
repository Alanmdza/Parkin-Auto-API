
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.json.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.google.gson.Gson;

import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;
import java.util.Random;
import java.util.Map;


public class Main {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException, JSONException {
        
        JSONObject jsonObject = new JSONObject(performHttpGet("http://localhost:8180"));
        JSONObject lugares = jsonObject.getJSONObject("lugares");
        enableCORS("*", "*", "*");
        Spark.get("/get", (req, res) -> {
            res.type("application/json"); // Cambia el tipo de contenido a JSON
            return lugares;

        });

        // Lanzamos el thread del trigger
        Trigger trigger = Trigger.getInstance();
        Thread thread = new Thread(trigger);
        thread.start();

        // --------------------------------------------------------------------------Routes

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
            model.put("template", "templates/admin/index.vtl");
            model.put("title", "Admin");
            model.put("css", "templates/admin/style.vtl");
            model.put("js", "templates/admin/script.vtl");
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
            System.out.println(body);

            if (jsonNode.has("Segundos")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String horaSalida = dateFormat.format(new Date());
                
                // Obtener la fecha actual
                SimpleDateFormat fechaFormat = new SimpleDateFormat("yyyy-MM-dd");
                String fechaActual = fechaFormat.format(new Date());
            
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")) {
                    // Insertar datos en la tabla
                    String insertQuery = "INSERT INTO ocupaciones (Lugar, Patente, Duracion, HoraSalida, Fecha) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                        preparedStatement.setString(1, jsonNode.get("lugar").asText());
                        String patente = generarPatenteArgentina();
                        preparedStatement.setString(2, patente);
                        preparedStatement.setDouble(3, (jsonNode.get("Segundos").asDouble() / 60));
                        preparedStatement.setString(4, horaSalida);
                        preparedStatement.setString(5, fechaActual);
                        preparedStatement.executeUpdate();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            JSONObject lugar = lugares.getJSONObject(jsonNode.get("lugar").asText());
            lugar.put("patente", jsonNode.get("patente").asText());
            lugar.put("ocupado", jsonNode.get("ocupado"));
            lugares.put(jsonNode.get("lugar").asText(), lugar);
            return "POST recibido con éxito";
        });

        // Definir la ruta de Spark
        get("/buscar/:patente", (request, response) -> {
            String dbUrl = "jdbc:sqlite:db.db";
            String patente = request.params(":patente");
        
            try (Connection connection = DriverManager.getConnection(dbUrl)) {
                // Consultar la base de datos
                String query = "SELECT * FROM ocupaciones WHERE Patente = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, patente);
        
                    ResultSet resultSet = preparedStatement.executeQuery();
        
                    // Construir una lista de resultados
                    List<Map<String, Object>> resultList = new ArrayList<>();
                    while (resultSet.next()) {
                        String lugar = resultSet.getString("Lugar");
                        double duracion = resultSet.getDouble("Duracion");
                        String horaSalida = resultSet.getString("HoraSalida");
                        String fecha = resultSet.getString("Fecha");
        
                        // Modificar la estructura de datos para que coincida con el formato JSON válido
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("fecha", fecha);
                        entry.put("lugar", lugar);
                        entry.put("duracion", duracion);
                        entry.put("horaSalida", horaSalida);
                        resultList.add(entry);
                    }
        
                    // Devolver la lista como JSON
                    Gson gson = new Gson();
                    return gson.toJson(resultList); // asumiendo que tienes un objeto Gson (gson) configurado
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.status(500);
                return "Error interno del servidor";
            }
        });

    }

    // -----------------------------------------------------------------------------------------------Métodos
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

    private static char generarLetraAleatoria() {
        Random random = new Random();
        char letra = (char) (random.nextInt(26) + 'A');
        return letra;
    }

    // Función para generar un número aleatorio de n dígitos
    private static String generarNumeroAleatorio(int n) {
        Random random = new Random();
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < n; i++) {
            numero.append(random.nextInt(10));
        }
        return numero.toString();
    }

    // Función para generar una patente argentina aleatoria
    public static String generarPatenteArgentina() {
        Random random = new Random();

        // Decide si la patente será de formato "LLLNNN" o "LLNNNLL"
        boolean formatoCorto = random.nextBoolean();

        StringBuilder patente = new StringBuilder();

        if (formatoCorto) {
            // Formato "LLLNNN"
            for (int i = 0; i < 3; i++) {
                patente.append(generarLetraAleatoria());
            }
            patente.append(generarNumeroAleatorio(3));
        } else {
            // Formato "LLNNNLL"
            for (int i = 0; i < 2; i++) {
                patente.append(generarLetraAleatoria());
            }
            patente.append(generarNumeroAleatorio(3));
            for (int i = 0; i < 2; i++) {
                patente.append(generarLetraAleatoria());
            }
        }

        return patente.toString();
    }

}
