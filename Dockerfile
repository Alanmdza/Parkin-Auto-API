# Usa una imagen base con Maven y OpenJDK 11
FROM maven:3.8.4-openjdk-11-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia todos los archivos de tu proyecto al directorio de trabajo en el contenedor
COPY . /app

# Compila y ejecuta tu aplicación
RUN mvn compile

# Especifica cómo iniciar tu aplicación (ajusta esto según tus necesidades)
CMD ["mvn", "exec:java"]
