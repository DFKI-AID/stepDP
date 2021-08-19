package de.dfki.step.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.dfki.step.blackboard.BasicToken;
import de.dfki.step.kb.IKBObject;
import de.dfki.step.kb.KnowledgeBase;

public class TokenRRSerializer extends StdSerializer<BasicToken> {

	public TokenRRSerializer() {
        this(null);
    }
  
    public TokenRRSerializer(Class<BasicToken> t) {
        super(t);
    }

    @Override
    public void serialize(
      BasicToken value, JsonGenerator jgen, SerializerProvider provider) 
      throws IOException, JsonProcessingException {
 
        jgen.writeStartObject();
        jgen.writeNumberField("timestamp", value.getTimestamp());
        jgen.writeStringField("uuid", value.getUUID().toString());
        jgen.writeStringField("type", value.getType().getName());
        jgen.writeBooleanField("active", value.isActive());
        provider.defaultSerializeField("payload", addRRInfoToPayload(value, jgen), jgen);
        jgen.writeEndObject();
    }

    private Map<String, Object> addRRInfoToPayload(BasicToken value, JsonGenerator jgen) throws IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	String payloadJson = mapper.writeValueAsString(value.getPayload());
    	Map<String, Object> payloadCopy = mapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        KnowledgeBase kb = value.getKB();
        Map<String, Object> newPayload = addRRInfoToMap(new HashMap<String,Object>(payloadCopy), kb);
        return newPayload;
    }

    private Map<String, Object> addRRInfoToMap(Map<String, Object> map, KnowledgeBase kb){
    	for (Entry<String, Object> entry : map.entrySet()) {
    		if (entry.getValue() instanceof Map) {
    			Map<String, Object> inner = (Map<String, Object>) entry.getValue();
    			Map<String, Object> innerWithRRInfo = addRRInfoToMap(inner, kb);
    			map.put(entry.getKey(), innerWithRRInfo);
    		} else if (entry.getValue() instanceof UUID) {
    			UUID uuid = (UUID) entry.getValue();
    			String uuidPlus = addRRInfoToUUID(uuid, kb);
    			map.put(entry.getKey(), uuidPlus);
    		} else if (entry.getValue() instanceof String) {
				try{
					UUID uuid = UUID.fromString(entry.getValue().toString());
	    			String uuidPlus = addRRInfoToUUID(uuid, kb);
	    			map.put(entry.getKey(), uuidPlus);
				} catch (IllegalArgumentException exception){
					continue;
				}
    		}
    	}
    	return map;
    }

    private String addRRInfoToUUID(UUID uuid, KnowledgeBase kb) {
    	IKBObject ref;
		ref = kb.getInstanceWriteable(uuid);
		if (ref == null)
			ref = kb.getBlackboard().getTokenByID(uuid, true);
		if (ref != null && ref.getName() != null)
			return uuid.toString() + " (" + ref.getName() + ")";
		else 
			return uuid.toString();
    }
}
