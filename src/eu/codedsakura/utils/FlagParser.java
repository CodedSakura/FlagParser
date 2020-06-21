package eu.codedsakura.utils;

import java.util.*;

/**
 * Java based flag and argument parser
 * @author codedsakura
 * @version 0.0.1
 */
public class FlagParser {
    /** Parsed arguments */
    private ArrayList<String> args = new ArrayList<>();
    /** Parsed options */
    private LinkedHashSet<String> ops = new LinkedHashSet<>();
    /** Parsed option-value pairs */
    private HashMap<String, String> pairs = new HashMap<>();

    /** Quote literals */
    private static final char[] QUOTES = {'\'', '"'};
    /** Escape character literal */
    private static final char ESCAPE_CHAR = '\\';

    /** Parser state possibilities */
    private enum State { NONE, SOP, OP, LOP, LOP_VAL, ARG }

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
        String buff = null;
        char hold = 0;
        State state = State.NONE;
        boolean escape = false;
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char cc = input.charAt(i);
//            System.out.printf("%7s '%s'; hold=%2d tmp=%s\n", state, cc, (int) hold, tmp);
            switch (state) {
                case NONE:
                    if (cc == '-') {
                        state = State.SOP;
                        break;
                    } else {
                        state = State.ARG;
                        if (cc == ESCAPE_CHAR) {
                            escape = !escape;
                            break;
                        }
                    }
                case ARG:
                    if (Character.isWhitespace(cc)) {
                        if (tmp == null) {
                            throw new MalformedInputException(i);
                        } else if (escape || isQuote(hold)) {
                            tmp.append(cc);
                        } else {
                            args.add(tmp.toString());
                            tmp = null;
                            hold = 0;
                            state = State.NONE;
                        }
                    } else if (hold == '\n') {
                        throw new MalformedInputException(i);
                    } else if (isQuote(cc)) {
                        if (escape) {
                            if (tmp == null) tmp = new StringBuilder();
                            tmp.append(cc);
                            escape = false;
                        } else {
                            if (tmp != null) {
                                if (cc == hold) {
                                    hold = '\n';
                                } else {
                                    throw new TokenException(Character.toString(cc), i);
                                }
                            } else {
                                tmp = new StringBuilder();
                                hold = cc;
                            }
                        }
                    } else if (cc == '\\') {
                        if (escape) {
                            if (tmp == null) tmp = new StringBuilder();
                            tmp.append(cc);
                        }
                        escape = !escape;
                    } else {
                        escape = false;
                        if (tmp == null) tmp = new StringBuilder();
                        tmp.append(cc);
                    }
                    break;
                case SOP:
                    if (cc == '-') {
                        state = State.LOP;
                        break;
                    } else {
                        state = State.OP;
                    }
                case OP:
                    if (Character.isWhitespace(cc)) {
                        if (tmp != null) {
                            tmp = null;
                        } else {
                            args.add("-");
                        }
                        state = State.NONE;
                    } else if (isValidOpChar(cc)) {
                        ops.add(Character.toString(cc));
                        if (tmp == null) tmp = new StringBuilder();
                        tmp.append(cc);
                    } else {
                        throw new TokenException(Character.toString(cc), i);
                    }
                    break;
                case LOP:
                    if (cc == '-') {
                        if (tmp == null) {
                            throw new TokenException(Character.toString(cc), i);
                        } else {
                            tmp.append(cc);
                        }
                    } else if (Character.isWhitespace(cc)) {
                        if (tmp != null) {
                            buff = tmp.toString();
                            ops.add(buff);
                            tmp = null;
                        } else {
                            args.add("--");
                        }
                        state = State.NONE;
                    } else if (isValidOpChar(cc)) {
                        if (tmp == null) tmp = new StringBuilder();
                        tmp.append(cc);
                    } else if (cc == '=') {
                        if (tmp != null) {
                            buff = tmp.toString();
                            ops.add(buff);
                            tmp = null;
                            state = State.LOP_VAL;
                        } else {
                            throw new TokenException(Character.toString(cc), i);
                        }
                    } else {
                        throw new TokenException(Character.toString(cc), i);
                    }
                    break;
                case LOP_VAL:
                    if (Character.isWhitespace(cc)) {
                        if (tmp == null) {
                            throw new MalformedInputException(i);
                        } else if (escape || isQuote(hold)) {
                            tmp.append(cc);
                        } else {
                            pairs.put(buff, tmp.toString());
                            tmp = null;
                            hold = 0;
                            state = State.NONE;
                        }
                    } else if (hold == '\n') {
                        throw new MalformedInputException(i);
                    } else if (isQuote(cc)) {
                        if (escape) {
                            tmp.append(cc);
                        } else {
                            if (tmp != null) {
                                if (cc == hold) {
                                    hold = '\n';
                                } else {
                                    throw new TokenException(Character.toString(cc), i);
                                }
                            } else {
                                tmp = new StringBuilder();
                                hold = cc;
                            }
                        }
                    } else if (cc == '\\') {
                        if (escape) {
                            tmp.append(cc);
                        }
                        escape = !escape;
                    } else {
                        escape = false;
                        if (tmp == null) tmp = new StringBuilder();
                        tmp.append(cc);
                    }
                    break;
            }
        }

        // cleanup
        switch (state) {
            case ARG:
                if (isQuote(hold) || tmp == null) {
                    throw new MalformedInputException(length-1);
                } else {
                    args.add(tmp.toString());
                }
                break;
            case SOP:
                args.add("-");
                break;
            case LOP:
                if (tmp == null) {
                    throw new MalformedInputException(length-1);
                } else {
                    ops.add(tmp.toString());
                }
                break;
            case LOP_VAL:
                if (isQuote(hold) || tmp == null) {
                    throw new MalformedInputException(length-1);
                } else {
                    pairs.put(buff, tmp.toString());
                }
                break;
        }
//        System.out.printf("%b: %s%n", hasArgs(), Arrays.toString(getArgs()));
//        System.out.printf("%b: %s%n", hasOps(), Arrays.toString(getOps()));
//        System.out.printf("%b: %s%n", hasPairs(), getPairs());
    }

    private boolean isValidOpChar(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }

    private boolean isQuote(final char ch) {
        for (char q : QUOTES)
            if (q == ch) return true;
        return false;
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
