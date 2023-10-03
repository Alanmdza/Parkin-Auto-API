import spark.Spark;

public class Main {
    public static void main(String[] args) {
        Spark.port(8082);
        Spark.post("/post", (request, response) -> {
            // Obtiene el cuerpo del POST
            String body = request.body();

            // Imprime el contenido en la consola
            System.out.println("Contenido del POST:");
            System.out.println(body);

            // Puedes responder con un mensaje de éxito si lo deseas
            return "POST recibido con éxito";
        });
        Spark.get("/", (request, response) -> {
            return "has hecho get a /";
        });
    }
}
