

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Trigger implements Runnable{

    private static Trigger instance = null;

    // Constructor privado para evitar instanciación directa
    private Trigger() {
        // Constructor privado
    }

    // Método para obtener la instancia del Singleton
    public static Trigger getInstance() {
        if (instance == null) {
            instance = new Trigger();
        }
        return instance;
    }

    public void run() {
    
        Vector<Lugar> oldContent = new Vector<Lugar>();
        Vector<Lugar> newContent = new Vector<Lugar>();
        long segundosDesdeEpoca=0;
        int i = 1;
        char c = 'A';
        for (int j = 0; j < 20; j++) {
            Lugar lugar = new Lugar(Character.toString(c) + i, "Null", false);
            oldContent.add(lugar);
            Lugar lugar2 = new Lugar(Character.toString(c) + i, "Null", false);
            newContent.add(lugar2);
            i++;
            if (i > 5) {
                i = 1;
                c++;
            }
        }
        while (true) {
            JSONObject jsonObject = new JSONObject(performHttpGet("http://localhost:8180"));
            JSONObject lugares = jsonObject.getJSONObject("lugares");
            i = 0;
            // Iterar sobre las claves (A1, A2, ...) y obtener la información de cada lugar
            for (String lugar : lugares.keySet()) {
                JSONObject lugarInfo = lugares.getJSONObject(lugar);
                String patente = lugarInfo.getString("patente");
                boolean ocupado = lugarInfo.getBoolean("ocupado");
                for (Lugar lugarr : newContent) {
                    if (lugarr.getLugar().equalsIgnoreCase(lugar)) {
                        lugarr.setPatente(patente);
                        lugarr.setOcupado(ocupado);
                    }
                }
            }
            for (int k = 0; k < 20; k++) {
                if ((oldContent.get(k).getOcupado() != newContent.get(k).getOcupado())) {
                    JSONObject lugardif = new JSONObject();
                    lugardif.put("lugar", newContent.get(k).getLugar());
                    lugardif.put("patente", newContent.get(k).getPatente());
                    lugardif.put("ocupado", newContent.get(k).getOcupado());
                    if(newContent.get(k).getOcupado()){
                        LocalDateTime tiempoActual = LocalDateTime.now();
                       segundosDesdeEpoca = tiempoActual.until(LocalDateTime.of(1970, 1, 1, 0, 0, 0), ChronoUnit.SECONDS);
                   }else{
                       LocalDateTime tiempoActual2 = LocalDateTime.now();
                       long segundosDesdeEpoca2 = tiempoActual2.until(LocalDateTime.of(1970, 1, 1, 0, 0, 0), ChronoUnit.SECONDS);
                       lugardif.put("Segundos",segundosDesdeEpoca-segundosDesdeEpoca2);
                       //agregar una bandera?

                   }
                    performHttpPost("http://localhost:4567/post", lugardif.toString());
                }
                oldContent.get(k).setOcupado(newContent.get(k).getOcupado());
                oldContent.get(k).setPatente(newContent.get(k).getPatente());
            }
            try {
                // Pausa el hilo actual durante el tiempo especificado
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Maneja la excepción si es necesario
                e.printStackTrace();
            }
        }
    
    }

    private static String performHttpGet(String url) {
        String response = null;
        try {

            // Abrir conexión
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            // Obtener la respuesta

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

    private static String performHttpPost(String url, String postData) {
        String response = null;
        try {
            // Crear cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Crear solicitud POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .build();

            // Enviar la solicitud y obtener la respuesta
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Almacenar la respuesta en el vector "nuevo"
            response = httpResponse.body();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}