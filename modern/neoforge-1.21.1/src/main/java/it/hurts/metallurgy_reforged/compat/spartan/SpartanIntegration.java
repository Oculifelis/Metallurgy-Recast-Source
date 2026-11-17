package it.hurts.metallurgy_reforged.compat.spartan;

import it.hurts.metallurgy_reforged.material.Materials;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.xiyu.spartanweaponryunofficial.api.SpartanWeaponryAPI;
import org.xiyu.spartanweaponryunofficial.api.SpartanWeaponryAPI.WeaponItemType;
import org.xiyu.spartanweaponryunofficial.api.WeaponMaterial;
import org.xiyu.spartanweaponryunofficial.api.WeaponTraits;

/**
 * Spartan Weaponry (unofficial 1.21.1 port, API 15) weapons for every Metallurgy tool metal.
 * Ids match 1.20.1: form_metal. Stats come from each metal's tool block in the material data,
 * traits from data/metallurgy/tags/spartan_weaponry_unofficial/weapon_traits/materials/<metal>.json.
 */
public final class SpartanIntegration {
    public static final String SPARTAN = "spartan_weaponry_unofficial";
    /** Metallurgy form name to Spartan weapon type, same 23 forms as 1.20.1. */
    public static final Map<String, WeaponItemType> FORMS = new LinkedHashMap<>();
    public static final List<DeferredItem<Item>> WEAPONS = new ArrayList<>();
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("metallurgy");
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "metallurgy");

    static {
        String[][] pairs = {
            {"dagger", "dagger"}, {"longsword", "longsword"}, {"katana", "katana"}, {"saber", "saber"},
            {"rapier", "rapier"}, {"greatsword", "greatsword"}, {"hammer", "battle_hammer"}, {"warhammer", "warhammer"},
            {"spear", "spear"}, {"halberd", "halberd"}, {"pike", "pike"}, {"lance", "lance"},
            {"throwing_knife", "throwing_knife"}, {"throwing_axe", "tomahawk"}, {"javelin", "javelin"},
            {"boomerang", "boomerang"}, {"battleaxe", "battleaxe"}, {"mace", "flanged_mace"}, {"glaive", "glaive"},
            {"staff", "quarterstaff"}, {"parrying_dagger", "parrying_dagger"}, {"longbow", "longbow"},
            {"crossbow", "heavy_crossbow"}};
        Map<String, WeaponItemType> byName = new LinkedHashMap<>();
        for (WeaponItemType type : WeaponItemType.values()) byName.put(type.getSerializedName(), type);
        for (String[] pair : pairs) {
            WeaponItemType type = byName.get(pair[1]);
            if (type == null) throw new IllegalStateException("Spartan weapon type missing: " + pair[1]);
            FORMS.put(pair[0], type);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("metallurgy", path);
    }

    private static WeaponMaterial material(Materials.Definition m) {
        return WeaponMaterial.builder(m.name(), "metallurgy")
            .colours(m.color(), m.color())
            .durability((int) m.tool("durability", 1))
            .speed((float) m.tool("efficiency", 0))
            .baseDamage((float) m.tool("damage", 0))
            .enchantability((int) m.tool("enchantability", 0))
            .repairTag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/" + m.name())))
            .traitsTag(TagKey.create(WeaponTraits.REGISTRY_KEY, id("materials/" + m.name())))
            .incorrectBlocksForDrops(TagKey.create(Registries.BLOCK, id("incorrect_for_level_" + (int) m.tool("harvest_level", 2))))
            .build()
            .setUseCustomDisplayName();
    }

    public static void register(IEventBus bus) {
        SpartanWeaponryAPI.assertAPIVersion("metallurgy", 15);
        for (Materials.Definition metal : Materials.ALL) {
            if (!metal.hasTools()) continue;
            WeaponMaterial material = material(metal);
            for (Map.Entry<String, WeaponItemType> form : FORMS.entrySet()) {
                WeaponItemType type = form.getValue();
                WEAPONS.add(ITEMS.register(form.getKey() + "_" + metal.name(), () -> SpartanWeaponryAPI.createWeapon(type, material)));
            }
        }
        ITEMS.register(bus);
        TABS.register("spartanweaponry", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.metallurgy.spartanweaponry"))
            .icon(() -> new ItemStack(WEAPONS.get(0).get()))
            .displayItems((parameters, output) -> WEAPONS.forEach(item -> output.accept(item.get())))
            .build());
        TABS.register(bus);
    }

    private SpartanIntegration() {}
}
