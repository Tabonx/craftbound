package com.craftbound.placement;

// Whether the server lays out a recipe the client asked for. The book offers every recipe, but the
// player's vanilla recipe book only holds the ones they earned, and other mods read that book. So a
// recipe the player does not know is lent to them for the placement alone. Under doLimitedCrafting
// an unknown recipe could not be crafted anyway, so it is not placed at all.
public enum PlacementAccess
{
    REFUSE,
    PLACE,
    PLACE_WITH_TEMPORARY_GRANT;

    public static PlacementAccess of(boolean limitedCrafting, boolean known)
    {
        if (known)
            return PLACE;
        return limitedCrafting ? REFUSE : PLACE_WITH_TEMPORARY_GRANT;
    }
}
