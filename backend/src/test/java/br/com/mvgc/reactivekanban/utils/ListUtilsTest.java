package br.com.mvgc.reactivekanban.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static br.com.mvgc.reactivekanban.utils.ListUtils.changePosition;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

/**
 * Tests for {@link ListUtils}.
 */
public class ListUtilsTest {

    @Test
    public void testChangingClosePositions() {

        List<Integer> listOfIntegers = Arrays.asList(1, 2, 3, 4);
        Collection<Integer> modified = changePosition(listOfIntegers, 1, 0);

        Assert.assertThat(listOfIntegers, contains(2, 1, 3, 4));

        Assert.assertEquals(2, modified.size());
        Assert.assertThat(modified, containsInAnyOrder(1, 2));

    }

    @Test
    public void testChangingAllPositions() {

        List<Integer> listOfIntegers = Arrays.asList(1, 2, 3, 4);
        Collection<Integer> modified = changePosition(listOfIntegers, 0, 3);

        Assert.assertThat(listOfIntegers, contains(2, 3, 4, 1));

        Assert.assertEquals(4, modified.size());
        Assert.assertThat(modified, containsInAnyOrder(1, 2, 3, 4));

    }

    @Test
    public void testChangingFarPositions() {

        List<Integer> listOfIntegers = Arrays.asList(1, 2, 3, 4);
        Collection<Integer> modified = changePosition(listOfIntegers, 1, 3);

        Assert.assertThat(listOfIntegers, contains(1, 3, 4, 2));

        Assert.assertEquals(3, modified.size());
        Assert.assertThat(modified, containsInAnyOrder(2, 3, 4));

    }

    @Test
    public void testChangingBackwardPosition() {

        List<Integer> listOfIntegers = Arrays.asList(1, 2, 3, 4);
        Collection<Integer> modified = changePosition(listOfIntegers, 3, 1);

        Assert.assertThat(listOfIntegers, contains(1, 4, 2, 3));

        Assert.assertEquals(3, modified.size());
        Assert.assertThat(modified, containsInAnyOrder(2, 3, 4));

    }


}
