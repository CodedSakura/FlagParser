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
    private enum State { NONE, SOP, OP, LOP, LOP_VAL, ARG}

    /** Copy of input string */
    private String input;

    /**
     * Constructor, which parses input on initialisation
     * @param input String input
     * @throws TokenException when there is an unnecessary token in the input
     * @throws MalformedInputException when input cannot be parsed
     */
    public FlagParser(String input) throws TokenException, MalformedInputException {
        this.input = input;
        StringBuilder tmp = null;
        String hold = "";
        State state = State.NONE;
        boolean escape = false;
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char cc = input.charAt(i);
            switch (state) {
                case NONE: {
                    if (cc == '-') {
                        state = State.SOP;
                    } else {
                        if (cc == ESCAPE_CHAR) {
                            escape = !escape;
                        }
                        state = State.ARG;
                    }
                    break;
                }
                case SOP:
                    if (cc == '-') {
                        state = State.LOP;
                        break;
                    } else {
                        state = State.OP;
                    }
                case OP: {
                    if (cc == '-') {
                        if (tmp == null || tmp.length() == 0) {
                            state = State.LOP;
                        } else {
                            throw new TokenException(Character.toString(cc), i);
                        }
                    } else {
                        if (isValidOpChar(cc)) {
                            ops.add(Character.toString(cc));
                        } else {
                            throw new TokenException(Character.toString(cc), i);
                        }
                    }
                    break;
                }
                case LOP: {
                    break;
                }
                case LOP_VAL: {
                    break;
                }
                case ARG: {
                    break;
                }
            }
        }

        System.out.printf("%b: %s%n", hasArgs(), Arrays.toString(getArgs()));
        System.out.printf("%b: %s%n", hasOps(), Arrays.toString(getOps()));
        System.out.printf("%b: %s%n", hasPairs(), getPairs());
    }

    private boolean isValidOpChar(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }

    private boolean startsWithQuote(final String in) {
        for (char c : QUOTES) {
            if (in.startsWith(Character.toString(c))) return true;
        }
        return false;
    }

    private boolean endsWithQuote(final String in, final char[] quotes) {
        for (char c : quotes) {
            if (!in.endsWith(Character.toString(ESCAPE_CHAR) + c) && in.endsWith(Character.toString(c))) return true;
        }
        return false;
    }

    private boolean containsQuote(final String in) {
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
     * @param in input string
     * @param position position for validity reporting
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

    //region Getters
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
    //endregion

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
