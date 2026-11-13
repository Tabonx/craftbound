package com.craftbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

// A dedicated server never loads com.craftbound.client, so nothing outside it may name a class in
// it. Class names sit in the constant pool as plain ASCII, which is enough to find them in the bytes.
class ClientBoundaryTest
{
    private static final String CLIENT = "com/craftbound/client/";
    private static final Pattern CLIENT_CLASS = Pattern.compile(Pattern.quote(CLIENT) + "[\\w/$]+");

    @Test
    void commonCodeNeverNamesAClientClass()
    {
        List<Path> roots = Arrays.stream(System.getProperty("craftbound.mainClasses").split(File.pathSeparator))
                .map(Path::of)
                .filter(Files::isDirectory)
                .toList();
        assertFalse(roots.isEmpty(), "no compiled classes to scan");

        Map<String, Set<String>> offenders = new TreeMap<>();
        for (Path root : roots)
            classes(root).forEach(file ->
            {
                String name = root.relativize(file).toString().replace(File.separatorChar, '/');
                if (name.startsWith(CLIENT))
                    return;
                Set<String> named = clientClassesNamedIn(file);
                if (!named.isEmpty())
                    offenders.put(name, named);
            });

        assertEquals(Map.of(), offenders);
    }

    private static Stream<Path> classes(Path root)
    {
        try
        {
            return Files.walk(root).filter(path -> path.toString().endsWith(".class")).toList().stream();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private static Set<String> clientClassesNamedIn(Path file)
    {
        try
        {
            Matcher matcher = CLIENT_CLASS.matcher(new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1));
            Set<String> named = new TreeSet<>();
            while (matcher.find())
                named.add(matcher.group());
            return named;
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
