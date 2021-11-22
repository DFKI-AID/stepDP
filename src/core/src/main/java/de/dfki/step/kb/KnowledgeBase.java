package de.dfki.step.kb;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.kb.semantic.Type;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KnowledgeBase {

    public List<Type> _types = new LinkedList<>();
    public List<IKBObject> _instances = new LinkedList<>();
    public List<IUUID> _objects = new LinkedList<>();

    private Type _root;
    private Board _blackboard;
    private ObjectMapper mapper = new ObjectMapper();

    public KnowledgeBase(Board blackboard)
    {
        this._blackboard = blackboard;
        try {
            this._root = new Type("Object", this, true);
            this.addType(this._root);

            RRTypes.addRRTypesToKB(this);
 
            /*Type token = new Type("Token", this, true);
            token.addInheritance(object);
            this.addType(token);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllKBObjects(List<UUID> exceptions) {
    	this._instances = this._instances.stream().filter(i -> exceptions.contains(i.getUUID())).collect(Collectors.toList());
    	this._objects =  this._objects.stream().filter(i -> exceptions.contains(i.getUUID())).collect(Collectors.toList());
    }
    
    public Type getRootType()
    {
        return this._root;
    }

    public void addUUIDtoList(IUUID object)
    {
        _objects.add(object);
    }

    public List<IUUID> getUUIDMapping()
    {
        return this._objects;
    }

    public Type getType(String name)
    {
        Optional<Type> result = _types.stream().filter(p->p.getName().equalsIgnoreCase(name)).findFirst();

        if(result.isPresent())
            return result.get();
        else
            return null;
    }

    public IKBObjectWriteable createInstance(String name, Type type)
    {
        KBObject newObj = new KBObject(name, type, this);
        this.addUUIDtoList(newObj);
        this._instances.add(newObj);
        return newObj;
    }

    public IKBObjectWriteable createInstance(String name, Type type, Map<String, Object> data) throws Exception
    {
        KBObject newObj = new KBObject(name, type, this, data);
        this.addUUIDtoList(newObj);
        this._instances.add(newObj);
        return newObj;
    }

    public IKBObject getInstance(UUID uuid, boolean checkBoard)
    {
        Optional<IUUID> obj = _objects.stream().filter(p->p.getUUID().equals(uuid)).findFirst();

        if(obj.isPresent() && (obj.get() instanceof IKBObject))
        {
            return (IKBObject) obj.get();
        }
        else if (checkBoard)
        {
            Optional<IToken> tok = this._blackboard.getActiveTokens().stream().filter(p->p.getUUID().equals(uuid)).findFirst();

            // TODO: sollen auch archivierte Tokens berücksichtigt werden?
            if(!tok.isPresent())
                tok = this._blackboard.getArchivedTokens().stream().filter(p->p.getUUID().equals(uuid)).findFirst();

            if(!tok.isPresent())
                return null;
            else
                return tok.get();
        }
        else {
            return null;
        }
    }

    public IKBObject getInstance(UUID uuid)
    {
        return getInstance(uuid, true);
    }
    
    public IKBObject getInstance(String name) {
        // TODO print warning if there are multiple options (when some instances have the same name)
        Optional<IKBObject> obj = _instances.stream().filter(p->p.getName().equalsIgnoreCase(name)).findFirst();

        if(obj.isPresent() && (obj.get() instanceof IKBObject))
        {
            return (IKBObject) obj.get();
        }
        else
            return null;
    }

    public IKBObjectWriteable getInstanceWriteable(UUID uuid)
    {
        Optional<IUUID> obj = _objects.stream().filter(p->p.getUUID().equals(uuid)).findFirst();

        if(obj.isPresent() && (obj.get() instanceof IKBObjectWriteable))
        {
            return (IKBObjectWriteable) obj.get();
        }
        else
            return null;
    }

    public IKBObjectWriteable getInstanceWriteable(String name) {
        Optional<IKBObject> obj = _instances.stream().filter(p->p.getName().equalsIgnoreCase(name)).findFirst();

        if(obj.isPresent() && (obj.get() instanceof IKBObjectWriteable))
        {
            return (IKBObjectWriteable) obj.get();
        }
        else
            return null;
    }

    public List<IKBObject> getInstancesOfType(Type type){
        return _instances.stream()
        		.filter(i -> (i.getType() == null) ? false : i.getType().isInheritanceFrom(type))
        		.collect(Collectors.toList());
    }

    public boolean existType(String name)
    {
        return this._types.stream().anyMatch(p-> p.getName().equalsIgnoreCase(name));
    }

    public boolean existType(Type type)
    {
        return this._types.stream().anyMatch(p-> p.getName().equalsIgnoreCase(type.getName()));
    }

    public void addType(Type type) throws Exception {
        if(existType(type.getName()))
        {
            throw new Exception("Type is already part of the KB!");
        }

        if (!type.isInheritanceFrom(this.getRootType()))
            type.addInheritance(this.getRootType());

        this._objects.add(type);
        this._types.add(type);
    }

    public Board getBlackboard() {
    	return this._blackboard;
    }

    public void importKBObject(String json) throws Exception {
        Map<String, Object> jsonObj = this.mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

        Object typeName = jsonObj.get("type");
        if (typeName == null || !(typeName instanceof String)) {
            throw new Exception("missing type");
        }
        Type type = getType((String) typeName);
        if(type == null)
        {
            throw new Exception("type not found");
        }

        Object name = jsonObj.get("name");

        createInstance((String) name, type, jsonObj);
      }

    public void importObjects(String json) throws Exception {
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> objects = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
      for (Map<String, Object> jsonObj : objects) {
          String name = jsonObj.get("name").toString();
          String type = jsonObj.get("type").toString();
          KBObject kbObj = new KBObject(name, this.getType(type), this, jsonObj);
          this.addUUIDtoList(kbObj);
          this._instances.add(kbObj);
      }
    }
}
