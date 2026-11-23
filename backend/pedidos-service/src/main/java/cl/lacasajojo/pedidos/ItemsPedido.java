package cl.lacasajojo.pedidos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

// Guarda la lista de ítems del pedido como JSON en una sola columna
@Converter
public class ItemsConverter implements AttributeConverter<List<ItemPedido>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ItemPedido> items) {
        try {
            return MAPPER.writeValueAsString(items == null ? new ArrayList<ItemPedido>() : items);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar los ítems", e);
        }
    }

    @Override
    public List<ItemPedido> convertToEntityAttribute(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return MAPPER.readValue(json, new TypeReference<List<ItemPedido>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo leer los ítems", e);
        }
    }
}
