package OTF_benchmark;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

public final class Utils {

    public static org.slf4j.Logger getLogger(IBench bench, IInput input) {
        return getLogger(bench, input, "");
    }

    public static org.slf4j.Logger getLogger(IBench bench, IInput input, String suffix) {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%msg%n");
        ple.setContext(lc);
        ple.start();

        final String id = "benchmark_" + input.id() + '_' + bench.id();

        final FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(id + ".csv");
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();

        final Logger logger = (Logger) LoggerFactory.getLogger(id + suffix);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(true);

        return logger;
    }

    public static String extractFilename(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
    }

    public static List<String> findResources(String prefix, Predicate<String> matcher) throws IOException, URISyntaxException {
        final URL resource = Utils.class.getResource(prefix);
        assert resource != null;
        final URI uri = resource.toURI();

        try {
            final Path path = Paths.get(uri);
            return findResources(path, prefix, matcher);
        } catch (FileSystemNotFoundException e) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                final Path path = fs.getPath(prefix);
                return findResources(path, prefix, matcher);
            }
        }
    }

    private static List<String> findResources(Path path, String prefix, Predicate<String> matcher) throws IOException {
        final List<String> result = new ArrayList<>();

        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                final String name = prefix + path.relativize(file);
                if (matcher.test(name)) {
                    result.add(name);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result;
    }
}
