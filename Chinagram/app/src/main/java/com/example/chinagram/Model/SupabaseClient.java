// com.example.chinagram.Model.SupabaseClient.java
package com.example.chinagram.Model;

import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.File;
import java.io.IOException;

public class SupabaseClient {
    // Usa la URL y clave real de tu proyecto Supabase
    private static final String SUPABASE_URL = "https://ggrcqwhcntzmjabqnxjc.supabase.co/storage/v1";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdncmNxd2hjbnR6bWphYnFueGpjIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MTAzNDg2NywiZXhwIjoyMDU2NjEwODY3fQ.V7684z2XmVQiTY4CRol9DQ-o97KIavq-hfvAtnx5Qr8";
    private static final OkHttpClient client = new OkHttpClient();

    public static String uploadFile(String bucketName, String filePath, File file) throws IOException {
        Log.d("SupabaseClient", "Subiendo archivo a bucket: " + bucketName + ", path: " + filePath);
        String uploadUrl = SUPABASE_URL + "/object/" + bucketName + "/" + filePath;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), file))
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        try {
            if (response.isSuccessful()) {
                // Generar la URL pública en el formato correcto, asegurándonos de que el path comience con "public/" si es necesario
                String publicUrl = SUPABASE_URL + "/object/public/" + bucketName + "/" + (filePath.startsWith("public/") ? filePath : "public/" + filePath);
                Log.d("SupabaseClient", "Subida exitosa, URL pública: " + publicUrl);
                return publicUrl;
            } else {
                Log.e("SupabaseClient", "Error en la subida: " + response.code() + " - " + response.message());
                // Intentar obtener más detalles del error desde el body si está disponible
                String responseBody = response.body().string();
                Log.e("SupabaseClient", "Detalles del error: " + responseBody);
                throw new IOException("Error uploading file: " + response.code() + " - " + response.message() + " - " + responseBody);
            }
        } finally {
            response.close();
        }
    }

    public static String getPublicUrl(String bucketName, String filePath) {
        // Generar la URL pública asegurándonos de que el path comience con "public/" si es necesario
        return SUPABASE_URL + "/object/public/" + bucketName + "/" + (filePath.startsWith("public/") ? filePath : "public/" + filePath);
    }

    // Nuevo método para eliminar un archivo de Supabase
    public static void deleteFile(String bucketName, String filePath) throws IOException {
        Log.d("SupabaseClient", "Eliminando archivo de bucket: " + bucketName + ", path: " + filePath);
        String deleteUrl = SUPABASE_URL + "/object/" + bucketName + "/" + filePath;

        Request request = new Request.Builder()
                .url(deleteUrl)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        try {
            if (!response.isSuccessful()) {
                throw new IOException("Error al eliminar archivo de Supabase: " + response.code() + " - " + response.message());
            }
            Log.d("SupabaseClient", "Archivo eliminado de Supabase: " + filePath);
        } finally {
            response.close();
        }
    }
}