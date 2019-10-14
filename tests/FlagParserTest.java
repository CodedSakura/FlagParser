import eu.thephisics101.utils.FlagParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FlagParserTest {
    @Test void empty() throws FlagParser.TokenException, FlagParser.MalformedInputException {
        FlagParser flagParser = new FlagParser("");
        assertFalse(flagParser.hasArgs());
        assertArrayEquals(new String[] {}, flagParser.getArgs());
        assertFalse(flagParser.hasOps());
        assertArrayEquals(new String[] {}, flagParser.getOps());
        assertFalse(flagParser.hasPairs());
        assertEquals(new HashMap<String, String>(), flagParser.getPairs());
    }

    static Stream<Arguments> argsStreamAndResultProvider() {
        return Stream.of(
                arguments("arg1 arg2", new String[] {"arg1", "arg2"}),
                arguments("'arg 3' \"arg 4\"", new String[] {"arg 3", "arg 4"}),
                arguments("'arg \\'5\\'' \"arg \\\"6\\\"\"", new String[] {"arg '5'", "arg \"6\""}),
                arguments("arg7 'arg 8' \"arg \\\"9\\\"\"", new String[] {"arg7", "arg 8", "arg \"9\""}),
                arguments("arg\\\"10 \"arg 11\"", new String[] {"arg\"10", "arg 11"}),
                arguments("\"\"", new String[] {""}),
                arguments("'long arg 12' \"long arg 13\"", new String[] {"long arg 12", "long arg 13"}),
                arguments("'long \\'arg\\' 14' \"long \\\"arg\\\" 15\"", new String[] {"long 'arg' 14", "long \"arg\" 15"}),
                arguments("\"arg 16\\\\\"", new String[] {"arg 16\\"}),
                arguments("arg\\ 17", new String[] {"arg 17"})
        );
    }
    @ParameterizedTest
    @MethodSource("argsStreamAndResultProvider")
    void args(String input, String[] expected) throws FlagParser.TokenException, FlagParser.MalformedInputException {
        FlagParser flagParser = new FlagParser(input);
        assertTrue(flagParser.hasArgs());
        assertArrayEquals(expected, flagParser.getArgs());
        assertFalse(flagParser.hasOps());
        assertArrayEquals(new String[] {}, flagParser.getOps());
        assertFalse(flagParser.hasPairs());
        assertEquals(new HashMap<String, String>(), flagParser.getPairs());
    }

    static Stream<Arguments> opsStreamAndResultProvider() {
        HashMap<String, String> emptyMap = new HashMap<>();
        HashMap<String, String> map1 = new HashMap<>();
        map1.put("qwe", "rty");
        map1.put("asd", "fgh");
        HashMap<String, String> map2 = new HashMap<>();
        map2.put("rty", "qwe rty");
        map2.put("fgh", "asd fgh");
        return Stream.of(
                arguments("-abC -d -a", new String[] {"a", "b", "C", "d"}, emptyMap),
                arguments("--qwe --rty", new String[] {"qwe", "rty"}, emptyMap),
                arguments("--qwe=rty --asd=fgh", new String[] {"qwe", "asd"}, map1),
                arguments("--rty='qwe rty' --fgh=\"asd fgh\"", new String[] {"rty", "fgh"}, map2)
        );
    }
    @ParameterizedTest
    @MethodSource("opsStreamAndResultProvider")
    void ops(String input, String[] ops, HashMap<String, String> pairs) throws FlagParser.TokenException, FlagParser.MalformedInputException {
        FlagParser flagParser = new FlagParser(input);
        assertFalse(flagParser.hasArgs());
        assertArrayEquals(new String[] {}, flagParser.getArgs());
        assertTrue(flagParser.hasOps());
        assertArrayEquals(ops, flagParser.getOps());
        assertEquals(pairs.size() > 0, flagParser.hasPairs());
        assertEquals(pairs, flagParser.getPairs());
    }

    static Stream<Arguments> malformedInputStreamAndResultProvider() {
        return Stream.of(
                arguments("'arg 1'arg2", 6),
                arguments("'arg 1''arg 2'", 6),
                arguments("\"arg 1\"'arg 2'", 6),
                arguments("'''", 2),
                arguments("\"", 0)
        );
    }
    @ParameterizedTest
    @MethodSource("malformedInputStreamAndResultProvider")
    void malformedInput(String input, int errorIndex) {
        FlagParser.MalformedInputException e = assertThrows(FlagParser.MalformedInputException.class,
                () -> new FlagParser(input));
        assertEquals(errorIndex, e.errorIndex);
    }

    @Test void combined() throws FlagParser.TokenException, FlagParser.MalformedInputException {
        FlagParser flagParser = new FlagParser("-a --op1='c d' -eFg arg1 \"arg 2\" --op2=ijk 'arg 3' --op3=\"m n o\" --op4 arg4 arg5");
        assertTrue(flagParser.hasArgs());
        assertArrayEquals(new String[] {"arg1", "arg 2", "arg 3", "arg4", "arg5"}, flagParser.getArgs());
        assertTrue(flagParser.hasOps());
        assertArrayEquals(new String[] {"a", "op1", "e", "F", "g", "op2", "op3", "op4"}, flagParser.getOps());
        HashMap<String, String> map = new HashMap<>();
        map.put("op1", "c d");
        map.put("op2", "ijk");
        map.put("op3", "m n o");
        assertTrue(flagParser.hasPairs());
        assertEquals(map, flagParser.getPairs());
    }

    static Stream<Arguments> tokenExceptionStreamAndResultProvider() {
        return Stream.of(
                arguments("arg'1", 3, "'"),
                arguments("arg\"1", 3, "\""),
                arguments("--'op 1'", 2, "'"),
                arguments("--\"op 1\"", 2, "\""),
                arguments("\\", 0, "\\"),
                arguments("-a-b", 2, "-")
        );
    }
    @ParameterizedTest
    @MethodSource("tokenExceptionStreamAndResultProvider")
    void tokenException(String input, int errorIndex, String problemToken) {
        FlagParser.TokenException e = assertThrows(FlagParser.TokenException.class,
                () -> new FlagParser(input));
        assertEquals(errorIndex, e.errorIndex);
        assertEquals(problemToken, e.problemToken);
    }
}