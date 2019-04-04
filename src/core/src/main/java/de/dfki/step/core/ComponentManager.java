package de.dfki.step.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComponentManager {
    void addComponent(Component behavior);

    Optional<Component> getComponent(String id);

    <T extends Component> Optional<T> getComponent(String id, Class<T> clazz);

    /**
     * @deprecated
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Component> List<T> getComponents(Class<T> clazz);

    <T extends Component> Map<String, T> getComponentsMap(Class<T> clazz);

    <T extends Component> Optional<T> getComponent(Class<T> clazz);

    void setPriority(String id, int priority);

    int getPriority(String id);

//    default <T extends Component> void setPriority(int priority, Class<T> clazz) {
//        getComponents(clazz).forEach(c -> setPriority(priority, c));
//    }

    <T extends Component> T retrieveComponent(Class<T> clazz, String errMsg);

    default <T extends Component> T retrieveComponent(Class<T> clazz) {
        return retrieveComponent(clazz, clazz.getSimpleName() + " required.");
    }

}
