package controller.util;

public class FormatUtils {

    public static String formatWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Dividir el texto en palabras
        String[] words = text.trim().split("\\s+");

        // Crear un StringBuilder para reconstruir el texto
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            // Asegurarse de que la palabra no esté vacía
            if (!word.isEmpty()) {
                // Convertir la primera letra a mayúscula y el resto a minúsculas
                String formattedWord = word.substring(0, 1).toUpperCase() +
                        word.substring(1).toLowerCase();

                // Agregar la palabra formateada al resultado
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(formattedWord);
            }
        }

        return result.toString();
    }
}
