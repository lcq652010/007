package com.jsontool.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class JsonConstants {
    
    private JsonConstants() {
    }

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    public static final char BOM_CHAR = '\uFEFF';
    public static final String JSON_EXTENSION = ".json";
    public static final int DEFAULT_INDENT_SIZE = 4;
    public static final char SPACE = ' ';
    public static final char TAB = '\t';
    public static final char LF = '\n';
    public static final char CR = '\r';
    public static final String NEWLINE = String.valueOf(LF);
    public static final char OBJECT_START = '{';
    public static final char OBJECT_END = '}';
    public static final String EMPTY_OBJECT = "{}";
    public static final char ARRAY_START = '[';
    public static final char ARRAY_END = ']';
    public static final String EMPTY_ARRAY = "[]";
    public static final char STRING_DELIMITER = '"';
    public static final char ESCAPE_CHAR = '\\';
    public static final char KEY_VALUE_SEPARATOR = ':';
    public static final String KEY_VALUE_SEPARATOR_PRETTY = ": ";
    public static final char VALUE_SEPARATOR = ',';
    public static final String VALUE_SEPARATOR_PRETTY = ",\n";
    public static final String JSON_NULL = "null";
    public static final String JSON_TRUE = "true";
    public static final String JSON_FALSE = "false";
    public static final char CHAR_BACKSPACE = '\b';
    public static final char CHAR_FORMFEED = '\f';
    public static final char CHAR_NEWLINE = '\n';
    public static final char CHAR_RETURN = '\r';
    public static final char CHAR_TAB = '\t';
    public static final String ESC_BACKSPACE = "\\b";
    public static final String ESC_FORMFEED = "\\f";
    public static final String ESC_NEWLINE = "\\n";
    public static final String ESC_RETURN = "\\r";
    public static final String ESC_TAB = "\\t";
    public static final String ESC_DOUBLE_QUOTE = "\\\"";
    public static final String ESC_BACKSLASH = "\\\\";
    public static final String ESC_SLASH = "\\/";
    public static final String UNICODE_ESCAPE_FORMAT = "\\u%04x";
    public static final int UNICODE_ESCAPE_LENGTH = 4;
    public static final char UNICODE_ESCAPE_PREFIX = 'u';
    public static final char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    public static final String SINGLE_LINE_COMMENT_START = "//";
    public static final String BLOCK_COMMENT_START = "/*";
    public static final String BLOCK_COMMENT_END = "*/";
    public static final int MIN_BUFFER_SIZE = 4096;
    public static final int STREAM_COPY_BUFFER_SIZE = 8192;
}
