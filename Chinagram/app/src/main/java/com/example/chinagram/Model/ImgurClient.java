// Author: Álvaro Pereira
// Date: 24-02-2025

package com.example.chinagram.Model;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;

public class ImgurClient {

    private static final String TAG = "ImgurClient"; // Etiqueta para logs (desactivar en la ver_final de la app)
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image"; // URL base de la API de Imgur para subir imágenes
    private static final String IMGUR_CLIENT_ID = "20dec99eeb9780b"; // Client ID de Imgur, necesario para autenticar las solicitudes
    private OkHttpClient httpClient; // Cliente HTTP de OkHttp para realizar solicitudes de red

    public ImgurClient() { // Constructor
        httpClient = new OkHttpClient();
    }

    // Método para subir una imagen a Imgur desde un Uri
    public void uploadImage(Uri imageUri, ContentResolver contentResolver, ImgurCallback callback) {
        try {
            InputStream inputStream = contentResolver.openInputStream(imageUri); // Abro un InputStream desde el Uri usando ContentResolver
            if (inputStream == null) {
                Log.e(TAG, "No se pudo abrir el InputStream para el Uri: " + imageUri); // Log de error
                callback.onError("No se pudo abrir la imagen.");
                return;
            }

            // Leo los bytes de la imagen (algo complejo)
            byte[] imageBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                imageBytes = inputStream.readAllBytes();
            }
            inputStream.close();
            Log.d(TAG, "Subiendo imagen desde Uri: " + imageUri); // Log de depuración del proceso

            RequestBody requestBody = new MultipartBody.Builder() // Construyo el cuerpo de la solicitud como un formulario multipart
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "image.jpg", RequestBody.create(imageBytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder() // Creo la solicitud HTTP con la URL, autenticación y cuerpo
                    .url(IMGUR_API_URL)
                    .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() { // Ejecuto la solicitud de forma asíncrona
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Fallo en la subida: " + e.getMessage()); // Log de errores de red o conexión
                    callback.onError("Error de red: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) { // Proceso la respuesta exitosa
                        String responseBody = response.body().string();
                        Log.d(TAG, "Respuesta de Imgur: " + responseBody); // Log de depuración del proceso
                        String imageUrl = extraerUrlDeRespuesta(responseBody);
                        if (imageUrl != null) {
                            Log.d(TAG, "URL obtenida: " + imageUrl); // Log de depuración del proceso
                            callback.onSuccess(imageUrl); // Notifico éxito con la URL
                        } else {
                            Log.e(TAG, "No se pudo extraer la URL de la respuesta"); // Log de depuración del proceso
                            callback.onError("Error al procesar la respuesta de Imgur");
                        }
                    } else {
                        // Maneja respuestas no exitosas (ej. error 403, error 429)
                        Log.e(TAG, "Error en la respuesta: " + response.message() + " - Código: " + response.code());
                        callback.onError("Error al subir la imagen: " + response.message());
                    }
                }
            });
        } catch (IOException e) { // Manejo errores al leer el Uri
            Log.e(TAG, "Error leyendo el Uri: " + e.getMessage());
            callback.onError("Error al leer la imagen: " + e.getMessage());
        }
    }

    private String extraerUrlDeRespuesta(String responseBody) { // Extraigo la URL de la imagen desde la respuesta JSON de Imgur
        try {
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONObject("data").getString("link"); // Obtengo el enlace directo
        } catch (Exception e) {
            Log.e(TAG, "Error parseando JSON: " + e.getMessage());
            return null; // Devuelve null si falla el parseo
        }
    }

    // Interfaz de callback para notificar resultados al Callback
    public interface ImgurCallback {
        void onSuccess(String imageUrl);    // Success: Devuelvo la URL de la imagen
        void onError(String errorMessage);  // Error: Devuelvo un mensaje
    }
}