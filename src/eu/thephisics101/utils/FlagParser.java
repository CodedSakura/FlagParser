package eu.thephisics101.utils;

import java.util.*;

/**
 * Java based flag and argument parser
 * @author thephisics101
 * @version 0.0.1
 */
public class FlagParser {
    /** Parsed arguments */
    private LinkedHashSet<String> args = new LinkedHashSet<>();
    /** Parsed options */
    private LinkedHashSet<String> ops = new LinkedHashSet<>();
    /** Parsed option-value pairs */
    private HashMap<String, String> pairs = new HashMap<>();

    /** Quote literals */
    private static final char[] QUOTES = {'\'', '"'};
    /** Escape character literal */
    private static final char ESCAPE_CHAR = '\\';

    /** Parser state possibilities */
    private enum State { NONE, ARG, OP }

    /** Copy of input string */
    private String input;

    /**
     * Constructor, which parses input on initialisation
     * @param input - String input
     * @throws TokenException when there is an unnecessary token in the input
     * @throws MalformedInputException when input cannot be parsed
     */
    public FlagParser(String input) throws TokenException, MalformedInputException {
        this.input = input;
        StringBuilder tmp = null;
        String hold = "";
        State state = State.NONE;
        int position = 0;
        if (input.endsWith(Character.toString(ESCAPE_CHAR)))
            throw new TokenException(input.substring(input.length() - 1), input.length() - 1);
        for (String i : input.split(" ")) {
            if (i.startsWith("--")) {
                String[] split = i.substring(2).split("=", 2);
                if (startsWithQuote(split[0])) throw new TokenException(split[0].substring(0, 1), position + 2);
                if (split.length > 1) {
                    if (startsWithQuote(split[1])) {
                        if (endsWithQuote(split[1], split[1].substring(0, 1).toCharArray())) {
                            pairs.put(split[0], stripNCheck(split[1], split[0].length() + position));
                        } else {
                            tmp = new StringBuilder(split[1]);
                            state = State.OP;
                            hold = split[0];
                        }
                    } else pairs.put(split[0], split[1]);
                }
                ops.add(split[0]);
            } else if (state == State.OP) {
                tmp.append(" ").append(i);
                if (endsWithQuote(i, tmp.substring(0, 1).toCharArray())) {
                    state = State.NONE;
                    pairs.put(hold, stripNCheck(tmp.toString(), position - tmp.length() + i.length()));
                }
            } else if (i.startsWith("-")) {
                if (containsQuote(i)) throw new TokenException("'", position);
                ops.addAll(Arrays.asList(i.substring(1).split("")));
            } else if (startsWithQuote(i)) {
                if (endsWithQuote(i, i.substring(0, 1).toCharArray())) {
                    args.add(stripNCheck(i, position));
                } else {
                    tmp = new StringBuilder(i);
                    state = State.ARG;
                }
            } else if (state == State.ARG) {
                tmp.append(" ").append(i);
                if (endsWithQuote(i, tmp.substring(0, 1).toCharArray())) {
                    state = State.NONE;
                    args.add(stripNCheck(tmp.toString(), position - tmp.length() + i.length()));
                }
            } else if (!i.equals("")) args.add(stripNCheck(i, position));
            position += i.length() + 1;
        }

        System.out.printf("%b: %s%n", hasArgs(), Arrays.toString(getArgs()));
        System.out.printf("%b: %s%n", hasOps(), Arrays.toString(getOps()));
        System.out.printf("%b: %s%n", hasPairs(), getPairs());
    }

    private boolean startsWithQuote(String in) {
        for (char c : QUOTES) {
            if (in.startsWith(Character.toString(c))) return true;
        }
        return false;
    }

    private boolean endsWithQuote(String in, char[] quotes) {
        for (char c : quotes) {
            if (!in.endsWith(Character.toString(ESCAPE_CHAR) + c) &&
                    in.endsWith(Character.toString(c))) return true;
        }
        return false;
    }

    private boolean containsQuote(String in) {
        if (startsWithQuote(in)) return true;
        if (endsWithQuote(in, QUOTES)) return true;
        for (char c : QUOTES) {
            if (in.contains(Character.toString(c))) {
                int i = in.indexOf(Character.toString(c));
                return in.charAt(i - 1) != ESCAPE_CHAR;
            }
        }
        return false;
    }

    /**
     * Strip surrounding quotes and check string validity
     * @param in - input string
     * @param position - position for validity reporting
     * @return stripped string
     * @throws TokenException if there is a problem within the input
     */
    private String stripNCheck(String in, int position) throws TokenException {
        if (startsWithQuote(in) && endsWithQuote(in, in.substring(0, 1).toCharArray()))
            in = in.substring(1, in.length() - 1);
        System.out.println(position);
        if (containsQuote(in)) {
            String data = in.split("(?<!\\\\)['\"]")[0];
            throw new TokenException(Character.toString(in.charAt(data.length())), data.length());
        }
        for (char c : QUOTES) in = in.replaceAll("\\\\" + c, Character.toString(c));
        return in;
    }

    /**
     * @return whether there are arguments
     */
    public boolean hasArgs() { return args.size() > 0; }

    /**
     * Get input arguments
     * @return array of ordered arguments
     */
    public String[] getArgs() { return args.toArray(new String[]{}); }

    /**
     * @return whether there are options
     */
    public boolean hasOps() { return ops.size() > 0; }

    /**
     * Get input options
     * @return array of ordered options
     */
    public String[] getOps() { return ops.toArray(new String[]{}); }

    /**
     * @return whether there are option-value pairs
     */
    public boolean hasPairs() { return pairs.size() > 0; }

    /**
     * Get input option-value pairs
     * @return map of ordered option-value pairs
     */
    public HashMap<String, String> getPairs() { return pairs; }

    /**
     * Exception when a token is misplaced
     */
    public class TokenException extends Exception {
        public int errorIndex;
        public String problemToken;

        TokenException(String token, int index) {
            super(String.format("Unexpected token (%s) in input \"%s\" at index (%d)", token, input, index));
            errorIndex = index;
            problemToken = token;
        }
    }

    /**
     * Exception when input cannot be parsed
     */
    public static class MalformedInputException extends Exception {
        public int errorIndex;

        MalformedInputException(int index) {
            super(String.format("Malformed input at index (%d)", index));
            errorIndex = index;
        }
    }
}
