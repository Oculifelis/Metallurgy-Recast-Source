package it.hurts.metallurgy_reforged.compat.thaumaturge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

/**
 * Wand cap and rod textures, recoloured at load time from the Thaumaturge textures installed beside us. Thaumaturge's
 * art is All Rights Reserved, so none of it ships in Metallurgy: each pixel's luminance, normalised to its source's
 * darkest..brightest, is gradient-mapped onto an 8-tone ramp averaged from the metal's ingot. Position and alpha stay
 * as they are. This is the same maths, in the same order, as the art repository's recolour_caps.py, so the result is
 * pixel-identical to the reviewed textures. No Minecraft types here, so it can be checked outside the game.
 */
public final class CapRecolour {
    public static final List<String> PLAIN = List.of("bronze", "steel", "platinum", "ignatius", "ceruclase", "shadow_steel",
            "sanguinite", "midasium", "etherium", "quicksilver", "kalendrite", "astral_silver", "tartarite", "krik", "amordrine",
            "desichalkos", "prometheum");
    /** Crafted inert and charged by infusion, like thaumium; recoloured from Thaumaturge's thaumium cap. */
    public static final List<String> CHARGED = List.of("mithril", "orichalcum", "adamantine");
    /** Metal to the Thaumaturge rod it is recoloured from. */
    public static final Map<String, String> RODS = Map.of("vulcanite", "blaze", "eximite", "obsidian");
    public static final String THAUM = "assets/thaumaturge/textures/";
    public static final String OURS = "assets/metallurgy/textures/";

    public record Image(int width, int height, int[] argb) {}

    public interface Source {
        /** A decoded image from a jar path, or null when it is missing. */
        Image read(String path);
    }

    private CapRecolour() {}

    static double lum(int c) {
        return 0.299 * (c >> 16 & 255) + 0.587 * (c >> 8 & 255) + 0.114 * (c & 255);
    }

    static double[][] ingotRamp(Image ingot) {
        int n = 8;
        List<Integer> cols = new ArrayList<>();
        for (int c : ingot.argb()) if ((c >>> 24) > 200) cols.add(c);
        cols.sort(Comparator.comparingDouble(CapRecolour::lum)); // stable, like Python's sorted
        double[][] ramp = new double[n][3];
        for (int i = 0; i < n; i++) {
            int from = i * cols.size() / n, to = Math.max(from + 1, (i + 1) * cols.size() / n);
            int[] sum = new int[3];
            for (int j = from; j < to; j++) for (int k = 0; k < 3; k++) sum[k] += cols.get(j) >> (16 - 8 * k) & 255;
            for (int k = 0; k < 3; k++) ramp[i][k] = sum[k] / (double) (to - from);
        }
        return ramp;
    }

    static double[] toneRange(Image img) {
        double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
        for (int c : img.argb()) if ((c >>> 24) != 0) { double l = lum(c); lo = Math.min(lo, l); hi = Math.max(hi, l); }
        return new double[] {lo, hi};
    }

    static Image recolour(Image src, double[][] ramp, double[] tones) {
        double lo = tones[0], hi = tones[1];
        int[] out = src.argb().clone();
        for (int p = 0; p < out.length; p++) {
            int c = out[p];
            if ((c >>> 24) == 0) continue;
            double t = Math.min(1.0, Math.max(0.0, (lum(c) - lo) / Math.max(1e-6, hi - lo))) * (ramp.length - 1);
            int i = Math.min((int) t, ramp.length - 2);
            double f = t - i;
            int rgb = 0;
            for (int k = 0; k < 3; k++) rgb = rgb << 8 | (int) Math.rint(ramp[i][k] + (ramp[i + 1][k] - ramp[i][k]) * f);
            out[p] = c & 0xFF000000 | rgb;
        }
        return new Image(src.width(), src.height(), out);
    }

    /**
     * Every texture path under assets/metallurgy/textures/ we provide, mapped to its PNG; Thaumaturge's thaumium
     * animation metadata is copied for the charged caps. Paths whose Thaumaturge source is missing are left out.
     */
    public static Map<String, byte[]> build(Source thaumaturge, Source metallurgy, byte[] thaumiumMcmeta) throws IOException {
        Map<String, byte[]> out = new LinkedHashMap<>();
        Image capItem = thaumaturge.read(THAUM + "item/wand_cap_iron.png"), capModel = thaumaturge.read(THAUM + "models/wand_cap_iron.png");
        Image charged = thaumaturge.read(THAUM + "item/wand_cap_thaumium.png"), inert = thaumaturge.read(THAUM + "item/wand_cap_thaumium_inert.png"),
                chargedModel = thaumaturge.read(THAUM + "models/wand_cap_thaumium.png");
        for (String metal : PLAIN) {
            Image ingot = metallurgy.read(OURS + "items/" + metal + "_ingot.png");
            if (ingot == null) continue;
            double[][] ramp = ingotRamp(ingot);
            put(out, "item/wand_cap_" + metal + ".png", capItem, ramp, null);
            put(out, "models/wand_cap_" + metal + ".png", capModel, ramp, null);
        }
        for (String metal : CHARGED) {
            Image ingot = metallurgy.read(OURS + "items/" + metal + "_ingot.png");
            if (ingot == null) continue;
            double[][] ramp = ingotRamp(ingot);
            put(out, "item/wand_cap_" + metal + "_inert.png", inert, ramp, null);
            if (put(out, "item/wand_cap_" + metal + ".png", charged, ramp, null) && thaumiumMcmeta != null)
                out.put("item/wand_cap_" + metal + ".png.mcmeta", thaumiumMcmeta);
            put(out, "models/wand_cap_" + metal + ".png", chargedModel, ramp, null);
        }
        for (Map.Entry<String, String> rod : RODS.entrySet()) {
            Image ingot = metallurgy.read(OURS + "items/" + rod.getKey() + "_ingot.png"), wand = thaumaturge.read(THAUM + "item/wand_rod_" + rod.getValue() + ".png");
            if (ingot == null || wand == null) continue;
            double[][] ramp = ingotRamp(ingot);
            // One tone range per rod (its wand icon), so the staff's bright binding doesn't darken the staff.
            double[] tones = toneRange(wand);
            put(out, "item/wand_rod_" + rod.getKey() + ".png", wand, ramp, tones);
            put(out, "item/staff_rod_" + rod.getKey() + ".png", thaumaturge.read(THAUM + "item/staff_rod_" + rod.getValue() + ".png"), ramp, tones);
            put(out, "models/wand_rod_" + rod.getKey() + ".png", thaumaturge.read(THAUM + "models/wand_rod_" + rod.getValue() + ".png"), ramp, null);
        }
        return out;
    }

    private static boolean put(Map<String, byte[]> out, String path, Image src, double[][] ramp, double[] tones) throws IOException {
        if (src == null) return false;
        out.put(path, png(recolour(src, ramp, tones != null ? tones : toneRange(src))));
        return true;
    }

    /** A plain 8-bit RGBA PNG. */
    public static byte[] png(Image img) throws IOException {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (DeflaterOutputStream z = new DeflaterOutputStream(raw)) {
            for (int y = 0; y < img.height(); y++) {
                z.write(0);
                for (int x = 0; x < img.width(); x++) {
                    int c = img.argb()[y * img.width() + x];
                    z.write(c >> 16 & 255); z.write(c >> 8 & 255); z.write(c & 255); z.write(c >>> 24);
                }
            }
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
        chunk(png, "IHDR", new byte[] {(byte) (img.width() >> 24), (byte) (img.width() >> 16), (byte) (img.width() >> 8), (byte) img.width(),
                (byte) (img.height() >> 24), (byte) (img.height() >> 16), (byte) (img.height() >> 8), (byte) img.height(), 8, 6, 0, 0, 0});
        chunk(png, "IDAT", raw.toByteArray());
        chunk(png, "IEND", new byte[0]);
        return png.toByteArray();
    }

    private static void chunk(ByteArrayOutputStream png, String type, byte[] data) {
        int n = data.length;
        png.write(n >>> 24); png.write(n >> 16 & 255); png.write(n >> 8 & 255); png.write(n & 255);
        byte[] t = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        png.write(t, 0, 4);
        png.write(data, 0, n);
        CRC32 crc = new CRC32();
        crc.update(t);
        crc.update(data);
        long v = crc.getValue();
        png.write((int) (v >>> 24)); png.write((int) (v >> 16 & 255)); png.write((int) (v >> 8 & 255)); png.write((int) (v & 255));
    }
}
