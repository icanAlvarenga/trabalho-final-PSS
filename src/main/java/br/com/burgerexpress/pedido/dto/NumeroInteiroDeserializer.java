package br.com.burgerexpress.pedido.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;

/** Restringe os números dos pedidos sem mudar a configuração JSON dos outros módulos. */
final class NumeroInteiroDeserializer {

    private NumeroInteiroDeserializer() {
    }

    private static void validarToken(JsonParser parser) throws JsonMappingException {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            throw JsonMappingException.from(parser, "Informe um número inteiro em JSON.");
        }
    }

    static final class ParaLong extends JsonDeserializer<Long> {

        @Override
        public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            validarToken(parser);
            return parser.getLongValue();
        }
    }

    static final class ParaInteger extends JsonDeserializer<Integer> {

        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            validarToken(parser);
            return parser.getIntValue();
        }
    }
}
