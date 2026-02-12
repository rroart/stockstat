package roart.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Jackson3Test {

    @Test
    public void test() {
        TestClass t = new TestClass();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperNonNull = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
        ObjectMapper objectMapperJsr310 = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        convertAndBack(objectMapper, t);
        convertAndBack(objectMapperNonNull, t);
        // TODO check gone? convertAndBack(objectMapperJsr310, t);        
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
