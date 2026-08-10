package com.routeresq.shared.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.routeresq.shared.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule jtsGeometryModule() {
        SimpleModule module = new SimpleModule("JtsGeometryModule");
        module.addSerializer(Point.class, new PointSerializer());
        module.addDeserializer(Point.class, new PointDeserializer());
        return module;
    }

    public static class PointSerializer extends JsonSerializer<Point> {
        @Override
        public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeStartObject();
                gen.writeNumberField("latitude", value.getY());
                gen.writeNumberField("longitude", value.getX());
                gen.writeEndObject();
            }
        }
    }

    public static class PointDeserializer extends JsonDeserializer<Point> {
        @Override
        public Point deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node != null && node.has("latitude") && node.has("longitude")) {
                double lat = node.get("latitude").asDouble();
                double lon = node.get("longitude").asDouble();
                return GeometryUtils.createPoint(lat, lon);
            }
            return null;
        }
    }
}
