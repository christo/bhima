package com.chromosundrift.bhima.dragonmind.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SegmentTest {
    @Test
    public void testGetSegmentNumbers() {
        Segment s = new Segment();
        s.addPixelPoint(new PixelPoint(1, 1, 0, 0));
        s.addPixelPoint(new PixelPoint(1, 2, 1, 0));
        s.addPixelPoint(new PixelPoint(2, 1, 2, 0));
        s.addPixelPoint(new PixelPoint(2, 2, 3, 0));

        Set<Integer> stripNumbers = s.getEffectiveStripNumbers();
        HashSet<Integer> expected = new HashSet<>(Arrays.asList(1, 2));
        Assert.assertEquals(expected, stripNumbers);

    }
}
