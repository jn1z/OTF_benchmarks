package OTF_benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import OTF.BAFormat;
import OTF.Model.Threshold;
import OTF.Registry.AntichainForestRegistry;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.exception.FormatException;
import org.openjdk.jmh.runner.RunnerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "java -jar /path/to/benchmark.jar", mixinStandardHelpOptions = true)
public class Benchmark implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    @Option(names = {"-b", "--bisim"}, description = "use bisimulation quotienting", defaultValue = "true")
    private boolean bisimulation;

    @Option(names = {"-t", "--threshold"}, description = "threshold strategy", defaultValue = "ADAPTIVE")
    private Thresholds threshold;

    @Option(names = {"-p", "--threshold-param"}, description = "threshold parameter", defaultValue = "5000")
    private int thresholdParam;

    @Option(names = {"-m", "--method"}, required = true, description = "minimization method")
    private Methods method;

    @Parameters(paramLabel = "FILE", arity = "1", description = "path to the benchmark file")
    private Path file;

    public static void main(String[] args) throws RunnerException, InterruptedException {
        int exitCode = new CommandLine(new Benchmark()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        final IConf<Integer> config = loadConfig(file);
        final Threshold t = this.threshold.getThreshold(thresholdParam);
        final Runnable benchmark = method.build(config, t, bisimulation);

        benchmark.run();
    }

    private IConf<Integer> loadConfig(Path path) {

        String abs = path.toFile().getAbsolutePath();

        if (abs.endsWith(".ba")) {
            try (InputStream is = Files.newInputStream(path)) {
                CompactNFA<Integer> nfa = BAFormat.convertBAFromCompactNFA(is);
                return new InputWalnut.Configuration<>(path.getFileName().toString(), nfa);
            } catch (IOException | FormatException e) {
                throw new IllegalArgumentException(e);
            }
        } else if (abs.endsWith(".mata")) {
            return new InputMata.Configuration(path);
        } else {
            throw new IllegalArgumentException("Cannot parse file format");
        }
    }

    private enum Methods {
        SC {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkSC.SCJob<>(config, bisim, LOGGER);
            }
        },
        SC_S {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkSimSC.SCJob<>(config, bisim, LOGGER);
            }
        },
        BRZ {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkBrz.BrzJob(config, bisim, LOGGER);
            }
        },
        BRZ_S {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkSimBrz.BrzJob(config, bisim, LOGGER);
            }
        },
        OTF {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkOTF.OTFJob(config, threshold, bisim, false, AntichainForestRegistry::new, LOGGER);
            }
        },
        OTF_S {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkOTF.OTFJob(config, threshold, bisim, true, AntichainForestRegistry::new, LOGGER);
            }
        },
        BRZ_OTF {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkOTFBrz.OTFBrzJob(config,
                                                     threshold,
                                                     bisim,
                                                     false,
                                                     AntichainForestRegistry::new,
                                                     LOGGER);
            }
        },
        BRZ_OTF_S {
            @Override
            Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim) {
                return new BenchmarkOTFBrz.OTFBrzJob(config,
                                                     threshold,
                                                     bisim,
                                                     true,
                                                     AntichainForestRegistry::new,
                                                     LOGGER);
            }
        };

        abstract Runnable build(IConf<Integer> config, Threshold threshold, boolean bisim);
    }

    private enum Thresholds {
        MAX_INC {
            @Override
            Threshold getThreshold(int param) {
                return Threshold.maxInc(param);
            }

        },
        MAX_STEP {
            @Override
            Threshold getThreshold(int param) {
                return Threshold.maxSteps(param);
            }
        },
        ADAPTIVE {
            @Override
            Threshold getThreshold(int param) {
                return Threshold.adaptiveSteps(param);
            }
        };

        abstract Threshold getThreshold(int param);
    }
}
