package de.dfki.step.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.blackboard.Board;

public class LogUtils {
    private static final Logger log = LoggerFactory.getLogger(Board.class);

	public static void printDebugInfo(String description, Object o) {
		try {
			ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
			SimpleModule module = new SimpleModule();
			module.addSerializer(BasicToken.class, new TokenRRSerializer());
			mapper.registerModule(module);
			String json = mapper.writeValueAsString(o);
			log.debug("{}:{} {}", description, System.lineSeparator(), json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
