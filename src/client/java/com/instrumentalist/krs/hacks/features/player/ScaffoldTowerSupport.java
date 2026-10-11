package com.instrumentalist.krs.hacks.features.player;

/** Source name/metadata support classification, without Minecraft dependencies. */
public final class ScaffoldTowerSupport {
    private static final String REPLACEABLE_BLOCK_NAMES = ",air,water,flowing_water,lava,flowing_lava,fire,tallgrass,deadbush,snow_layer,double_plant,vine,sapling,yellow_flower,red_flower,brown_mushroom,red_mushroom,wheat,carrots,potatoes,nether_wart,reeds,";

    private ScaffoldTowerSupport() {
    }

    /** Source isReplaceable(), lines 5878-5883; snow variant is zero-based. */
    public static boolean isReplaceable(String name, int variant) {
        if (name == null) return true;
        name = name.toLowerCase();
        if (name.equals("snow_layer")) return variant <= 0;
        return REPLACEABLE_BLOCK_NAMES.contains("," + name + ",");
    }

    /**
     * Registry flattening mappings for the source's 1.8 block families. Blocks
     * introduced after 1.8 retain their names and follow the source's name-list
     * fallback; modern collision shapes/replaceability tags are not substituted.
     */
    public static String legacyName(String name) {
        if (name == null) return null;
        return switch (name) {
            case "cave_air", "void_air" -> "air";
            case "short_grass", "fern" -> "tallgrass";
            case "dead_bush" -> "deadbush";
            case "snow" -> "snow_layer";
            case "sunflower", "lilac", "tall_grass", "large_fern", "rose_bush", "peony" -> "double_plant";
            case "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling",
                    "acacia_sapling", "dark_oak_sapling" -> "sapling";
            case "dandelion" -> "yellow_flower";
            case "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip",
                    "white_tulip", "pink_tulip", "oxeye_daisy" -> "red_flower";
            case "sugar_cane" -> "reeds";
            default -> name;
        };
    }
}
