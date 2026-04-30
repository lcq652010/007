package com.jsontool.formatter;

import java.util.List;
import java.util.Map;

import static com.jsontool.util.JsonConstants.*;

public class JsonFormatter {
    private static final String DEFAULT_INDENT;
    
    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DEFAULT_INDENT_SIZE; i++) {
            sb.append(SPACE);
        }
        DEFAULT_INDENT = sb.toString();
    }
    
    private final String indent;

    public JsonFormatter() {
        this.indent = DEFAULT_INDENT;
    }

    public JsonFormatter(int indentSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentSize; i++) {
            sb.append(SPACE);
        }
        this.indent = sb.toString();
    }

    public FormatResult format(Object jsonValue) {
        if (jsonValue == null) {
            return new FormatResult(false, "JSON值为空", null);
        }

        try {
            StringBuilder result = new StringBuilder();
            formatValue(jsonValue, result, 0);
            return new FormatResult(true, "格式化成功", result.toString());
        } catch (JsonFormatException e) {
            return new FormatResult(false, e.getMessage(), null);
        }
    }

    private void formatValue(Object value, StringBuilder result, int level) throws JsonFormatException {
        if (value == null) {
            result.append(JSON_NULL);
        } else if (value instanceof Map) {
            formatObject((Map<String, Object>) value, result, level);
        } else if (value instanceof List) {
            formatArray((List<Object>) value, result, level);
        } else if (value instanceof String) {
            formatString((String) value, result);
        } else if (value instanceof Number) {
            result.append(value.toString());
        } else if (value instanceof Boolean) {
            result.append(((Boolean) value).booleanValue() ? JSON_TRUE : JSON_FALSE);
        } else {
            throw new JsonFormatException("不支持的类型: " + value.getClass().getName());
        }
    }

    private void formatObject(Map<String, Object> obj, StringBuilder result, int level) throws JsonFormatException {
        if (obj.isEmpty()) {
            result.append(EMPTY_OBJECT);
            return;
        }

        result.append(OBJECT_START).append(NEWLINE);
        int newLevel = level + 1;
        boolean first = true;

        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!first) {
                result.append(VALUE_SEPARATOR_PRETTY);
            }
            first = false;

            addIndent(result, newLevel);
            formatString(entry.getKey(), result);
            result.append(KEY_VALUE_SEPARATOR_PRETTY);
            formatValue(entry.getValue(), result, newLevel);
        }

        result.append(LF);
        addIndent(result, level);
        result.append(OBJECT_END);
    }

    private void formatArray(List<Object> array, StringBuilder result, int level) throws JsonFormatException {
        if (array.isEmpty()) {
            result.append(EMPTY_ARRAY);
            return;
        }

        result.append(ARRAY_START).append(NEWLINE);
        int newLevel = level + 1;
        boolean first = true;

        for (Object item : array) {
            if (!first) {
                result.append(VALUE_SEPARATOR_PRETTY);
            }
            first = false;

            addIndent(result, newLevel);
            formatValue(item, result, newLevel);
        }

        result.append(LF);
        addIndent(result, level);
        result.append(ARRAY_END);
    }

    private void formatString(String str, StringBuilder result) {
        result.append(STRING_DELIMITER);
        for (char c : str.toCharArray()) {
            if (c == STRING_DELIMITER) {
                result.append(ESC_DOUBLE_QUOTE);
            } else if (c == ESCAPE_CHAR) {
                result.append(ESC_BACKSLASH);
            } else if (c == CHAR_BACKSPACE) {
                result.append(ESC_BACKSPACE);
            } else if (c == CHAR_FORMFEED) {
                result.append(ESC_FORMFEED);
            } else if (c == CHAR_NEWLINE) {
                result.append(ESC_NEWLINE);
            } else if (c == CHAR_RETURN) {
                result.append(ESC_RETURN);
            } else if (c == CHAR_TAB) {
                result.append(ESC_TAB);
            } else {
                if (c < 0x20 || (c >= 0x7f && c <= 0x9f)) {
                    result.append(String.format(UNICODE_ESCAPE_FORMAT, (int) c));
                } else {
                    result.append(c);
                }
            }
        }
        result.append(STRING_DELIMITER);
    }

    private void addIndent(StringBuilder result, int level) {
        for (int i = 0; i < level; i++) {
            result.append(indent);
        }
    }

    public FormatResult formatMinified(Object jsonValue) {
        if (jsonValue == null) {
            return new FormatResult(false, "JSON值为空", null);
        }

        try {
            StringBuilder result = new StringBuilder();
            formatValueMinified(jsonValue, result);
            return new FormatResult(true, "压缩成功", result.toString());
        } catch (JsonFormatException e) {
            return new FormatResult(false, e.getMessage(), null);
        }
    }

    private void formatValueMinified(Object value, StringBuilder result) throws JsonFormatException {
        if (value == null) {
            result.append(JSON_NULL);
        } else if (value instanceof Map) {
            formatObjectMinified((Map<String, Object>) value, result);
        } else if (value instanceof List) {
            formatArrayMinified((List<Object>) value, result);
        } else if (value instanceof String) {
            formatString((String) value, result);
        } else if (value instanceof Number) {
            result.append(value.toString());
        } else if (value instanceof Boolean) {
            result.append(((Boolean) value).booleanValue() ? JSON_TRUE : JSON_FALSE);
        } else {
            throw new JsonFormatException("不支持的类型: " + value.getClass().getName());
        }
    }

    private void formatObjectMinified(Map<String, Object> obj, StringBuilder result) throws JsonFormatException {
        if (obj.isEmpty()) {
            result.append(EMPTY_OBJECT);
            return;
        }

        result.append(OBJECT_START);
        boolean first = true;

        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!first) {
                result.append(VALUE_SEPARATOR);
            }
            first = false;

            formatString(entry.getKey(), result);
            result.append(KEY_VALUE_SEPARATOR);
            formatValueMinified(entry.getValue(), result);
        }

        result.append(OBJECT_END);
    }

    private void formatArrayMinified(List<Object> array, StringBuilder result) throws JsonFormatException {
        if (array.isEmpty()) {
            result.append(EMPTY_ARRAY);
            return;
        }

        result.append(ARRAY_START);
        boolean first = true;

        for (Object item : array) {
            if (!first) {
                result.append(VALUE_SEPARATOR);
            }
            first = false;

            formatValueMinified(item, result);
        }

        result.append(ARRAY_END);
    }

    public static class FormatResult {
        private final boolean success;
        private final String message;
        private final String formatted;

        public FormatResult(boolean success, String message, String formatted) {
            this.success = success;
            this.message = message;
            this.formatted = formatted;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getFormatted() {
            return formatted;
        }
    }

    private static class JsonFormatException extends Exception {
        public JsonFormatException(String message) {
            super(message);
        }
    }
}
