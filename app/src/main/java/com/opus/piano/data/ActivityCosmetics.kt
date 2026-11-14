package com.opus.piano.data

enum class CosmeticCategory { AVATAR, FRAME, BACKGROUND, HEATMAP, GRAPH }
enum class CosmeticRarity(val weight: Int) { COMMON(60), RARE(25), EPIC(12), LEGENDARY(3) }
data class ActivityCosmetic(
    val id: String,
    val nameEn: String,
    val nameCs: String,
    val category: CosmeticCategory,
    val rarity: CosmeticRarity,
    val requirement: String,
    val color: Long,
)

/** Original, code-drawn Gould collection. IDs are permanent backup/storage identifiers. */
object ActivityCosmetics {
    val all = listOf(
        ActivityCosmetic("avatar_ivory_note", "Ivory Note", "Slonovinová nota", CosmeticCategory.AVATAR, CosmeticRarity.COMMON, "Earned from a weekly minutes goal", 0xFFD7C6A7),
        ActivityCosmetic("avatar_nocturne_bloom", "Nocturne Bloom", "Noční květ", CosmeticCategory.AVATAR, CosmeticRarity.RARE, "Earned from a weekly minutes goal", 0xFF778B86),
        ActivityCosmetic("avatar_golden_treble", "Golden Treble", "Zlatý houslový klíč", CosmeticCategory.AVATAR, CosmeticRarity.EPIC, "Earned from a weekly minutes goal", 0xFFC5A45F),
        ActivityCosmetic("frame_quiet_line", "Quiet Line", "Jemná linka", CosmeticCategory.FRAME, CosmeticRarity.COMMON, "Earned from a weekly minutes goal", 0xFF8B806F),
        ActivityCosmetic("frame_meadow", "Meadow", "Louka", CosmeticCategory.FRAME, CosmeticRarity.RARE, "Earned from a weekly minutes goal", 0xFF66856B),
        ActivityCosmetic("frame_burgundy", "Burgundy", "Vínová", CosmeticCategory.FRAME, CosmeticRarity.EPIC, "Earned from a weekly minutes goal", 0xFF9B5963),
        ActivityCosmetic("frame_celestial", "Celestial", "Nebeská", CosmeticCategory.FRAME, CosmeticRarity.LEGENDARY, "Earned from a weekly minutes goal", 0xFF7C6BA6),
        ActivityCosmetic("background_linen", "Linen", "Len", CosmeticCategory.BACKGROUND, CosmeticRarity.COMMON, "Earned from a weekly minutes goal", 0xFFE7DCCB),
        ActivityCosmetic("background_midnight", "Midnight", "Půlnoc", CosmeticCategory.BACKGROUND, CosmeticRarity.RARE, "Earned from a weekly minutes goal", 0xFF343A42),
        ActivityCosmetic("background_autumn", "Autumn", "Podzim", CosmeticCategory.BACKGROUND, CosmeticRarity.EPIC, "Earned from a weekly minutes goal", 0xFF9B684C),
        ActivityCosmetic("heatmap_evergreen", "Evergreen", "Stálezelená", CosmeticCategory.HEATMAP, CosmeticRarity.COMMON, "Earned from a weekly minutes goal", 0xFF4F8A62),
        ActivityCosmetic("heatmap_rosewood", "Rosewood", "Růžové dřevo", CosmeticCategory.HEATMAP, CosmeticRarity.RARE, "Earned from a weekly minutes goal", 0xFF9A5D65),
        ActivityCosmetic("heatmap_sapphire", "Sapphire", "Safír", CosmeticCategory.HEATMAP, CosmeticRarity.EPIC, "Earned from a weekly minutes goal", 0xFF547EA3),
        ActivityCosmetic("graph_classic", "Classic Lines", "Klasické linky", CosmeticCategory.GRAPH, CosmeticRarity.COMMON, "Earned from a weekly minutes goal", 0xFF7C735F),
        ActivityCosmetic("graph_rose", "Rose Lines", "Růžové linky", CosmeticCategory.GRAPH, CosmeticRarity.RARE, "Earned from a weekly minutes goal", 0xFFA26372),
        ActivityCosmetic("graph_celestial", "Celestial Lines", "Nebeské linky", CosmeticCategory.GRAPH, CosmeticRarity.LEGENDARY, "Earned from a weekly minutes goal", 0xFF7665A3),
    )
    val byId = all.associateBy { it.id }
    init { require(byId.size == all.size) }
}
