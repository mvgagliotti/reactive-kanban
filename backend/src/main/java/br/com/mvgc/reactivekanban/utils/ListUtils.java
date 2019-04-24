package br.com.mvgc.reactivekanban.utils;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Provides a method to handle list positions changes.
 */
public class ListUtils {

    /**
     * Changes a element position inside a list from the position denoted by argument "from"
     * to the position denoted by argument "to".
     * <p>
     * Updates all prior or next elements position as well, until the "empty slot" left from
     * the removal of element from position "from"
     *
     * @param <T>  type parameter
     * @param list the list which contains the element to be moved
     * @param from current position
     * @param to   target position
     * @return elements whose position has been changed.
     */
    public static <T> Collection<T> changePosition(List<T> list, int from, int to) {

        if (from == to) {
            return Collections.emptyList();
        }

        Preconditions.checkElementIndex(from, list.size());
        Preconditions.checkElementIndex(to, list.size());

        Collection<T> modified = new HashSet<>();

        int toPosition = to;
        T removed = list.get(from);
        int step = from > to ? 1 : -1;

        while (toPosition != from + step) {
            removed = list.set(toPosition, removed);
            toPosition += step;
            modified.add(removed);
        }

        return modified;

    }

}
