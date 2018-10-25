package de.dfki.tocalog.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class SimpleDialogCoordinator implements DialogCoordinator {
    @Override
    public List<DialogFunction> coordinate(List<DialogFunction> dialogFunctions) {
        List<DialogFunction> sortedDf = dialogFunctions.stream()
                .sorted(Comparator.comparingDouble(df -> df.getConfidence().getConfidence()))
                .collect(Collectors.toList());
        if (sortedDf.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<DialogFunction> result = new ArrayList<>();
        result.add(sortedDf.get(sortedDf.size() - 1));
        return result;
    }
}
