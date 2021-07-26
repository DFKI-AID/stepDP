package de.dfki.step.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.rr.constraints.ObjectScore;

public class LogUtils {
    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);
	private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	public static void printDebugInfo(String description, Object o) {
		try {
			SimpleModule module = new SimpleModule();
			module.addSerializer(BasicToken.class, new TokenRRSerializer());
			mapper.registerModule(module);
			String json = mapper.writeValueAsString(o);
			log.debug("{}:{} {}", description, System.lineSeparator(), json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public static void logScores(String constraintDescription, List<ObjectScore> scores) {
		Map<String, Float> map = scores
									.stream()
									.map(e -> (Pair<String, Float>) Pair.of(e.getObject().getName(), e.getScore()))
									.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		String json = null;
		try {
			json = mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		if (json != null)
			log.debug("{}:{} {}", constraintDescription, System.lineSeparator(), json);
		else
			log.debug("{}: {}", constraintDescription, "error while parsing json");
	}
}
