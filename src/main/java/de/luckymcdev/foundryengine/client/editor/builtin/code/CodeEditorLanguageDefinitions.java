package de.luckymcdev.foundryengine.client.editor.builtin.code;

import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.extension.texteditor.flag.TextEditorPaletteIndex;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CodeEditorLanguageDefinitions {
    private static final TextEditorLanguageDefinition GLSL = createGlsl();
    private static final TextEditorLanguageDefinition JSON = createJson();
    private static final TextEditorLanguageDefinition TOML = createToml();
    private static final TextEditorLanguageDefinition GROOVY = createGroovy();
    private static final TextEditorLanguageDefinition JAVA = createJava();

    /**
     * Returns the shared GLSL language definition.
     */
    public static TextEditorLanguageDefinition glsl() {
        return GLSL;
    }

    /**
     * Returns the shared JSON language definition.
     */
    public static TextEditorLanguageDefinition json() {
        return JSON;
    }

    /**
     * Returns the shared TOML language definition.
     */
    public static TextEditorLanguageDefinition toml() {
        return TOML;
    }

    /**
     * Returns the shared Groovy language definition.
     */
    public static TextEditorLanguageDefinition groovy() {
        return GROOVY;
    }

    /**
     * Returns the shared Java language definition.
     */
    public static TextEditorLanguageDefinition java() {
        return JAVA;
    }


    private static TextEditorLanguageDefinition createJson() {
        return new LanguageBuilder("JSON")
                .keywords("true", "false", "null")
                .tokenRegexes(Map.ofEntries(
                        Map.entry(TokenPatterns.STRING_DOUBLE, TextEditorPaletteIndex.String),
                        Map.entry(TokenPatterns.FLOAT_NUMBER, TextEditorPaletteIndex.Number),
                        Map.entry("true|false|null", TextEditorPaletteIndex.Keyword),
                        Map.entry("[\\[\\]\\{\\}\\:,]", TextEditorPaletteIndex.Punctuation)
                ))
                .comments("", "", "")
                .build();
    }
    private static TextEditorLanguageDefinition createToml() {
        // LinkedHashMap preserves insertion order — critical for correct priority
        Map<String, Integer> regexes = new LinkedHashMap<>();

        // Section headers MUST come first (most specific line-start anchored patterns)
        regexes.put("^\\s*\\[\\[.*?\\]\\]", TextEditorPaletteIndex.Preprocessor); // [[array-of-tables]]
        regexes.put("^\\s*\\[.*?\\]", TextEditorPaletteIndex.Preprocessor); // [table]

        // Triple-quoted strings (multi-line) before single-quoted
        regexes.put("\"\"\"(\\\\.|[^\"]|\"[^\"]|\"\"[^\"])*\"\"\"", TextEditorPaletteIndex.String);
        regexes.put("'''(\\\\.|[^']|'[^']|''[^'])*'''", TextEditorPaletteIndex.String);

        // Standard strings
        regexes.put(TokenPatterns.STRING_DOUBLE, TextEditorPaletteIndex.String);
        regexes.put(TokenPatterns.STRING_SINGLE, TextEditorPaletteIndex.String);

        // ISO-8601 date/time (before plain numbers so the date separator isn't swallowed)
        regexes.put(
                "\\d{4}-\\d{2}-\\d{2}[T ]?\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2})?",
                TextEditorPaletteIndex.Number);

        // TOML-specific numeric bases
        regexes.put("0x[0-9a-fA-F_]+", TextEditorPaletteIndex.Number); // hex
        regexes.put("0o[0-7_]+", TextEditorPaletteIndex.Number); // octal
        regexes.put("0b[01_]+", TextEditorPaletteIndex.Number); // binary

        // Float / integer (allow TOML underscore separators)
        regexes.put("[+-]?(?:inf|nan)", TextEditorPaletteIndex.Number);
        regexes.put("[+-]?[0-9][0-9_]*(?:\\.[0-9][0-9_]*)?(?:[eE][+-]?[0-9][0-9_]*)?[fF]?",
                TextEditorPaletteIndex.Number);

        // Booleans as keywords (must come before identifier so "true"/"false" don't stay plain)
        regexes.put("\\btrue\\b|\\bfalse\\b", TextEditorPaletteIndex.Keyword);

        // Punctuation — equals sign, inline-table braces, array brackets, comma, dot
        regexes.put("[={}\\[\\],\\.]", TextEditorPaletteIndex.Punctuation);

        // Bare key identifier (last, catch-all)
        regexes.put(TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Identifier);

        return new LanguageBuilder("TOML")
                .keywords("true", "false", "inf", "nan")
                .tokenRegexes(regexes)
                .comments("#", "", "")
                .build();
    }

    private static TextEditorLanguageDefinition createJava() {
        return new LanguageBuilder("Java")
                .keywords(
                        // Primitive types and type-related
                        "boolean", "byte", "char", "double", "float", "int", "long", "short", "void",
                        // Control flow
                        "break", "case", "continue", "default", "do", "else", "for", "goto",
                        "if", "return", "switch", "while",
                        // Exception handling
                        "catch", "finally", "throw", "throws", "try",
                        // OOP structure
                        "abstract", "class", "enum", "extends", "implements", "interface",
                        "new", "super", "this",
                        // Access / storage modifiers
                        "final", "native", "private", "protected", "public",
                        "static", "strictfp", "synchronized", "transient", "volatile",
                        // Package/module system
                        "const", "import", "instanceof", "package",
                        "exports", "module", "open", "opens", "provides", "requires",
                        "to", "uses", "with",
                        // Java 10+ contextual keywords
                        "var",
                        // Java 14+ pattern matching / records / sealed
                        "record", "sealed", "permits", "non-sealed",
                        // Java 14+ switch expression
                        "yield",
                        // Literal values (highlighted as keywords)
                        "true", "false", "null",
                        // assert keyword
                        "assert"
                )
                .tokenRegexes(createJavaRegexes())
                .identifiers(createJavaIdentifiers())
                .comments("//", "/*", "*/")
                .build();
    }

    private static Map<String, Integer> createJavaRegexes() {
        Map<String, Integer> r = new LinkedHashMap<>();
        r.put(TokenPatterns.STRING_DOUBLE, TextEditorPaletteIndex.String);
        r.put(TokenPatterns.STRING_SINGLE, TextEditorPaletteIndex.CharLiteral);
        // Text block (Java 15+): triple-quoted string
        r.put("\"\"\"(\\\\.|[^\"]|\"(?!\"\")|\"\"(?!\"))*\"\"\"", TextEditorPaletteIndex.String);
        r.put(TokenPatterns.HEX_NUMBER, TextEditorPaletteIndex.Number);
        r.put(TokenPatterns.OCTAL_NUMBER, TextEditorPaletteIndex.Number);
        // Binary literals (Java 7+)
        r.put("0[bB][01_]+[lL]?", TextEditorPaletteIndex.Number);
        r.put(TokenPatterns.FLOAT_NUMBER, TextEditorPaletteIndex.Number);
        r.put(TokenPatterns.INTEGER_NUMBER, TextEditorPaletteIndex.Number);
        // Annotations — before the plain identifier pattern
        r.put("@" + TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Preprocessor);
        r.put(TokenPatterns.PUNCTUATION, TextEditorPaletteIndex.Punctuation);
        r.put(TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Identifier);
        return r;
    }

    private static Map<String, String> createJavaIdentifiers() {
        return createMap(
                // java.lang — Object hierarchy
                "Object", "The root of the Java class hierarchy. Every class has Object as a superclass.",
                "Class", "Instances of Class represent classes and interfaces in a running Java application.",
                "Enum", "The common base class for all Java enumeration types.",
                "Record", "The common base class for all Java record types (Java 16+).",
                // java.lang — Strings
                "String", "Represents an immutable sequence of characters.\n" +
                        "Key methods: length(), charAt(int), substring(int, int),\n" +
                        "contains(CharSequence), replace(char, char), split(String),\n" +
                        "trim(), strip(), formatted(Object...), isBlank()",
                "StringBuilder", "A mutable sequence of characters. Faster than String for\n" +
                        "repeated concatenation. Not thread-safe.\n" +
                        "Key methods: append(…), insert(int, …), delete(int, int),\n" +
                        "reverse(), toString()",
                "StringBuffer", "A thread-safe, mutable sequence of characters.\n" +
                        "Slower than StringBuilder; prefer StringBuilder unless\n" +
                        "thread-safety is required.",
                "CharSequence", "Interface implemented by String, StringBuilder, and StringBuffer.",
                // java.lang — Numbers
                "Integer", "Wraps a primitive int value.\n" +
                        "Useful constants: MIN_VALUE, MAX_VALUE.\n" +
                        "Key methods: parseInt(String), valueOf(int), toBinaryString(int),\n" +
                        "toHexString(int), bitCount(int), reverse(int)",
                "Long", "Wraps a primitive long value.\n" +
                        "Key methods: parseLong(String), valueOf(long), toBinaryString(long)",
                "Double", "Wraps a primitive double value.\n" +
                        "Key methods: parseDouble(String), isNaN(double), isInfinite(double)",
                "Float", "Wraps a primitive float value.\n" +
                        "Key methods: parseFloat(String), isNaN(float), isInfinite(float)",
                "Byte", "Wraps a primitive byte value (−128 to 127).",
                "Short", "Wraps a primitive short value (−32768 to 32767).",
                "Number", "Abstract superclass of Integer, Long, Double, Float, Byte, Short, BigInteger, BigDecimal.",
                "Math", "Utility class for mathematical operations.\n" +
                        "Key methods: abs(x), ceil(x), floor(x), round(x), sqrt(x),\n" +
                        "pow(a, b), min(a, b), max(a, b), log(x), exp(x),\n" +
                        "sin(x), cos(x), tan(x), random()\n" +
                        "Constants: Math.PI, Math.E",
                "StrictMath", "Provides the same API as Math but with stricter IEEE-754 compliance.",
                // java.lang — Booleans / Characters
                "Boolean", "Wraps a primitive boolean value.\n" +
                        "Key methods: parseBoolean(String), valueOf(boolean), toString(boolean)",
                "Character", "Wraps a primitive char value.\n" +
                        "Key methods: isLetter(char), isDigit(char), isWhitespace(char),\n" +
                        "toUpperCase(char), toLowerCase(char), isUpperCase(char)",
                // java.lang — System / Runtime
                "System", "Provides access to system resources.\n" +
                        "Key fields: in, out, err\n" +
                        "Key methods: currentTimeMillis(), nanoTime(),\n" +
                        "arraycopy(src, srcPos, dest, destPos, length),\n" +
                        "getenv(String), getProperty(String), exit(int)",
                "Runtime", "Represents the runtime environment of the JVM.\n" +
                        "Key methods: getRuntime(), availableProcessors(),\n" +
                        "totalMemory(), freeMemory(), exec(String)",
                "Thread", "Represents a thread of execution.\n" +
                        "Key methods: start(), run(), sleep(long), join(),\n" +
                        "interrupt(), isAlive(), currentThread(), getName()",
                "Runnable", "Functional interface to be implemented by any class whose\n" +
                        "instances are intended to be executed by a thread.\n" +
                        "Method: void run()",
                "Process", "Represents a native OS process started by Runtime.exec().",
                "ProcessBuilder", "A builder for creating OS processes.\n" +
                        "Key methods: command(…), directory(File), start()",
                // java.lang — Exceptions
                "Exception", "The base class for checked exceptions.",
                "RuntimeException", "The base class for unchecked (runtime) exceptions.",
                "Error", "Represents serious JVM-level errors (e.g., OutOfMemoryError).\n" +
                        "Generally should not be caught.",
                "Throwable", "The root of the Java exception hierarchy.\n" +
                        "Key methods: getMessage(), getCause(), getStackTrace(),\n" +
                        "printStackTrace()",
                "NullPointerException", "Thrown when an application attempts to use null where\nan object is required.",
                "IllegalArgumentException", "Thrown to indicate that a method has been passed an\nillegal or inappropriate argument.",
                "IllegalStateException", "Thrown to indicate that a method has been invoked at an\nillegal or inappropriate time.",
                "IndexOutOfBoundsException", "Thrown to indicate that an index is out of range.",
                "UnsupportedOperationException", "Thrown to indicate that the requested operation is\nnot supported.",
                // java.lang — Functional / Optional
                "Optional", "A container object that may or may not contain a non-null value.\n" +
                        "Key methods: of(T), ofNullable(T), empty(), isPresent(),\n" +
                        "get(), orElse(T), orElseGet(Supplier), map(Function),\n" +
                        "flatMap(Function), filter(Predicate), ifPresent(Consumer)",
                "Iterable", "Implementing this interface allows an object to be used in a\nfor-each loop.\nMethod: Iterator<T> iterator()",
                "Comparable", "Defines natural ordering for a class.\nMethod: int compareTo(T o)",
                "Cloneable", "Marker interface enabling Object.clone() for a class.",
                "AutoCloseable", "Interface for objects that can be closed with try-with-resources.\nMethod: void close() throws Exception",
                // java.util — Collections
                "List", "An ordered collection (also known as a sequence).\n" +
                        "Common implementations: ArrayList, LinkedList, List.of(…)\n" +
                        "Key methods: add(E), get(int), remove(int), size(),\n" +
                        "contains(Object), indexOf(Object), subList(int, int)",
                "ArrayList", "Resizable-array implementation of List.\n" +
                        "O(1) random access; O(n) insertion/removal in the middle.",
                "LinkedList", "Doubly-linked list implementation of List and Deque.\n" +
                        "O(1) insertion/removal at ends; O(n) random access.",
                "Map", "Maps keys to values; no duplicate keys.\n" +
                        "Common implementations: HashMap, LinkedHashMap, TreeMap\n" +
                        "Key methods: put(K, V), get(K), remove(K), containsKey(K),\n" +
                        "keySet(), values(), entrySet(), getOrDefault(K, V)",
                "HashMap", "Hash table implementation of Map.\n" +
                        "O(1) average for put/get/remove. Unordered. Permits null keys.",
                "LinkedHashMap", "Hash table + linked list implementation of Map.\n" +
                        "Maintains insertion order or LRU access order.",
                "TreeMap", "Red-black tree implementation of NavigableMap.\n" +
                        "Keys kept in natural (or Comparator) order. O(log n) operations.",
                "Set", "A collection that contains no duplicate elements.\n" +
                        "Common implementations: HashSet, LinkedHashSet, TreeSet",
                "HashSet", "Hash table implementation of Set. O(1) average add/remove/contains.",
                "TreeSet", "NavigableSet backed by a TreeMap. Sorted in natural order.",
                "Queue", "A collection for holding elements prior to processing.\n" +
                        "Key methods: offer(E), poll(), peek()",
                "Deque", "A double-ended queue supporting element insertion/removal at both ends.",
                "ArrayDeque", "Resizable-array implementation of Deque. Preferred over Stack and LinkedList\nfor stack/queue use cases.",
                "Collections", "Utility class for operating on collections.\n" +
                        "Key methods: sort(List), shuffle(List), reverse(List),\n" +
                        "unmodifiableList(List), singletonList(T), emptyList(),\n" +
                        "frequency(Collection, Object), disjoint(Collection, Collection)",
                "Arrays", "Utility class for operating on arrays.\n" +
                        "Key methods: sort(T[]), binarySearch(T[], T),\n" +
                        "fill(T[], T), copyOf(T[], int), asList(T...),\n" +
                        "stream(T[]), deepToString(Object[])",
                "Iterator", "Interface for iterating over a collection.\n" +
                        "Methods: hasNext(), next(), remove()",
                "Comparator", "Functional interface to define an external ordering.\n" +
                        "Key static factories: comparing(…), naturalOrder(),\n" +
                        "reverseOrder(), thenComparing(…)",
                // java.util — Streams
                "Stream", "A sequence of elements supporting sequential and parallel\n" +
                        "aggregate operations (java.util.stream).\n" +
                        "Key intermediate: filter, map, flatMap, sorted, distinct, limit\n" +
                        "Key terminal: collect, forEach, reduce, count, findFirst, anyMatch",
                "Collectors", "Utility factory for Stream.collect() operations.\n" +
                        "Key methods: toList(), toSet(), toMap(…), groupingBy(…),\n" +
                        "joining(…), counting(), summarizingInt(…)",
                "Optional", "Container that may or may not contain a non-null value.",
                // java.util — Misc
                "Objects", "Utility methods for operating on objects.\n" +
                        "Key methods: requireNonNull(T), requireNonNullElse(T, T),\n" +
                        "isNull(Object), nonNull(Object), equals(Object, Object),\n" +
                        "toString(Object, String), hash(Object...)",
                "UUID", "Represents an immutable universally unique identifier (UUID).\n" +
                        "Key methods: randomUUID(), fromString(String), toString()",
                "Random", "Generates pseudo-random numbers.\n" +
                        "Key methods: nextInt(int), nextLong(), nextDouble(), nextBoolean()",
                "Scanner", "Simple text scanner for parsing primitive types and strings.\n" +
                        "Key methods: nextLine(), nextInt(), nextDouble(), hasNext()",
                "Date", "Represents a specific instant in time (legacy — prefer java.time).",
                // java.time
                "LocalDate", "A date without a time-zone (java.time).\n" +
                        "Key methods: now(), of(int, int, int), plusDays(long),\n" +
                        "getYear(), getMonth(), getDayOfMonth()",
                "LocalTime", "A time without a date or time-zone (java.time).\n" +
                        "Key methods: now(), of(int, int), plusHours(long), getHour()",
                "LocalDateTime", "A date-time without a time-zone (java.time).\n" +
                        "Key methods: now(), of(LocalDate, LocalTime), format(DateTimeFormatter)",
                "Instant", "An instantaneous point on the time-line (java.time).\n" +
                        "Key methods: now(), getEpochSecond(), toEpochMilli()",
                "Duration", "Time-based amount of time (java.time).\n" +
                        "Key methods: ofSeconds(long), ofMillis(long), between(Temporal, Temporal)",
                // java.io / java.nio
                "File", "Represents a file or directory pathname (legacy — prefer Path).\n" +
                        "Key methods: exists(), isDirectory(), listFiles(),\n" +
                        "createNewFile(), delete(), getAbsolutePath()",
                "Path", "Represents a file-system path (java.nio.file).\n" +
                        "Key methods: of(String, String...), resolve(String), toFile(),\n" +
                        "getFileName(), getParent(), toAbsolutePath()",
                "Files", "Utility class for file operations (java.nio.file).\n" +
                        "Key methods: readString(Path), writeString(Path, CharSequence),\n" +
                        "readAllBytes(Path), copy(Path, Path), move(Path, Path),\n" +
                        "createDirectories(Path), delete(Path), lines(Path)",
                "InputStream", "Abstract base class for reading bytes.",
                "OutputStream", "Abstract base class for writing bytes.",
                "Reader", "Abstract base class for reading characters.",
                "Writer", "Abstract base class for writing characters.",
                "BufferedReader", "Reads text from a character-input stream with buffering.\n" +
                        "Key methods: readLine(), lines()",
                "PrintStream", "Prints formatted representations of objects to an output stream.",
                "PrintWriter", "Prints formatted representations of objects to a character stream.",
                // Concurrency
                "Runnable", "Functional interface: void run()",
                "Callable", "Functional interface: V call() throws Exception",
                "Future", "Represents the result of an asynchronous computation.\n" +
                        "Key methods: get(), isDone(), cancel(boolean)",
                "CompletableFuture", "A Future that can be manually completed and composed.\n" +
                        "Key methods: supplyAsync(Supplier), thenApply(Function),\n" +
                        "thenAccept(Consumer), exceptionally(Function), join()",
                // Functional interfaces (java.util.function)
                "Function", "Functional interface: R apply(T t)\n" +
                        "Compose with: andThen(Function), compose(Function)",
                "BiFunction", "Functional interface: R apply(T t, U u)",
                "Predicate", "Functional interface: boolean test(T t)\n" +
                        "Compose with: and(Predicate), or(Predicate), negate()",
                "Consumer", "Functional interface: void accept(T t)",
                "BiConsumer", "Functional interface: void accept(T t, U u)",
                "Supplier", "Functional interface: T get()",
                "UnaryOperator", "Specialisation of Function where T and R are the same type.",
                "BinaryOperator", "Specialisation of BiFunction where all types are the same.",
                // Annotations from java.lang
                "Override", "Indicates that a method declaration intends to override a\nmethod in a supertype.",
                "Deprecated", "Marks a program element as deprecated.\nThe compiler warns when it is used.",
                "SuppressWarnings", "Instructs the compiler to suppress specified warnings.",
                "FunctionalInterface", "Indicates that an interface is intended to be a functional interface.",
                "SafeVarargs", "Suppresses unchecked warnings for heap pollution from parameterized vararg types."
        );
    }

    private static TextEditorLanguageDefinition createGroovy() {
        return new LanguageBuilder("Groovy")
                .keywords(
                        // Java reserved words (inherited)
                        "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                        "char", "class", "const", "continue", "default", "do", "double",
                        "else", "enum", "extends", "final", "finally", "float", "for",
                        "goto", "if", "implements", "import", "instanceof", "int",
                        "interface", "long", "native", "new", "package", "private",
                        "protected", "public", "return", "short", "static", "strictfp",
                        "super", "switch", "synchronized", "this", "throw", "throws",
                        "transient", "try", "void", "volatile", "while",
                        // Groovy-specific keywords
                        "as",          // cast operator: value as Type
                        "def",         // dynamic type declaration
                        "in",          // for-in loop, membership test
                        "it",          // default closure parameter
                        "trait",       // Groovy trait (interface with default implementation)
                        "threadsafe",  // reserved but unused
                        // Groovy common closure/collection methods treated as keywords
                        "with",        // context delegation block
                        "tap",         // like 'with' but returns the receiver
                        // Literal values
                        "true", "false", "null"
                )
                .tokenRegexes(createGroovyRegexes())
                .identifiers(createJavaIdentifiers())
                .comments("//", "/*", "*/")
                .build();
    }

    private static Map<String, Integer> createGroovyRegexes() {
        Map<String, Integer> r = new LinkedHashMap<>();

        // Triple-quoted GString / triple single-quoted string (multi-line) — before simple strings
        r.put("\"\"\"(\\\\.|[^\"]|\"(?!\"\")|\"\"(?!\"))*\"\"\"", TextEditorPaletteIndex.String);
        r.put("'''(\\\\.|[^']|'(?!'')|''(?!'))*'''", TextEditorPaletteIndex.String);

        // Standard strings (GString double, raw single)
        r.put(TokenPatterns.STRING_DOUBLE, TextEditorPaletteIndex.String);
        r.put(TokenPatterns.STRING_SINGLE, TextEditorPaletteIndex.CharLiteral);

        // Slashy string: /pattern/ — must not collide with division; simple heuristic
        r.put("/[^/\\n]+/", TextEditorPaletteIndex.String);

        // Numbers — hex, octal, binary, float (with optional G/g suffix for BigDecimal)
        r.put(TokenPatterns.HEX_NUMBER, TextEditorPaletteIndex.Number);
        r.put(TokenPatterns.OCTAL_NUMBER, TextEditorPaletteIndex.Number);
        r.put("0[bB][01_]+[lLgG]?", TextEditorPaletteIndex.Number);
        r.put("[+-]?[0-9][0-9_]*(?:\\.[0-9][0-9_]*)?(?:[eE][+-]?[0-9][0-9_]*)?[fFdDgG]?",
                TextEditorPaletteIndex.Number);

        // Annotations — before plain identifier
        r.put("@" + TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Preprocessor);

        // Closure arrow operator
        r.put("->", TextEditorPaletteIndex.Punctuation);
        r.put(TokenPatterns.PUNCTUATION, TextEditorPaletteIndex.Punctuation);
        r.put(TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Identifier);
        return r;
    }

    private static TextEditorLanguageDefinition createGlsl() {
        Map<String, Integer> regexes = new LinkedHashMap<>();
        regexes.put("[ \\t]*#[ \\t]*version.+", TextEditorPaletteIndex.Preprocessor);
        regexes.put("[ \\t]*#[ \\t]*[a-zA-Z_]+", TextEditorPaletteIndex.Preprocessor);
        regexes.put(TokenPatterns.STRING_DOUBLE, TextEditorPaletteIndex.String);
        regexes.put(TokenPatterns.STRING_SINGLE, TextEditorPaletteIndex.CharLiteral);
        regexes.put(TokenPatterns.HEX_NUMBER, TextEditorPaletteIndex.Number);
        regexes.put(TokenPatterns.OCTAL_NUMBER, TextEditorPaletteIndex.Number);
        regexes.put(TokenPatterns.FLOAT_NUMBER, TextEditorPaletteIndex.Number);
        regexes.put(TokenPatterns.INTEGER_NUMBER, TextEditorPaletteIndex.Number);
        regexes.put(TokenPatterns.IDENTIFIER, TextEditorPaletteIndex.Identifier);
        regexes.put(TokenPatterns.PUNCTUATION, TextEditorPaletteIndex.Punctuation);

        return new LanguageBuilder("GLSL")
                .keywords(
                        // OpenGL core qualifiers and keywords
                        "const", "uniform", "buffer", "shared", "attribute", "varying",
                        "coherent", "volatile", "restrict", "readonly", "writeonly",
                        "atomic_uint", "layout", "centroid", "flat", "smooth", "noperspective",
                        "patch", "sample", "invariant", "precise", "break", "continue", "do",
                        "for", "while", "switch", "case", "default", "if", "else", "subroutine",
                        "in", "out", "inout", "int", "void", "bool", "true", "false", "float",
                        "double", "discard", "return",
                        // Vector types
                        "vec2", "vec3", "vec4",
                        "ivec2", "ivec3", "ivec4",
                        "bvec2", "bvec3", "bvec4",
                        "uint", "uvec2", "uvec3", "uvec4",
                        "dvec2", "dvec3", "dvec4",
                        // Matrix types
                        "mat2", "mat3", "mat4",
                        "mat2x2", "mat2x3", "mat2x4",
                        "mat3x2", "mat3x3", "mat3x4",
                        "mat4x2", "mat4x3", "mat4x4",
                        "dmat2", "dmat3", "dmat4",
                        "dmat2x2", "dmat2x3", "dmat2x4",
                        "dmat3x2", "dmat3x3", "dmat3x4",
                        "dmat4x2", "dmat4x3", "dmat4x4",
                        // Precision qualifiers
                        "lowp", "mediump", "highp", "precision",
                        // Sampler types — 1D
                        "sampler1D", "sampler1DShadow", "sampler1DArray", "sampler1DArrayShadow",
                        "isampler1D", "isampler1DArray", "usampler1D", "usampler1DArray",
                        // Sampler types — 2D
                        "sampler2D", "sampler2DShadow", "sampler2DArray", "sampler2DArrayShadow",
                        "isampler2D", "isampler2DArray", "usampler2D", "usampler2DArray",
                        "sampler2DRect", "sampler2DRectShadow", "isampler2DRect", "usampler2DRect",
                        "sampler2DMS", "isampler2DMS", "usampler2DMS",
                        "sampler2DMSArray", "isampler2DMSArray", "usampler2DMSArray",
                        // Sampler types — 3D / Cube / Buffer
                        "sampler3D", "isampler3D", "usampler3D",
                        "samplerCube", "samplerCubeShadow", "isamplerCube", "usamplerCube",
                        "samplerCubeArray", "samplerCubeArrayShadow",
                        "isamplerCubeArray", "usamplerCubeArray",
                        "samplerBuffer", "isamplerBuffer", "usamplerBuffer",
                        // Image types — 1D
                        "image1D", "iimage1D", "uimage1D", "image1DArray", "iimage1DArray", "uimage1DArray",
                        // Image types — 2D
                        "image2D", "iimage2D", "uimage2D", "image2DArray", "iimage2DArray", "uimage2DArray",
                        "image2DRect", "iimage2DRect", "uimage2DRect",
                        "image2DMS", "iimage2DMS", "uimage2DMS",
                        "image2DMSArray", "iimage2DMSArray", "uimage2DMSArray",
                        // Image types — 3D / Cube / Buffer
                        "image3D", "iimage3D", "uimage3D",
                        "imageCube", "iimageCube", "uimageCube",
                        "imageCubeArray", "iimageCubeArray", "uimageCubeArray",
                        "imageBuffer", "iimageBuffer", "uimageBuffer",
                        "struct",
                        // Reserved / future keywords
                        "common", "partition", "active", "asm", "class", "union", "enum", "typedef",
                        "template", "this", "resource", "goto", "inline", "noinline", "public",
                        "static", "extern", "external", "interface", "long", "short", "half",
                        "fixed", "unsigned", "superp", "input", "output",
                        "hvec2", "hvec3", "hvec4", "fvec2", "fvec3", "fvec4",
                        "filter", "sizeof", "cast", "namespace", "using", "sampler3DRect"
                )
                .identifiers(createMap(
                        // ---- Trigonometric ----
                        "radians", "Converts degrees to radians: (π / 180) · degrees",
                        "degrees", "Converts radians to degrees: (180 / π) · radians",
                        "sin", "Standard trigonometric sine function",
                        "cos", "Standard trigonometric cosine function",
                        "tan", "Standard trigonometric tangent",
                        "asin", "Arc sine. Returns an angle in [−π/2, π/2] whose sine is x.\nUndefined if |x| > 1.",
                        "acos", "Arc cosine. Returns an angle in [0, π] whose cosine is x.\nUndefined if |x| > 1.",
                        "atan", "Arc tangent. Returns angle in [−π, π] for y/x.\nUndefined if both x and y are 0.",
                        "sinh", "Hyperbolic sine: (eˣ − e⁻ˣ) / 2",
                        "cosh", "Hyperbolic cosine: (eˣ + e⁻ˣ) / 2",
                        "tanh", "Hyperbolic tangent: sinh(x) / cosh(x)",
                        "asinh", "Inverse hyperbolic sine",
                        "acosh", "Inverse hyperbolic cosine. Undefined if x < 1.",
                        "atanh", "Inverse hyperbolic tangent. Undefined if |x| ≥ 1.",
                        // ---- Exponential ----
                        "pow", "Returns xʸ. Undefined if x < 0, or x = 0 and y ≤ 0.",
                        "exp", "Returns eˣ (natural exponentiation)",
                        "log", "Returns the natural logarithm of x. Undefined if x ≤ 0.",
                        "exp2", "Returns 2ˣ",
                        "log2", "Returns log₂(x). Undefined if x ≤ 0.",
                        "sqrt", "Returns √x. Undefined if x < 0.",
                        "inversesqrt", "Returns 1/√x. Undefined if x ≤ 0.",
                        // ---- Common ----
                        "abs", "Returns |x|",
                        "sign", "Returns 1.0 if x > 0, 0.0 if x = 0, −1.0 if x < 0",
                        "floor", "Largest integer ≤ x",
                        "trunc", "Nearest integer to x whose absolute value ≤ |x|",
                        "round", "Nearest integer to x; 0.5 rounds implementation-defined",
                        "roundEven", "Nearest integer to x; 0.5 rounds to nearest even integer",
                        "ceil", "Smallest integer ≥ x",
                        "fract", "Returns x − floor(x)",
                        "mod", "Modulus: x − y·floor(x/y)",
                        "modf", "Returns fractional part and sets i to the integer part",
                        "min", "Returns the smaller of x and y",
                        "max", "Returns the larger of x and y",
                        "clamp", "Returns min(max(x, minVal), maxVal). Undefined if minVal > maxVal.",
                        "mix", "Linear blend: x·(1−a) + y·a",
                        "step", "Returns 0.0 if x < edge, otherwise 1.0",
                        "smoothstep", "Smooth Hermite interpolation between 0 and 1 when edge0 < x < edge1",
                        "isnan", "Returns true if x is NaN",
                        "isinf", "Returns true if x is positive or negative infinity",
                        "floatBitsToInt", "Reinterprets the bit pattern of a float as a signed integer",
                        "floatBitsToUint", "Reinterprets the bit pattern of a float as an unsigned integer",
                        "intBitsToFloat", "Reinterprets an int bit pattern as a float",
                        "uintBitsToFloat", "Reinterprets a uint bit pattern as a float",
                        "fma", "Fused multiply-add: a·b + c (single precise operation)",
                        "frexp", "Splits x into a significand in [0.5, 1.0) and an integer exponent",
                        "ldexp", "Builds a floating-point from significand and exponent",
                        // ---- Packing / Unpacking ----
                        "packUnorm2x16", "Packs two normalised floats into a 32-bit uint (UNorm 16-bit each)",
                        "packSnorm2x16", "Packs two normalised floats into a 32-bit uint (SNorm 16-bit each)",
                        "packUnorm4x8", "Packs four normalised floats into a 32-bit uint (UNorm 8-bit each)",
                        "packSnorm4x8", "Packs four normalised floats into a 32-bit uint (SNorm 8-bit each)",
                        "unpackUnorm2x16", "Unpacks a uint into two UNorm floats",
                        "unpackSnorm2x16", "Unpacks a uint into two SNorm floats",
                        "unpackUnorm4x8", "Unpacks a uint into four UNorm floats",
                        "unpackSnorm4x8", "Unpacks a uint into four SNorm floats",
                        "packDouble2x32", "Packs two uints into a double",
                        "unpackDouble2x32", "Unpacks a double into two uints",
                        "packHalf2x16", "Packs two floats into a uint as 16-bit float halves",
                        "unpackHalf2x16", "Unpacks two 16-bit float halves from a uint",
                        // ---- Geometric ----
                        "length", "Returns the length (Euclidean norm) of the vector",
                        "distance", "Returns the distance between two points",
                        "dot", "Returns the dot product of two vectors",
                        "cross", "Returns the cross product of two vec3 vectors",
                        "normalize", "Returns a vector with the same direction and unit length",
                        "faceforward", "Returns N if dot(Nref, I) < 0, otherwise −N",
                        "reflect", "Returns the reflection direction: I − 2·dot(N,I)·N",
                        "refract", "Returns the refraction vector for the given incidence vector, normal and ratio of indices",
                        // ---- Matrix ----
                        "matrixCompMult", "Component-wise matrix multiplication (NOT linear-algebra multiply)",
                        "outerProduct", "Linear-algebra outer product of two vectors producing a matrix",
                        "transpose", "Returns the transpose of a matrix",
                        "determinant", "Returns the determinant of a square matrix",
                        "inverse", "Returns the inverse of a square matrix",
                        // ---- Vector relational ----
                        "lessThan", "Component-wise x < y, returns bvec",
                        "lessThanEqual", "Component-wise x ≤ y, returns bvec",
                        "greaterThan", "Component-wise x > y, returns bvec",
                        "greaterThanEqual", "Component-wise x ≥ y, returns bvec",
                        "equal", "Component-wise x == y, returns bvec",
                        "notEqual", "Component-wise x != y, returns bvec",
                        "any", "Returns true if any component of the bvec is true",
                        "all", "Returns true if all components of the bvec are true",
                        "not", "Component-wise logical complement",
                        // ---- Integer ----
                        "uaddCarry", "Adds two uints and returns carry",
                        "usubBorrow", "Subtracts two uints and returns borrow",
                        "umulExtended", "Multiplies two uints and returns the full 64-bit result split into msb/lsb",
                        "imulExtended", "Signed version of umulExtended",
                        "bitfieldExtract", "Extracts a range of bits from an integer",
                        "bitfieldInsert", "Inserts bits from one integer into another",
                        "bitfieldReverse", "Reverses all bits in the integer",
                        "bitCount", "Counts the number of set (1) bits",
                        "findLSB", "Returns the position of the least-significant set bit (−1 if none)",
                        "findMSB", "Returns the position of the most-significant set bit (−1 if none)",
                        // ---- Texture ----
                        "textureSize", "Returns the dimensions of a texture level",
                        "texture", "Samples a texture at a given coordinate",
                        "textureProj", "Projective texture lookup",
                        "textureLod", "Texture lookup with explicit LOD",
                        "textureOffset", "Texture lookup with texel offset",
                        "texelFetch", "Fetches a single texel by integer texel coordinate",
                        "texelFetchOffset", "texelFetch with texel offset",
                        "textureProjOffset", "Projective texture lookup with offset",
                        "textureLodOffset", "Texture lookup with explicit LOD and offset",
                        "textureProjLod", "Projective texture lookup with explicit LOD",
                        "textureProjLodOffset", "Projective texture lookup with explicit LOD and offset",
                        "textureGrad", "Texture lookup with explicit gradients",
                        "textureGradOffset", "Texture lookup with explicit gradients and offset",
                        "textureProjGrad", "Projective texture lookup with explicit gradients",
                        "textureProjGradOffset", "Projective texture lookup with explicit gradients and offset",
                        "textureGather", "Gathers the w components of the four texels that would be used for bilinear filtering",
                        "textureGatherOffset", "textureGather with per-texel offset",
                        "textureGatherOffsets", "textureGather with multiple offsets",
                        // ---- Image ----
                        "imageSize", "Returns the dimensions of an image",
                        "imageLoad", "Loads a texel from an image",
                        "imageStore", "Stores a value to an image texel",
                        "imageAtomicAdd", "Atomically adds a value to the texel",
                        "imageAtomicMin", "Atomically stores the minimum of the current and given values",
                        "imageAtomicMax", "Atomically stores the maximum of the current and given values",
                        "imageAtomicAnd", "Atomically ANDs a value with the texel",
                        "imageAtomicOr", "Atomically ORs a value with the texel",
                        "imageAtomicXor", "Atomically XORs a value with the texel",
                        "imageAtomicExchange", "Atomically stores a value and returns the old value",
                        "imageAtomicCompSwap", "Atomically compares-and-swaps the texel value",
                        // ---- Fragment / derivative ----
                        "dFdx", "Partial derivative of p with respect to window x",
                        "dFdy", "Partial derivative of p with respect to window y",
                        "dFdxFine", "Fine partial derivative in x (per-fragment)",
                        "dFdyFine", "Fine partial derivative in y (per-fragment)",
                        "dFdxCoarse", "Coarse partial derivative in x (one or more fragments)",
                        "dFdyCoarse", "Coarse partial derivative in y (one or more fragments)",
                        "fwidth", "Returns abs(dFdx(p)) + abs(dFdy(p))",
                        "fwidthFine", "Returns abs(dFdxFine(p)) + abs(dFdyFine(p))",
                        "fwidthCoarse", "Returns abs(dFdxCoarse(p)) + abs(dFdyCoarse(p))",
                        // ---- Interpolation ----
                        "interpolateAtCentroid", "Evaluates the interpolant at the centroid of the pixel and primitive",
                        "interpolateAtSample", "Evaluates the interpolant at the location of a specific sample",
                        "interpolateAtOffset", "Evaluates the interpolant at an offset from the pixel center",
                        // ---- Noise (deprecated 4.4+) ----
                        "noise1", "1D noise based on x  [deprecated since GLSL 4.4]",
                        "noise2", "2D noise based on x  [deprecated since GLSL 4.4]",
                        "noise3", "3D noise based on x  [deprecated since GLSL 4.4]",
                        "noise4", "4D noise based on x  [deprecated since GLSL 4.4]",
                        // ---- Synchronisation (compute / tessellation) ----
                        "barrier", "Synchronises all invocations in the workgroup/patch before continuing",
                        "memoryBarrier", "Ensures ordering of all memory transactions for the current invocation",
                        "memoryBarrierAtomicCounter", "Ensures ordering of atomic-counter accesses",
                        "memoryBarrierBuffer", "Ensures ordering of buffer-variable accesses",
                        "memoryBarrierShared", "Ensures ordering of shared-variable accesses (compute shaders only)",
                        "memoryBarrierImage", "Ensures ordering of image accesses",
                        "groupMemoryBarrier", "Ensures ordering of all memory transactions as seen by the workgroup (compute shaders only)",
                        // ---- Subpass / vote ----
                        "subpassLoad", "Reads from a subpass input at the implicit fragment coordinate",
                        "anyInvocation", "Returns true if value is true for at least one active invocation in the group",
                        "allInvocations", "Returns true if value is true for all active invocations in the group",
                        "allInvocationsEqual", "Returns true if value is the same for all active invocations in the group"
                ))
                .tokenRegexes(regexes)
                .comments("//", "/*", "*/")
                .build();
    }

    /**
     * Creates a flat {@code Map<String, String>} from alternating key/value varargs.
     *
     * @param keysAndValues Alternating key, value, key, value, … strings.
     * @return A new {@link HashMap} containing all pairs.
     * @throws IllegalArgumentException if the array length is odd.
     */
    private static Map<String, String> createMap(String... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("keysAndValues must have an even length");
        }
        Map<String, String> map = HashMap.newHashMap(keysAndValues.length / 2);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * Reusable regex fragments for common token categories.
     *
     * <p>These are intentionally kept as simple, composable strings rather than
     * compiled {@link java.util.regex.Pattern} instances because the TextEditor
     * library accepts raw regex strings.
     */
    private static final class TokenPatterns {
        /**
         * Hexadecimal integer literal: {@code 0xFF}, {@code 0xDEAD_BEEFuL}.
         */
        static final String HEX_NUMBER = "0[xX][0-9a-fA-F_]+[uU]?[lL]?[lL]?";
        /**
         * Octal integer literal: {@code 0755}.
         */
        static final String OCTAL_NUMBER = "0[0-7]+[Uu]?[lL]?[lL]?";
        /**
         * Decimal or scientific float/integer with optional suffix: {@code 3.14f}, {@code 1e10}.
         */
        static final String FLOAT_NUMBER = "[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][+-]?[0-9]+)?[fF]?";
        /**
         * Plain decimal integer: {@code 42}, {@code 1024L}.
         */
        static final String INTEGER_NUMBER = "[+-]?[0-9]+[Uu]?[lL]?[lL]?";
        /**
         * C/Java-style identifier: starts with letter or underscore.
         */
        static final String IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*";
        /**
         * Common operator and delimiter characters.
         */
        static final String PUNCTUATION = "[\\[\\]\\{\\}\\!\\%\\^\\&\\*\\(\\)\\-\\+\\=\\~\\|\\<\\>\\?\\/\\;\\,\\.]";
        /**
         * Double-quoted string with escape sequences: {@code "hello\nworld"}.
         */
        static final String STRING_DOUBLE = "L?\\\"(\\\\.|[^\\\"])*\\\"";
        /**
         * Single-quoted character literal: {@code 'a'}, {@code '\n'}.
         */
        static final String STRING_SINGLE = "\\'\\\\?[^\\']\\'";
    }

    private static class LanguageBuilder {

        private final TextEditorLanguageDefinition definition;

        /**
         * @param name Display name of the language (shown in status bar and menus).
         */
        LanguageBuilder(String name) {
            this.definition = new TextEditorLanguageDefinition();
            this.definition.setName(name);
            this.definition.setAutoIndentation(true);
        }

        /**
         * Sets the reserved keywords. Matched words receive the
         * {@link TextEditorPaletteIndex#Keyword} colour.
         */
        LanguageBuilder keywords(String... keywords) {
            this.definition.setKeywords(keywords);
            return this;
        }

        /**
         * Sets identifier tooltip documentation.
         * Keys are identifier strings; values are the tooltip text shown on hover.
         */
        LanguageBuilder identifiers(Map<String, String> identifiers) {
            this.definition.setIdentifiers(identifiers);
            return this;
        }

        /**
         * Sets the ordered map of regex → {@link TextEditorPaletteIndex} colour entries.
         * <b>Order matters</b>: earlier entries shadow later ones for the same text.
         */
        LanguageBuilder tokenRegexes(Map<String, Integer> regexes) {
            this.definition.setTokenRegexStrings(regexes);
            return this;
        }

        /**
         * Configures Comment markers.
         */
        LanguageBuilder comments(String lineComment, String blockStart, String blockEnd) {
            this.definition.setSingleLineComment(lineComment);
            this.definition.setCommentStart(blockStart);
            this.definition.setCommentEnd(blockEnd);
            return this;
        }

        /**
         * Finalises and returns the built {@link TextEditorLanguageDefinition}.
         */
        TextEditorLanguageDefinition build() {
            return this.definition;
        }
    }
}