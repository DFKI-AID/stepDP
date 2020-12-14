package de.dfki.step.kb;

import de.dfki.step.kb.semantic.IProperty;
import de.dfki.step.kb.semantic.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KnowledgeBase {

    public List<Type> _types = new LinkedList<>();
    public List<IUUID> _objects = new LinkedList<>();

    public KnowledgeBase()
    {
        try {
            Type object = new Type("Object", this, true);
            this.addType(object);

            /*Type token = new Type("Token", this, true);
            token.addInheritance(object);
            this.addType(token);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUUIDtoList(IUUID object)
    {
        _objects.add(object);
    }

    public Type getType(String name)
    {
        Optional<Type> result = _types.stream().filter(p->p.getName().equals(name)).findFirst();

        if(result.isPresent())
            return result.get();
        else
            return null;
    }
    
    public IKBObject getInstance(UUID uuid) {
    	return null;
    }
    
    public IKBObject getInstance(String name) {
    	return null;
    }

    public boolean existType(String name)
    {
        return this._types.stream().anyMatch(p-> p.getName().equals(name));
    }

    public boolean existType(Type type)
    {
        return this._types.stream().anyMatch(p-> p.getName().equals(type.getName()));
    }

    public void addType(Type type) throws Exception {
        if(existType(type.getName()))
        {
            throw new Exception("Type is already part of the KB!");
        }

        this._objects.add(type);
        this._types.add(type);
    }

}
