package io.github.mahlylegros.unisettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchro entre le profil partagé et le fichier options.txt d'une instance.
 * <p>
 * Aucune dépendance à Minecraft ni à un loader : la classe ne manipule que des fichiers,
 * ce qui permet de la tester et de la réutiliser telle quelle sur tous les loaders.
 */
public final class ProfileSync {

    public static final String OPTIONS_FILE = "options.txt";
    static final String BACKUP_FILE = "options.txt.unisettings-backup";

    private final Path profileDirectory;

    public ProfileSync(Path profileDirectory) {
        this.profileDirectory = profileDirectory;
    }

    public Path profileOptions() {
        return profileDirectory.resolve(OPTIONS_FILE);
    }

    /**
     * À appeler juste avant que Minecraft lise options.txt.
     * <ul>
     *   <li>Si le profil existe, il remplace le options.txt de l'instance
     *   (l'original est sauvegardé une seule fois à côté, au cas où).</li>
     *   <li>Sinon, le profil est créé à partir des réglages actuels de l'instance.</li>
     * </ul>
     */
    public void applyProfileTo(Path instanceOptions) throws IOException {
        Path profile = profileOptions();
        if (Files.isRegularFile(profile)) {
            if (Files.isRegularFile(instanceOptions)) {
                Path backup = instanceOptions.resolveSibling(BACKUP_FILE);
                if (!Files.exists(backup)) {
                    Files.copy(instanceOptions, backup);
                }
            }
            Files.createDirectories(instanceOptions.toAbsolutePath().getParent());
            writeAtomically(instanceOptions, Files.readAllLines(profile, StandardCharsets.UTF_8));
            Constants.LOG.info("Réglages chargés depuis le profil {}", profile);
        } else if (Files.isRegularFile(instanceOptions)) {
            Files.createDirectories(profileDirectory);
            writeAtomically(profile, Files.readAllLines(instanceOptions, StandardCharsets.UTF_8));
            Constants.LOG.info("Profil créé à partir de cette instance : {}", profile);
        }
    }

    /**
     * À appeler juste après que Minecraft a écrit options.txt.
     * Les valeurs de l'instance remplacent celles du profil, mais les lignes que cette instance
     * ne connaît pas (par exemple les touches d'un mod absent ici) sont conservées.
     */
    public void saveToProfile(Path instanceOptions) throws IOException {
        if (!Files.isRegularFile(instanceOptions)) {
            return;
        }
        List<String> instanceLines = Files.readAllLines(instanceOptions, StandardCharsets.UTF_8);
        Path profile = profileOptions();
        List<String> profileLines = Files.isRegularFile(profile)
                ? Files.readAllLines(profile, StandardCharsets.UTF_8)
                : Collections.<String>emptyList();

        List<String> merged = merge(profileLines, instanceLines);
        if (merged.equals(profileLines)) {
            return;
        }
        Files.createDirectories(profileDirectory);
        writeAtomically(profile, merged);
        Constants.LOG.debug("Profil mis à jour : {}", profile);
    }

    /**
     * Fusionne deux fichiers au format "clé:valeur". L'ordre et les valeurs de {@code newer}
     * gagnent ; les clés présentes seulement dans {@code base} sont ajoutées à la fin.
     */
    static List<String> merge(List<String> base, List<String> newer) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : newer) {
            String key = keyOf(line);
            if (key != null) {
                result.put(key, line);
            }
        }
        for (String line : base) {
            String key = keyOf(line);
            if (key != null) {
                result.putIfAbsent(key, line);
            }
        }
        return new ArrayList<>(result.values());
    }

    private static String keyOf(String line) {
        int colon = line.indexOf(':');
        return colon > 0 ? line.substring(0, colon) : null;
    }

    private static void writeAtomically(Path target, List<String> lines) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicNotSupported) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
