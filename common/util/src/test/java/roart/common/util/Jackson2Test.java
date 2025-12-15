package roart.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class Jackson2Test {

    @Test
    public void test() {
        TestClass t = new TestClass();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperJsr310 = new ObjectMapper();
        objectMapperJsr310.registerModule(new JavaTimeModule());
        ObjectMapper objectMapperJsr310TimestampNot = jsonObjectMapper();
        convertAndBack(objectMapper, t);
        convertAndBack(objectMapperJsr310, t);
        convertAndBack(objectMapperJsr310TimestampNot, t);
    }
    
    public void convertAndBack(ObjectMapper objectMapper, TestClass t) {
        try {
            String ser = objectMapper.writeValueAsString(t);
            System.out.println("ser: " + ser);
            TestClass t2 = objectMapper.readValue(ser, TestClass.class);
            System.out.println("t2.name: " + t2.name);
            System.out.println("t2.value: " + t2.value);
            System.out.println("t2.dateTime: " + t2.dateTime);
            System.out.println("t2.date: " + t2.date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ObjectMapper jsonObjectMapper() {
                return Jackson2ObjectMapperBuilder.json()
                        .serializationInclusion(JsonInclude.Include.NON_NULL)
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .modules(new JavaTimeModule())
                        .build();
    }

    static class TestClass {
        public String name;
        public int value;
        public Integer i;
        public LocalDateTime dateTime;
        public LocalDate date;
        public TestClass() {
            name = "test";
            value = 42;
            dateTime = LocalDateTime.now();
            date = LocalDate.now();
        }
    }
}
