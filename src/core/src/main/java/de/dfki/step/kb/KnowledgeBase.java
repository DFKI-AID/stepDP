package de.dfki.step.kb;

import de.dfki.step.blackboard.Board;
import de.dfki.step.blackboard.IToken;
import de.dfki.step.kb.semantic.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KnowledgeBase {

    public List<Type> _types = new LinkedList<>();
    public List<IKBObject> _instances = new LinkedList<>();
    public List<IUUID> _objects = new LinkedList<>();

    private Type _root;
    private Board _blackboard;

    public KnowledgeBase(Board blackboard)
    {
        this._blackboard = blackboard;
        try {
            this._root = new Type("Object", this, true);
            this.addType(this._root);

            /*Type token = new Type("Token", this, true);
            token.addInheritance(object);
            this.addType(token);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Type getRootType()
    {
        return this._root;
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

    public IKBObjectWriteable createInstance(String name, Type type)
    {
        KBObject newObj = new KBObject(name, type, this);
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

        if (!type.isInheritanceFrom(this.getRootType()))
            type.addInheritance(this.getRootType());

        this._objects.add(type);
        this._types.add(type);
    }

}
