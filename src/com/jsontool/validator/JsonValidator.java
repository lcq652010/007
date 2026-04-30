package com.jsontool.validator;

import java.util.Stack;

public class JsonValidator {
    private int position;
    private String json;
    private int length;

    public ValidationResult validate(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ValidationResult(false, "JSON字符串为空");
        }
        
        this.json = jsonString;
        this.length = jsonString.length();
        this.position = 0;
        
        try {
            skipWhitespace();
            
            if (position >= length) {
                return new ValidationResult(false, "JSON字符串为空");
            }
            
            char firstChar = json.charAt(position);
            
            if (firstChar == '{') {
                parseObject();
            } else if (firstChar == '[') {
                parseArray();
            } else {
                return new ValidationResult(false, "JSON必须以'{'或'['开头");
            }
            
            skipWhitespace();
            
            if (position < length) {
                return new ValidationResult(false, "在位置 " + position + " 发现多余的字符: " + json.charAt(position));
            }
            
            return new ValidationResult(true, "JSON语法有效");
        } catch (JsonValidationException e) {
            return new ValidationResult(false, e.getMessage());
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

    private void parseObject() throws JsonValidationException {
        expectChar('{');
        position++;
        skipWhitespace();
        
        if (json.charAt(position) == '}') {
            position++;
            return;
        }
        
        while (true) {
            skipWhitespace();
            parseString();
            skipWhitespace();
            expectChar(':');
            position++;
            skipWhitespace();
            parseValue();
            skipWhitespace();
            
            if (json.charAt(position) == '}') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
                if (json.charAt(position) == '}') {
                    throw new JsonValidationException("在位置 " + position + " 发现尾随逗号");
                }
            } else {
                throw new JsonValidationException("在位置 " + position + " 期望 '}' 或 ',' 但发现 '" + json.charAt(position) + "'");
            }
        }
    }

    private void parseArray() throws JsonValidationException {
        expectChar('[');
        position++;
        skipWhitespace();
        
        if (json.charAt(position) == ']') {
            position++;
            return;
        }
        
        while (true) {
            skipWhitespace();
            parseValue();
            skipWhitespace();
            
            if (json.charAt(position) == ']') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
                if (json.charAt(position) == ']') {
                    throw new JsonValidationException("在位置 " + position + " 发现尾随逗号");
                }
            } else {
                throw new JsonValidationException("在位置 " + position + " 期望 ']' 或 ',' 但发现 '" + json.charAt(position) + "'");
            }
        }
    }

    private void parseValue() throws JsonValidationException {
        skipWhitespace();
        if (position >= length) {
            throw new JsonValidationException("在位置 " + position + " 意外到达字符串末尾");
        }
        
        char c = json.charAt(position);
        
        if (c == '{') {
            parseObject();
        } else if (c == '[') {
            parseArray();
        } else if (c == '"') {
            parseString();
        } else if (c == '-' || (c >= '0' && c <= '9')) {
            parseNumber();
        } else if (json.startsWith("true", position)) {
            position += 4;
        } else if (json.startsWith("false", position)) {
            position += 5;
        } else if (json.startsWith("null", position)) {
            position += 4;
        } else {
            throw new JsonValidationException("在位置 " + position + " 发现无效值起始字符: '" + c + "'");
        }
    }

    private void parseString() throws JsonValidationException {
        expectChar('"');
        position++;
        
        while (position < length) {
            char c = json.charAt(position);
            
            if (c == '"') {
                position++;
                return;
            } else if (c == '\\') {
                position++;
                parseEscapeSequence();
            } else {
                position++;
            }
        }
        
        throw new JsonValidationException("在位置 " + position + " 字符串未闭合");
    }

    private void parseEscapeSequence() throws JsonValidationException {
        if (position >= length) {
            throw new JsonValidationException("在位置 " + position + " 转义序列未完成");
        }
        
        char c = json.charAt(position);
        position++;
        
        switch (c) {
            case '"':
            case '\\':
            case '/':
            case 'b':
            case 'f':
            case 'n':
            case 'r':
            case 't':
                break;
            case 'u':
                parseUnicodeEscape();
                break;
            default:
                throw new JsonValidationException("在位置 " + (position - 1) + " 发现无效转义序列: \\" + c);
        }
    }

    private void parseUnicodeEscape() throws JsonValidationException {
        if (position + 3 >= length) {
            throw new JsonValidationException("在位置 " + position + " Unicode转义序列不完整");
        }
        
        for (int i = 0; i < 4; i++) {
            char c = json.charAt(position + i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                throw new JsonValidationException("在位置 " + (position + i) + " 发现无效Unicode字符: '" + c + "'");
            }
        }
        
        position += 4;
    }

    private void parseNumber() throws JsonValidationException {
        if (json.charAt(position) == '-') {
            position++;
            if (position >= length || !(json.charAt(position) >= '0' && json.charAt(position) <= '9')) {
                throw new JsonValidationException("在位置 " + (position - 1) + " 减号后缺少数字");
            }
        }
        
        if (json.charAt(position) == '0') {
            position++;
            if (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                throw new JsonValidationException("在位置 " + (position - 1) + " 数字有前导零");
            }
        } else {
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }
        
        if (position < length && json.charAt(position) == '.') {
            position++;
            if (position >= length || !(json.charAt(position) >= '0' && json.charAt(position) <= '9')) {
                throw new JsonValidationException("在位置 " + (position - 1) + " 小数点后缺少数字");
            }
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }
        
        if (position < length && (json.charAt(position) == 'e' || json.charAt(position) == 'E')) {
            position++;
            if (position < length && (json.charAt(position) == '+' || json.charAt(position) == '-')) {
                position++;
            }
            if (position >= length || !(json.charAt(position) >= '0' && json.charAt(position) <= '9')) {
                throw new JsonValidationException("在位置 " + (position - 1) + " 指数部分缺少数字");
            }
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }
    }

    private void expectChar(char expected) throws JsonValidationException {
        if (position >= length || json.charAt(position) != expected) {
            throw new JsonValidationException("在位置 " + position + " 期望 '" + expected + "' 但发现 '" + 
                (position < length ? json.charAt(position) : "EOF") + "'");
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class JsonValidationException extends Exception {
        public JsonValidationException(String message) {
            super(message);
        }
    }
}
