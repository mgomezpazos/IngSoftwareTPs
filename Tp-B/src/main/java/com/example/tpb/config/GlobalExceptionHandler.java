package com.example.tpb.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        String message = ex.getMessage();

        // Mapear mensajes de error a respuestas amigables
        switch (message) {
            case "InvalidUser":
                errorResponse.put("error", "Usuario o contraseña incorrectos");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

            case "InvalidToken":
                errorResponse.put("error", "Token inválido o expirado. Por favor inicia sesión nuevamente");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

            case "InvalidCard":
                errorResponse.put("error", "Tarjeta inválida o ya fue redimida por otro usuario");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

            case "InvalidMerchant":
                errorResponse.put("error", "Comercio no válido");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

            case "CargoImposible":
                errorResponse.put("error", "No se puede realizar el cargo. Verifica que la tarjeta esté redimida y tenga saldo suficiente");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

            case "CardAlreadyRedeemed":
                errorResponse.put("error", "Esta tarjeta ya fue redimida por ti anteriormente");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

            default:
                errorResponse.put("error", message != null ? message : "Error desconocido");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error del servidor: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}