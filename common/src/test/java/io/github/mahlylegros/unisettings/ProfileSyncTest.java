package io.github.mahlylegros.unisettings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileSyncTest {

    @TempDir
    Path temp;

    private ProfileSync sync() {
        return new ProfileSync(temp.resolve("profile"));
    }

    private Path instance(String name) {
        return temp.resolve(name).resolve("options.txt");
    }

    @Test
    void firstLaunchCreatesProfileFromInstance() throws Exception {
        Path a = instance("a");
        Files.createDirectories(a.getParent());
        Files.write(a, List.of("fov:0.5", "lang:fr_fr"));

        sync().applyProfileTo(a);

        assertEquals(List.of("fov:0.5", "lang:fr_fr"), Files.readAllLines(sync().profileOptions()));
    }

    @Test
    void profileIsAppliedToAnotherInstanceWithBackup() throws Exception {
        Path profile = sync().profileOptions();
        Files.createDirectories(profile.getParent());
        Files.write(profile, List.of("fov:0.5", "lang:fr_fr"));

        Path b = instance("b");
        Files.createDirectories(b.getParent());
        Files.write(b, List.of("fov:0.0", "lang:en_us"));

        sync().applyProfileTo(b);

        assertEquals(List.of("fov:0.5", "lang:fr_fr"), Files.readAllLines(b));
        assertEquals(List.of("fov:0.0", "lang:en_us"),
                Files.readAllLines(b.resolveSibling(ProfileSync.BACKUP_FILE)));
    }

    @Test
    void profileIsAppliedToBrandNewInstance() throws Exception {
        Path profile = sync().profileOptions();
        Files.createDirectories(profile.getParent());
        Files.write(profile, List.of("fov:0.5"));

        Path c = instance("c");
        sync().applyProfileTo(c);

        assertEquals(List.of("fov:0.5"), Files.readAllLines(c));
        assertFalse(Files.exists(c.resolveSibling(ProfileSync.BACKUP_FILE)));
    }

    @Test
    void backupIsNotOverwrittenOnLaterLaunches() throws Exception {
        Path profile = sync().profileOptions();
        Files.createDirectories(profile.getParent());
        Files.write(profile, List.of("fov:0.5"));
        Path b = instance("b");
        Files.createDirectories(b.getParent());
        Files.write(b, List.of("fov:0.0"));

        sync().applyProfileTo(b);
        sync().applyProfileTo(b);

        assertEquals(List.of("fov:0.0"), Files.readAllLines(b.resolveSibling(ProfileSync.BACKUP_FILE)));
    }

    @Test
    void saveUpdatesProfileAndKeepsUnknownKeys() throws Exception {
        Path profile = sync().profileOptions();
        Files.createDirectories(profile.getParent());
        Files.write(profile, List.of("fov:0.5", "key_key.othermod.dash:key.keyboard.r"));

        Path a = instance("a");
        Files.createDirectories(a.getParent());
        Files.write(a, List.of("fov:1.0", "lang:fr_fr"));

        sync().saveToProfile(a);

        assertEquals(List.of("fov:1.0", "lang:fr_fr", "key_key.othermod.dash:key.keyboard.r"),
                Files.readAllLines(profile));
    }

    @Test
    void saveWithoutInstanceFileDoesNothing() throws Exception {
        sync().saveToProfile(instance("missing"));
        assertFalse(Files.exists(sync().profileOptions()));
    }

    @Test
    void mergeIgnoresLinesWithoutKey() {
        assertEquals(List.of("a:1"), ProfileSync.merge(List.of("garbage"), List.of("a:1", "")));
        assertTrue(ProfileSync.merge(List.of(), List.of()).isEmpty());
    }
}
