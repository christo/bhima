package com.chromosundrift.bhima.dragonmind;

import org.junit.Assert;
import org.junit.Test;

public class DragonMindTest {
    @Test
    public void testGetResourceUrl() {
        String url = DragonMind.getResourceFileOrUrl("dragon-logo.png");
        Assert.assertNotNull(url);
    }
}
