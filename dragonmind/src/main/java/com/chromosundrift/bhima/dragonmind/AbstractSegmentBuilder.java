package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.geometry.Knapp;

import java.util.function.Function;

/**
 * Provides delegation to overloaded methods for building to multiple implementors
 */
abstract class AbstractSegmentBuilder {

    /**
     * Full method with all parameters to which we delegate the overloaded, simplified ones.
     *
     * @param nPanels      number of panels in segment.
     * @param includePoint per-point configuration to enable excluding points.
     * @param pw           panel width.
     * @param ph           panel height.
     * @param k            knapp, ZIG_ZAG means start panel light on the opposite side to the previous panel.
     * @return a {@link DragonBuilder.SegmentBuilding} to use for adding further segments etc.
     */
    public abstract DragonBuilder.SegmentBuilding addSegment(int nPanels, Function<DragonBuilder.PanelPoint, Boolean> includePoint, int pw, int ph, Knapp k);

    public DragonBuilder.SegmentBuilding addSegment(int nPanels, int pw, int ph) {
        return addSegment(nPanels, pw, ph, Knapp.ZIG_ZAG);
    }

    public DragonBuilder.SegmentBuilding addSegment(int nPanels, int pw, int ph, Knapp k) {
        return addSegment(nPanels, p -> true, pw, ph, k);
    }

    public DragonBuilder.SegmentBuilding addSegment(int nPanels, Function<DragonBuilder.PanelPoint, Boolean> exceptions, int pw, int ph) {
        return addSegment(nPanels, exceptions, pw, ph, Knapp.ZIG_ZAG);
    }

}