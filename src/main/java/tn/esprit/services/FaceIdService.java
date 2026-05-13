package tn.esprit.services;

import com.github.sarxos.webcam.Webcam;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handles Face ID authentication by capturing a webcam image and
 * verifying it against the Python face recognition API.
 */
public class FaceIdService {

    private static final String FACE_API_URL = "http://localhost:5000/verify";
    private static final String BOUNDARY = "----FaceIdBoundary" + System.currentTimeMillis();

    /**
     * Result of a Face ID verification attempt.
     */
    public static class FaceIdResult {
        public final boolean match;
        public final String email;
        public final double confidence;
        public final String error;

        public FaceIdResult(boolean match, String email, double confidence, String error) {
            this.match = match;
            this.email = email;
            this.confidence = confidence;
            this.error = error;
        }
    }

    /**
     * Captures a frame from the default webcam and sends it to the face
     * recognition API for verification.
     *
     * @return FaceIdResult with match status, email (if matched), confidence, and error info
     */
    public FaceIdResult verify() {
        Webcam webcam = null;
        try {
            // 1. Open the default webcam
            webcam = Webcam.getDefault();
            if (webcam == null) {
                return new FaceIdResult(false, null, 0, "Aucune webcam détectée.");
            }

            if (!webcam.isOpen()) {
                webcam.open();
            }

            // Give the webcam a moment to initialize (auto-exposure, auto-focus)
            Thread.sleep(1500);

            // 2. Capture a frame
            BufferedImage image = webcam.getImage();
            if (image == null) {
                return new FaceIdResult(false, null, 0, "Impossible de capturer une image.");
            }

            // 3. Convert to JPEG bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            // 4. Send to the face recognition API
            return sendToApi(imageBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return new FaceIdResult(false, null, 0, "Erreur Face ID: " + e.getMessage());
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }
    }

    /**
     * Sends the captured image to the Flask face recognition API as a
     * multipart/form-data POST request.
     */
    private FaceIdResult sendToApi(byte[] imageBytes) throws Exception {
        URL url = new URL(FACE_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        try (OutputStream os = conn.getOutputStream()) {
            // Write multipart image field
            writeMultipartField(os, "image", "face.jpg", "image/jpeg", imageBytes);

            // Write closing boundary
            os.write(("--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        String responseBody = readStream(status >= 400 ? conn.getErrorStream() : conn.getInputStream());

        if (status != 200) {
            return new FaceIdResult(false, null, 0, "Erreur serveur (HTTP " + status + ")");
        }

        JSONObject json = new JSONObject(responseBody);

        if (json.has("error") && !json.optBoolean("match", false)) {
            return new FaceIdResult(false, null, 0, json.getString("error"));
        }

        boolean matched = json.optBoolean("match", false);
        double confidence = json.optDouble("confidence", 0);
        String email = json.optString("email", null);

        return new FaceIdResult(matched, email, confidence, null);
    }

    private void writeMultipartField(OutputStream os, String fieldName, String fileName,
                                     String contentType, byte[] data) throws IOException {
        String header = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        os.write(data);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
