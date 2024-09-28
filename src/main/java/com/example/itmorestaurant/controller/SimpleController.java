package com.example.itmorestaurant.controller;

import com.example.itmorestaurant.repository.RefreshTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class SimpleController {
    private final RefreshTokenRepository refreshTokenRepository;

    public SimpleController(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @GetMapping("/static/{filename}")
    @ResponseBody
    public ResponseEntity<?> serveFile(@PathVariable("filename") String filename) throws IOException {
        // Определение расширения файла
        String extension = filename.substring(filename.lastIndexOf(".") + 1);

        InputStream is = null;
        HttpHeaders headers = new HttpHeaders();

        switch (extension) {
            case "css":
                // Обработка CSS файла
                is = getClass().getClassLoader().getResourceAsStream("static/css/" + filename);
                if (is == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                BufferedReader bf = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                headers.add("Content-Type", "text/css; charset=utf-8");
                return new ResponseEntity<>(sb.toString(), headers, HttpStatus.OK);

            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
                // Обработка изображения
                is = getClass().getClassLoader().getResourceAsStream("static/images/" + filename);
                if (is == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                byte[] imageBytes = is.readAllBytes();
                String contentType = switch (extension) {
                    case "png" -> "image/png";
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "gif" -> "image/gif";
                    default -> throw new IllegalArgumentException("Unsupported image type: " + extension);
                };

                headers.setContentType(MediaType.parseMediaType(contentType));
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

            default:
                return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }
}