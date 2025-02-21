package net.paulgray.lti;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JsonUtils {

    public static ObjectMapper getMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }
}
