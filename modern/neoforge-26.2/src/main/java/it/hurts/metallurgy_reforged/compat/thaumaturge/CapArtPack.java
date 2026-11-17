package it.hurts.metallurgy_reforged.compat.thaumaturge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A hidden built-in resource pack holding the wand cap and rod textures {@link CapRecolour} makes from the installed
 * Thaumaturge jar. Nothing is made until the game first asks for one of them; then all 52 files are made once (a few
 * milliseconds) and kept for the session. It sits at the bottom so resource packs can still replace them.
 */
@EventBusSubscriber(modid = "metallurgy", value = Dist.CLIENT)
public final class CapArtPack extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEXTURES = "textures/";
    private static @Nullable Map<String, byte[]> files;

    private CapArtPack(PackLocationInfo location) {
        super(location);
    }

    @SubscribeEvent
    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded("thaumaturge")) return;
        PackLocationInfo location = new PackLocationInfo("metallurgy:thaumaturge_caps", Component.literal("Metallurgy wand caps"),
                PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override public PackResources openPrimary(PackLocationInfo info) { return new CapArtPack(info); }
            @Override public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) { return new CapArtPack(info); }
        };
        Pack.Metadata metadata = new Pack.Metadata(Component.literal("Wand caps recoloured from Thaumaturge's"), PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(), List.of(), true);
        Pack pack = new Pack(location, resources, metadata, new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    private static synchronized Map<String, byte[]> files() {
        if (files == null) {
            long start = System.nanoTime();
            try {
                JarContents thaumaturge = ModList.get().getModFileById("thaumaturge").getFile().getContents();
                JarContents metallurgy = ModList.get().getModFileById("metallurgy").getFile().getContents();
                files = CapRecolour.build(path -> read(thaumaturge, path), path -> read(metallurgy, path),
                        thaumaturge.containsFile(CapRecolour.THAUM + "item/wand_cap_thaumium.png.mcmeta")
                                ? thaumaturge.readFile(CapRecolour.THAUM + "item/wand_cap_thaumium.png.mcmeta") : null);
                LOGGER.info("Made {} Thaumaturge wand cap textures in {} ms", files.size(), (System.nanoTime() - start) / 1_000_000);
                String dump = System.getProperty("metallurgy.capArtDump"); // test runs: write them out for comparison
                if (dump != null) for (Map.Entry<String, byte[]> file : files.entrySet()) {
                    java.nio.file.Path out = java.nio.file.Path.of(dump, file.getKey());
                    java.nio.file.Files.createDirectories(out.getParent());
                    java.nio.file.Files.write(out, file.getValue());
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Could not make the Thaumaturge wand cap textures", e);
                files = Map.of();
            }
        }
        return files;
    }

    private static CapRecolour.@Nullable Image read(JarContents jar, String path) {
        if (!jar.containsFile(path)) return null;
        try (InputStream in = jar.openFile(path); NativeImage image = NativeImage.read(NativeImage.Format.RGBA, in)) {
            int[] argb = new int[image.getWidth() * image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++)
                for (int x = 0; x < image.getWidth(); x++) argb[y * image.getWidth() + x] = image.getPixel(x, y);
            return new CapRecolour.Image(image.getWidth(), image.getHeight(), argb);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static @Nullable String texture(Identifier id) {
        return id.getNamespace().equals("metallurgy") && id.getPath().startsWith(TEXTURES) ? id.getPath().substring(TEXTURES.length()) : null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        String path = type == PackType.CLIENT_RESOURCES ? texture(id) : null;
        byte[] bytes = path != null ? files().get(path) : null;
        return bytes != null ? () -> new ByteArrayInputStream(bytes) : null;
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, PackResources.ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals("metallurgy") || !(directory + "/").startsWith(TEXTURES)) return;
        String prefix = directory.length() > TEXTURES.length() ? directory.substring(TEXTURES.length()) + "/" : "";
        files().forEach((path, bytes) -> {
            if (path.startsWith(prefix)) output.accept(Identifier.fromNamespaceAndPath("metallurgy", TEXTURES + path), () -> new ByteArrayInputStream(bytes));
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of("metallurgy") : Set.of();
    }

    @Override
    public void close() {}
}
