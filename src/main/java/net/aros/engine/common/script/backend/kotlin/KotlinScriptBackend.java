package net.aros.engine.common.script.backend.kotlin;

import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.script.compilation.ScriptLanguage;
import net.aros.engine.common.api.script.compilation.ScriptSource;
import net.aros.engine.common.api.script.compilation.backend.ScriptBackend;
import net.aros.engine.common.api.script.compilation.diagnostic.ScriptDiagnostic;
import net.aros.engine.common.api.script.compilation.exception.ScriptCompilationException;
import net.aros.engine.common.api.script.compilation.exception.ScriptExecutionException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KotlinScriptBackend implements ScriptBackend {
    public static final ScriptLanguage LANGUAGE = new ScriptLanguage("kotlin", "kts");

    private static final String LIBRARY_INDEX = "kotlin-scripting-libs/index.txt";
    private static final String LIBRARY_DIRECTORY = "kotlin-scripting-libs";
    private static final String JVM_TARGET = System.getProperty(
            "aengine.script.kotlin.jvmTarget",
            Integer.toString(Runtime.version().feature())
    );
    private static volatile KotlinScriptBackend shared;

    private final URLClassLoader scriptingClassLoader;
    private final Class<?> function1Class;
    private final Object host;
    private final Object compilationConfiguration;
    private final Object evaluationConfiguration;
    private final Method runInCoroutineContext;
    private final Method getCompiler;
    private final Method getEvaluator;
    private final Method compilerInvoke;
    private final Method evaluatorInvoke;
    private final Method getReports;
    private final Method successValue;
    private final Class<?> failureClass;
    private final Method diagnosticSeverity;
    private final Method diagnosticMessage;
    private final Method diagnosticException;
    private final Method diagnosticRender;
    private final Constructor<?> stringSourceConstructor;

    private KotlinScriptBackend() {
        try {
            ClassLoader applicationClassLoader = KotlinScriptBackend.class.getClassLoader();
            List<Path> libraries = extractLibraries(applicationClassLoader);
            configureKotlinJarProperties(libraries);
            scriptingClassLoader = new URLClassLoader(toUrls(libraries), applicationClassLoader);

            function1Class = load("kotlin.jvm.functions.Function1");
            Class<?> basicHostClass = load("kotlin.script.experimental.host.BasicScriptingHost");
            Class<?> basicJvmHostClass = load("kotlin.script.experimental.jvmhost.BasicJvmScriptingHost");
            Class<?> sourceCodeClass = load("kotlin.script.experimental.api.SourceCode");
            Class<?> stringSourceClass = load("kotlin.script.experimental.host.StringScriptSource");
            Class<?> compiledScriptClass = load("kotlin.script.experimental.api.CompiledScript");
            Class<?> compilationConfigClass = load("kotlin.script.experimental.api.ScriptCompilationConfiguration");
            Class<?> evaluationConfigClass = load("kotlin.script.experimental.api.ScriptEvaluationConfiguration");
            Class<?> continuationClass = load("kotlin.coroutines.Continuation");
            Class<?> compilerClass = load("kotlin.script.experimental.api.ScriptCompiler");
            Class<?> evaluatorClass = load("kotlin.script.experimental.api.ScriptEvaluator");
            Class<?> resultClass = load("kotlin.script.experimental.api.ResultWithDiagnostics");
            Class<?> successClass = load("kotlin.script.experimental.api.ResultWithDiagnostics$Success");
            Class<?> jvmScriptCompilationKtClass = load("kotlin.script.experimental.jvm.JvmScriptCompilationKt");
            Class<?> jvmScriptEvaluationKtClass = load("kotlin.script.experimental.jvm.JvmScriptEvaluationKt");
            Class<?> compilationKeysClass = load("kotlin.script.experimental.api.ScriptCompilationConfigurationKeys");
            Class<?> evaluationKeysClass = load("kotlin.script.experimental.api.ScriptEvaluationConfigurationKeys");
            Class<?> jvmCompilationBuilderClass = load("kotlin.script.experimental.jvm.JvmScriptCompilationConfigurationBuilder");
            Class<?> jvmCompilationKeysClass = load("kotlin.script.experimental.jvm.JvmScriptCompilationConfigurationKeys");
            Class<?> jvmEvaluationKeysClass = load("kotlin.script.experimental.jvm.JvmScriptEvaluationConfigurationKeys");
            Class<?> propertiesBuilderClass = load("kotlin.script.experimental.util.PropertiesCollection$Builder");
            Class<?> propertiesKeyClass = load("kotlin.script.experimental.util.PropertiesCollection$Key");

            host = construct(basicJvmHostClass);
            runInCoroutineContext = basicHostClass.getMethod("runInCoroutineContext", function1Class);
            getCompiler = basicHostClass.getMethod("getCompiler");
            getEvaluator = basicHostClass.getMethod("getEvaluator");
            compilerInvoke = compilerClass.getMethod("invoke", sourceCodeClass, compilationConfigClass, continuationClass);
            evaluatorInvoke = evaluatorClass.getMethod("invoke", compiledScriptClass, evaluationConfigClass, continuationClass);
            getReports = resultClass.getMethod("getReports");
            successValue = successClass.getMethod("getValue");
            failureClass = load("kotlin.script.experimental.api.ResultWithDiagnostics$Failure");
            diagnosticSeverity = load("kotlin.script.experimental.api.ScriptDiagnostic").getMethod("getSeverity");
            diagnosticMessage = load("kotlin.script.experimental.api.ScriptDiagnostic").getMethod("getMessage");
            diagnosticException = load("kotlin.script.experimental.api.ScriptDiagnostic").getMethod("getException");
            diagnosticRender = load("kotlin.script.experimental.api.ScriptDiagnostic").getMethod("render", boolean.class, boolean.class, boolean.class, boolean.class);

            Method configureBuilder = propertiesBuilderClass.getMethod("invoke", propertiesBuilderClass, function1Class);
            Method put = propertiesBuilderClass.getMethod("put", propertiesKeyClass, Object.class);
            Method getCompilationJvm = jvmScriptCompilationKtClass.getMethod("getJvm", compilationKeysClass);
            Method dependenciesFromClassloader = jvmScriptCompilationKtClass.getMethod(
                    "dependenciesFromClassloader",
                    jvmCompilationBuilderClass,
                    String[].class,
                    ClassLoader.class,
                    boolean.class,
                    boolean.class
            );
            Method jvmTargetKey = jvmScriptCompilationKtClass.getMethod("getJvmTarget", jvmCompilationKeysClass);
            Object builder = function1(argument -> {
                Object jvm = getCompilationJvm.invoke(null, argument);
                configureBuilder.invoke(argument, jvm, function1(jvmArgument -> {
                    dependenciesFromClassloader.invoke(null, jvmArgument, new String[0], applicationClassLoader, true, true);
                    put.invoke(jvmArgument, jvmTargetKey.invoke(null, jvmArgument), JVM_TARGET);
                    return unit();
                }));
                return unit();
            });
            compilationConfiguration = compilationConfigClass.getConstructor(function1Class).newInstance(builder);

            Method getEvaluationJvm = jvmScriptEvaluationKtClass.getMethod("getJvm", evaluationKeysClass);
            Method baseClassLoaderKey = jvmScriptEvaluationKtClass.getMethod("getBaseClassLoader", jvmEvaluationKeysClass);
            Object evaluationBuilder = function1(argument -> {
                Object jvm = getEvaluationJvm.invoke(null, argument);
                configureBuilder.invoke(argument, jvm, function1(jvmArgument -> {
                    put.invoke(jvmArgument, baseClassLoaderKey.invoke(null, jvmArgument), applicationClassLoader);
                    return unit();
                }));
                return unit();
            });
            evaluationConfiguration = evaluationConfigClass.getConstructor(function1Class).newInstance(evaluationBuilder);
            stringSourceConstructor = stringSourceClass.getConstructor(String.class, String.class);
        } catch (ReflectiveOperationException | IOException e) {
            throw new IllegalStateException("Failed to initialize Kotlin scripting compiler", unwrap(e));
        }
    }

    public static @NotNull KotlinScriptBackend get() {
        KotlinScriptBackend backend = shared;
        if (backend == null) {
            synchronized (KotlinScriptBackend.class) {
                backend = shared;
                if (backend == null) {
                    backend = new KotlinScriptBackend();
                    shared = backend;
                }
            }
        }
        return backend;
    }

    @Override
    public @NotNull ScriptLanguage language() {
        return LANGUAGE;
    }

    @Override
    public synchronized @NotNull AeScript compile(@NotNull ScriptSource source) {
        Objects.requireNonNull(source, "source");
        if (!LANGUAGE.equals(source.language())) {
            throw new IllegalArgumentException(
                    "Kotlin backend cannot compile %s scripts".formatted(source.language().id())
            );
        }

        try {
            Object scriptSource = stringSourceConstructor.newInstance(source.contents(), source.path().toString());
            Object compiler = getCompiler.invoke(host);
            Object compileResult = runInCoroutineContext.invoke(host, function1(continuation ->
                    compilerInvoke.invoke(compiler, scriptSource, compilationConfiguration, continuation)));
            List<ScriptDiagnostic> diagnostics = diagnosticsOf(compileResult);
            if (failureClass.isInstance(compileResult) || diagnostics.stream().anyMatch(ScriptDiagnostic::isError)) {
                throw new ScriptCompilationException(source, diagnostics);
            }
            Object compiledScript = successValue.invoke(compileResult);
            return new AeScript(language(), () -> evaluate(source, compiledScript));
        } catch (ScriptCompilationException e) {
            throw e;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to compile Kotlin script '%s'".formatted(source.path()), unwrap(e));
        }
    }

    private synchronized void evaluate(ScriptSource source, Object compiledScript) {
        try {
            Object evaluator = getEvaluator.invoke(host);
            Object evalResult = runInCoroutineContext.invoke(host, function1(continuation ->
                    evaluatorInvoke.invoke(evaluator, compiledScript, evaluationConfiguration, continuation)));
            List<ScriptDiagnostic> diagnostics = diagnosticsOf(evalResult);
            if (failureClass.isInstance(evalResult) || diagnostics.stream().anyMatch(ScriptDiagnostic::isError)) {
                throw new ScriptExecutionException(source, diagnostics);
            }
        } catch (ScriptExecutionException e) {
            throw e;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to run Kotlin script '%s'".formatted(source.path()), unwrap(e));
        }
    }

    private List<ScriptDiagnostic> diagnosticsOf(Object result) throws ReflectiveOperationException {
        List<?> reports = (List<?>) getReports.invoke(result);
        List<ScriptDiagnostic> diagnostics = new ArrayList<>(reports.size());
        for (Object report : reports) {
            Object severity = diagnosticSeverity.invoke(report);
            String severityName = severity instanceof Enum<?> severityEnum ? severityEnum.name() : String.valueOf(severity);
            String message = (String) diagnosticMessage.invoke(report);
            Throwable exception = (Throwable) diagnosticException.invoke(report);
            String rendered = (String) diagnosticRender.invoke(report, true, true, true, true);
            diagnostics.add(new ScriptDiagnostic(severityName, message, rendered, exception));
        }
        return diagnostics;
    }

    private Object function1(ReflectiveFunction body) {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("invoke")
                    && method.getParameterCount() == 1) {
                return body.invoke(args[0]);
            }

            return switch (method.getName()) {
                case "toString" -> "KotlinScriptBackend$Function1";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(
                        "Unsupported Function1 method: " + method
                );
            };
        };

        return Proxy.newProxyInstance(
                scriptingClassLoader,
                new Class<?>[]{function1Class},
                handler
        );
    }

    private Object unit() throws ReflectiveOperationException {
        return load("kotlin.Unit").getField("INSTANCE").get(null);
    }

    private Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, true, scriptingClassLoader);
    }

    private static Object construct(Class<?> type) throws ReflectiveOperationException {
        return type.getConstructor().newInstance();
    }

    private static URL[] toUrls(List<Path> libraries) throws IOException {
        URL[] urls = new URL[libraries.size()];
        for (int i = 0; i < libraries.size(); i++) {
            urls[i] = libraries.get(i).toUri().toURL();
        }
        return urls;
    }

    private static List<Path> extractLibraries(ClassLoader classLoader) throws IOException {
        Path directory = Files.createTempDirectory("aengine-kotlin-scripting-");
        directory.toFile().deleteOnExit();

        List<Path> libraries = new ArrayList<>();
        for (String fileName : readLibraryIndex(classLoader)) {
            String resource = LIBRARY_DIRECTORY + '/' + fileName;
            Path target = resolveIndexedLibraryTarget(directory, fileName);
            Files.createDirectories(target.getParent());
            try (InputStream input = classLoader.getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IOException("Missing Kotlin scripting resource: " + resource);
                }
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            target.toFile().deleteOnExit();
            libraries.add(target);
        }
        return libraries;
    }

    private static void configureKotlinJarProperties(List<Path> libraries) throws IOException {
        Path stdlib = findLibrary(libraries, "kotlin-stdlib-");
        setJarProperty("kotlin.java.stdlib.jar", stdlib);
        setJarProperty("kotlin.java.runtime.jar", stdlib);

        Path scriptRuntime = findLibrary(libraries, "kotlin-script-runtime-");
        setJarProperty("kotlin.script.runtime.jar", scriptRuntime);

        findLibraryOrNull(libraries, "kotlin-reflect-")
                .ifPresent(path -> setJarProperty("kotlin.java.reflect.jar", path));
    }

    private static Path findLibrary(List<Path> libraries, String prefix) throws IOException {
        return findLibraryOrNull(libraries, prefix)
                .orElseThrow(() -> new IOException("Missing Kotlin scripting library: " + prefix + "*.jar"));
    }

    private static java.util.Optional<Path> findLibraryOrNull(List<Path> libraries, String prefix) {
        return libraries.stream()
                .filter(path -> path.getFileName().toString().startsWith(prefix))
                .findFirst();
    }

    private static void setJarProperty(String property, Path path) {
        String current = System.getProperty(property);
        if (current == null || !Files.isRegularFile(Path.of(current))) {
            System.setProperty(property, path.toAbsolutePath().toString());
        }
    }

    private static Path resolveIndexedLibraryTarget(Path directory, String indexEntry) throws IOException {
        Path relative = Path.of(indexEntry).normalize();
        if (relative.isAbsolute() || relative.startsWith("..")) {
            throw new IOException("Unsafe Kotlin scripting library index entry: " + indexEntry);
        }
        return directory.resolve(relative).normalize();
    }

    private static List<String> readLibraryIndex(ClassLoader classLoader) throws IOException {
        try (InputStream input = classLoader.getResourceAsStream(LIBRARY_INDEX)) {
            if (input == null) {
                throw new IOException("Missing Kotlin scripting library index: " + LIBRARY_INDEX);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("#"))
                        .toList();
            }
        }
    }

    private static Throwable unwrap(Throwable throwable) {
        while (throwable instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            throwable = invocation.getCause();
        }
        return throwable;
    }

    @FunctionalInterface
    private interface ReflectiveFunction {
        Object invoke(Object argument) throws ReflectiveOperationException;
    }

}
