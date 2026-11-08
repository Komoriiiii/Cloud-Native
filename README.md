# Tickets Service

Proyecto de **Desarrollo Cloud Native (CDY2204)** – Semana 3.

Es una API para una startup que vende entradas a eventos. Cuando alguien reserva o compra una entrada, la API genera un **ticket en PDF**, lo guarda y después lo sube a la nube, ordenado por evento y por usuario.

## ¿Qué hace?

- Genera un ticket en PDF para cada reserva o compra.
- Guarda el PDF primero en **EFS** (una carpeta temporal en la nube).
- Lo sube a **S3** con esta forma: `evento123/usuarioX/ticket-CODIGO.pdf`.
- Deja descargar, modificar y eliminar tickets.
- Muestra estadísticas de reservas y ventas por evento.

## ¿Cómo funciona?

```
Postman  -->  API (contenedor Docker en una EC2)  -->  EFS (temporal)  -->  S3 (definitivo)
```

Cada vez que hacemos `git push` a `main`, GitHub Actions construye la imagen Docker, la sube a Docker Hub y la despliega sola en la EC2.

## Endpoints

Todos empiezan con `/api`.

| Qué hace | Método | Ruta |
|---|---|---|
| Generar ticket (y subirlo a S3) | POST | `/tickets` |
| Subir un ticket a S3 | POST | `/tickets/{id}/subir` |
| Ver un ticket | GET | `/tickets/{id}` |
| Listar tickets de un evento | GET | `/tickets/evento/{eventoId}` |
| Descargar un ticket (PDF) | GET | `/tickets/{id}/descargar` |
| Descargar todos los tickets de un evento (ZIP) | GET | `/tickets/evento/{eventoId}/descargar` |
| Descargar los tickets de un usuario (ZIP) | GET | `/tickets/evento/{eventoId}/usuario/{usuarioId}/descargar` |
| Modificar un ticket | PUT | `/tickets/{id}` |
| Eliminar un ticket | DELETE | `/tickets/{id}` |
| Estadísticas de un evento | GET | `/eventos/{eventoId}/estadisticas` |

### Ejemplo para crear un ticket

`POST /api/tickets`

```json
{
  "eventoId": "evento123",
  "usuarioId": "usuarioX",
  "nombreAsistente": "Luis Ramirez",
  "tipoEntrada": "VIP",
  "precio": 60000,
  "estado": "VENDIDO"
}
```

Nota: el `estado` va **en mayúsculas** y solo puede ser `RESERVADO`, `VENDIDO` o `CANCELADO`.

Si quieres crear el ticket sin subirlo a S3 todavía, usa `POST /api/tickets?subirS3=false` y después `POST /api/tickets/{id}/subir`.

## Probarlo en tu computador

Necesitas Java 17 y Maven.

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. Los PDF se guardan en la carpeta `efs-local`. Si no tienes AWS configurado, crea los tickets con `?subirS3=false` para que no intente conectarse a S3.

Estas variables de entorno son opcionales (tienen un valor por defecto):

| Variable | Para qué sirve |
|---|---|
| `EFS_PATH` | Carpeta donde se guardan los PDF temporales |
| `S3_BUCKET` | Nombre del bucket de S3 |
| `AWS_REGION` | Región de AWS (por ejemplo `us-east-1`) |
| `DB_URL` | Dónde se guarda la base de datos |

## Despliegue automático

El archivo `.github/workflows/deploy.yml` hace dos cosas cuando hacemos push a `main`:

1. **build-and-push:** construye la imagen y la sube a Docker Hub (`komoriii/tickets-service`).
2. **deploy:** entra por SSH a la EC2, baja la imagen y reinicia el contenedor con el EFS conectado.

Los datos sensibles van como *secrets* de GitHub (nunca dentro del código):
`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `S3_BUCKET` y `AWS_REGION`.

> No subas archivos `.pem`, tokens ni contraseñas al repositorio.

## Tecnologías

Java 17 · Spring Boot · Docker · GitHub Actions · AWS (EC2, EFS y S3) · Postman

## Integrantes

- Maria Florencia Burgos Sepulveda
- Gustavo Gana Luna
