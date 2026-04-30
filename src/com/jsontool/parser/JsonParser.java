package com.jsontool.parser;

import com.jsontool.validator.JsonValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private final JsonValidator validator = new JsonValidator();
    private String json;
    private int position;
    private int length;

    public ParseResult parse(String jsonString) {
        JsonValidator.ValidationResult validation = validator.validate(jsonString);
        if (!validation.isValid()) {
            return new ParseResult(false, validation.getMessage(), null);
        }

        this.json = jsonString;
        this.length = jsonString.length();
        this.position = 0;

        try {
            skipWhitespace();
            Object result = parseValue();
            return new ParseResult(true, "解析成功", result);
        } catch (JsonParseException e) {
            return new ParseResult(false, e.getMessage(), null);
        }
    }

    private void skipWhitespace() {
        while (position < length) {
            char c = json.charAt(position);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                position++;
            } else {
                break;
            }
        }
    }

    private Object parseValue() throws JsonParseException {
        skipWhitespace();
        char c = json.charAt(position);

        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            return parseNumber();
        } else if (json.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        } else if (json.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        } else if (json.startsWith("null", position)) {
            position += 4;
            return null;
        }

        throw new JsonParseException("在位置 " + position + " 发现无效值");
    }

    private Map<String, Object> parseObject() throws JsonParseException {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        position++; 
        skipWhitespace();

        if (json.charAt(position) == '}') {
            position++;
            return result;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            position++; 
            skipWhitespace();
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();

            if (json.charAt(position) == '}') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
            }
        }

        return result;
    }

    private List<Object> parseArray() throws JsonParseException {
        List<Object> result = new ArrayList<Object>();
        position++; 
        skipWhitespace();

        if (json.charAt(position) == ']') {
            position++;
            return result;
        }

        while (true) {
            skipWhitespace();
            Object value = parseValue();
            result.add(value);
            skipWhitespace();

            if (json.charAt(position) == ']') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
            }
        }

        return result;
    }

    private String parseString() throws JsonParseException {
        position++; 
        StringBuilder result = new StringBuilder();

        while (position < length) {
            char c = json.charAt(position);

            if (c == '"') {
                position++;
                return result.toString();
            } else if (c == '\\') {
                position++;
                result.append(parseEscapeSequence());
            } else {
                result.append(c);
                position++;
            }
        }

        throw new JsonParseException("字符串未闭合");
    }

    private char parseEscapeSequence() throws JsonParseException {
        char c = json.charAt(position);
        position++;

        switch (c) {
            case '"':
                return '"';
            case '\\':
                return '\\';
            case '/':
                return '/';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                return parseUnicodeEscape();
            default:
                throw new JsonParseException("无效的转义序列: \\" + c);
        }
    }

    private char parseUnicodeEscape() throws JsonParseException {
        if (position + 3 >= length) {
            throw new JsonParseException("Unicode转义序列不完整");
        }

        String hex = json.substring(position, position + 4);
        position += 4;

        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new JsonParseException("无效的Unicode序列: \\u" + hex);
        }
    }

    private Number parseNumber() throws JsonParseException {
        int start = position;
        boolean isFloat = false;

        if (json.charAt(position) == '-') {
            position++;
        }

        if (json.charAt(position) == '0') {
            position++;
        } else {
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }

        if (position < length && json.charAt(position) == '.') {
            isFloat = true;
            position++;
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }

        if (position < length && (json.charAt(position) == 'e' || json.charAt(position) == 'E')) {
            isFloat = true;
            position++;
            if (position < length && (json.charAt(position) == '+' || json.charAt(position) == '-')) {
                position++;
            }
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }

        String numberStr = json.substring(start, position);

        try {
            if (isFloat) {
                return Double.parseDouble(numberStr);
            } else {
                try {
                    return Integer.parseInt(numberStr);
                } catch (NumberFormatException e) {
                    return Long.parseLong(numberStr);
                }
            }
        } catch (NumberFormatException e) {
            throw new JsonParseException("无效的数字格式: " + numberStr);
        }
    }

    public static class ParseResult {
        private final boolean success;
        private final String message;
        private final Object value;

        public ParseResult(boolean success, String message, Object value) {
            this.success = success;
            this.message = message;
            this.value = value;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getValue() {
            return value;
        }
    }

    private static class JsonParseException extends Exception {
        public JsonParseException(String message) {
            super(message);
        }
    }
}
