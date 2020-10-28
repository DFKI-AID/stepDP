package de.dfki.step.kb.semantic;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Type{

    private String _name;
    private List<IProperty> _properties = new LinkedList<>();
    private List<Type> _inherit = new LinkedList<>();

    public Type(String name) throws Exception
    {
        if(name == null)
            throw new Exception("invalid name");
        this._name = name;
    }

    public String getName()
    {
        return this._name;
    }

    public boolean hasProperty(String prop)
    {
        return _properties.stream().anyMatch(p->p.getName().equals(prop));
    }

    public void addProperty(IProperty prop) throws Exception
    {
        if(hasProperty(prop.getName()))
        {
            throw new Exception("Property is already part of the Type!");
        }

        _properties.add(prop);
    }

    public IProperty getProperty(String prop)
    {
        Optional<IProperty> result = _properties.stream().filter(p->p.getName().equals(prop)).findFirst();

        if(result.isPresent())
            return result.get();
        else
            return null;
    }

    public void addInheritance(Type inheritFrom) throws Exception {
        if(isInheritanceFrom(inheritFrom))
        {
            throw new Exception("Type is already inheritance from " + inheritFrom.getName() + "!");
        }

        this._inherit.add(inheritFrom);
    }

    public boolean isInheritanceFrom(Type inheritFrom)
    {
        return this._inherit.contains(inheritFrom);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;

        return _name.equals(type._name);
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}
