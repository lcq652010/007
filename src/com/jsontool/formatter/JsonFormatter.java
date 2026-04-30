package com.jsontool.formatter;

import java.util.List;
import java.util.Map;

public class JsonFormatter {
    private static final String DEFAULT_INDENT = "    ";
    private final String indent;

    public JsonFormatter() {
        this.indent = DEFAULT_INDENT;
    }

    public JsonFormatter(int indentSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentSize; i++) {
            sb.append(' ');
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
            result.append("null");
        } else if (value instanceof Map) {
            formatObject((Map<String, Object>) value, result, level);
        } else if (value instanceof List) {
            formatArray((List<Object>) value, result, level);
        } else if (value instanceof String) {
            formatString((String) value, result);
        } else if (value instanceof Number) {
            result.append(value.toString());
        } else if (value instanceof Boolean) {
            result.append(((Boolean) value).toString());
        } else {
            throw new JsonFormatException("不支持的类型: " + value.getClass().getName());
        }
    }

    private void formatObject(Map<String, Object> obj, StringBuilder result, int level) throws JsonFormatException {
        if (obj.isEmpty()) {
            result.append("{}");
            return;
        }

        result.append("{\n");
        int newLevel = level + 1;
        boolean first = true;

        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!first) {
                result.append(",\n");
            }
            first = false;

            addIndent(result, newLevel);
            formatString(entry.getKey(), result);
            result.append(": ");
            formatValue(entry.getValue(), result, newLevel);
        }

        result.append('\n');
        addIndent(result, level);
        result.append('}');
    }

    private void formatArray(List<Object> array, StringBuilder result, int level) throws JsonFormatException {
        if (array.isEmpty()) {
            result.append("[]");
            return;
        }

        result.append("[\n");
        int newLevel = level + 1;
        boolean first = true;

        for (Object item : array) {
            if (!first) {
                result.append(",\n");
            }
            first = false;

            addIndent(result, newLevel);
            formatValue(item, result, newLevel);
        }

        result.append('\n');
        addIndent(result, level);
        result.append(']');
    }

    private void formatString(String str, StringBuilder result) {
        result.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < 0x20 || (c >= 0x7f && c <= 0x9f)) {
                        result.append(String.format("\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        result.append('"');
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
            result.append("null");
        } else if (value instanceof Map) {
            formatObjectMinified((Map<String, Object>) value, result);
        } else if (value instanceof List) {
            formatArrayMinified((List<Object>) value, result);
        } else if (value instanceof String) {
            formatString((String) value, result);
        } else if (value instanceof Number) {
            result.append(value.toString());
        } else if (value instanceof Boolean) {
            result.append(((Boolean) value).toString());
        } else {
            throw new JsonFormatException("不支持的类型: " + value.getClass().getName());
        }
    }

    private void formatObjectMinified(Map<String, Object> obj, StringBuilder result) throws JsonFormatException {
        if (obj.isEmpty()) {
            result.append("{}");
            return;
        }

        result.append('{');
        boolean first = true;

        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (!first) {
                result.append(',');
            }
            first = false;

            formatString(entry.getKey(), result);
            result.append(':');
            formatValueMinified(entry.getValue(), result);
        }

        result.append('}');
    }

    private void formatArrayMinified(List<Object> array, StringBuilder result) throws JsonFormatException {
        if (array.isEmpty()) {
            result.append("[]");
            return;
        }

        result.append('[');
        boolean first = true;

        for (Object item : array) {
            if (!first) {
                result.append(',');
            }
            first = false;

            formatValueMinified(item, result);
        }

        result.append(']');
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
