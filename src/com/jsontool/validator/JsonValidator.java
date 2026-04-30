package com.jsontool.validator;

import java.util.ArrayList;
import java.util.List;

import static com.jsontool.util.JsonConstants.*;

public class JsonValidator {
    private int position;
    private String json;
    private int length;
    private List<Integer> lineStartPositions;

    public ValidationResult validate(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ValidationResult(false, "JSON字符串为空");
        }
        
        this.json = jsonString;
        this.length = jsonString.length();
        this.position = 0;
        this.lineStartPositions = new ArrayList<Integer>();
        calculateLineStartPositions();
        
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
                return new ValidationResult(false, 
                    formatError(position, "JSON必须以'{'或'['开头，实际发现: '" + firstChar + "'"));
            }
            
            skipWhitespace();
            
            if (position < length) {
                return new ValidationResult(false, 
                    formatError(position, "发现多余的字符: '" + json.charAt(position) + "'"));
            }
            
            return new ValidationResult(true, "JSON语法有效");
        } catch (JsonValidationException e) {
            return new ValidationResult(false, e.getMessage());
        }
    }

    private void calculateLineStartPositions() {
        lineStartPositions.add(0);
        for (int i = 0; i < length; i++) {
            if (json.charAt(i) == LF) {
                lineStartPositions.add(i + 1);
            }
        }
    }

    private int getLineNumber(int pos) {
        int line = 1;
        for (int i = 0; i < lineStartPositions.size(); i++) {
            if (lineStartPositions.get(i) <= pos) {
                line = i + 1;
            } else {
                break;
            }
        }
        return line;
    }

    private int getColumnNumber(int pos) {
        int lineStart = 0;
        for (int i = 0; i < lineStartPositions.size(); i++) {
            if (lineStartPositions.get(i) <= pos) {
                lineStart = lineStartPositions.get(i);
            } else {
                break;
            }
        }
        return pos - lineStart + 1;
    }

    private String getLineContent(int lineNumber) {
        if (lineNumber < 1 || lineNumber > lineStartPositions.size()) {
            return "";
        }
        int startPos = lineStartPositions.get(lineNumber - 1);
        int endPos = length;
        if (lineNumber < lineStartPositions.size()) {
            endPos = lineStartPositions.get(lineNumber) - 1;
        }
        while (endPos > startPos && (json.charAt(endPos - 1) == CR || json.charAt(endPos - 1) == LF)) {
            endPos--;
        }
        return json.substring(startPos, endPos);
    }

    private String formatError(int pos, String message) {
        int line = getLineNumber(pos);
        int column = getColumnNumber(pos);
        String lineContent = getLineContent(line);
        
        StringBuilder result = new StringBuilder();
        result.append("第 ").append(line).append(" 行, 第 ").append(column).append(" 列: ");
        result.append(message);
        
        if (lineContent.length() > 0) {
            result.append("\n\n    ");
            result.append(lineContent);
            result.append("\n    ");
            for (int i = 0; i < column - 1; i++) {
                if (i < lineContent.length() && lineContent.charAt(i) == '\t') {
                    result.append('\t');
                } else {
                    result.append(' ');
                }
            }
            result.append('^');
        }
        
        return result.toString();
    }

    private String formatErrorWithChar(int pos, String message, char foundChar) {
        if (foundChar == '/') {
            String commentType = checkForComment(pos);
            if (commentType != null) {
                return formatError(pos, "JSON 不支持注释。发现 " + commentType);
            }
        }
        
        String charRepr;
        if (Character.isISOControl(foundChar) || Character.isWhitespace(foundChar)) {
            if (foundChar == LF) {
                charRepr = ESC_NEWLINE;
            } else if (foundChar == CR) {
                charRepr = ESC_RETURN;
            } else if (foundChar == TAB) {
                charRepr = ESC_TAB;
            } else if (foundChar == CHAR_BACKSPACE) {
                charRepr = ESC_BACKSPACE;
            } else if (foundChar == CHAR_FORMFEED) {
                charRepr = ESC_FORMFEED;
            } else {
                charRepr = String.format(UNICODE_ESCAPE_FORMAT, (int) foundChar);
            }
        } else {
            charRepr = "'" + foundChar + "'";
        }
        return formatError(pos, message + "，实际发现: " + charRepr);
    }

    private String checkForComment(int pos) {
        if (pos + 1 >= length) {
            return null;
        }
        
        char nextChar = json.charAt(pos + 1);
        if (nextChar == '/') {
            return "单行注释 " + SINGLE_LINE_COMMENT_START;
        } else if (nextChar == '*') {
            return "块注释 " + BLOCK_COMMENT_START + BLOCK_COMMENT_END;
        }
        return null;
    }

    private void skipWhitespace() {
        while (position < length) {
            char c = json.charAt(position);
            if (c == SPACE || c == TAB || c == LF || c == CR) {
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
        
        if (position >= length) {
            throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 '}'"));
        }
        
        if (json.charAt(position) == '}') {
            position++;
            return;
        }
        
        while (true) {
            skipWhitespace();
            if (position >= length) {
                throw new JsonValidationException(formatError(position, "意外到达字符串末尾，期望键名"));
            }
            parseString();
            skipWhitespace();
            if (position >= length) {
                throw new JsonValidationException(formatError(position, "意外到达字符串末尾，期望 ':'"));
            }
            expectChar(':');
            position++;
            skipWhitespace();
            parseValue();
            skipWhitespace();
            
            if (position >= length) {
                throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 '}'"));
            }
            
            if (json.charAt(position) == '}') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
                if (position >= length) {
                    throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 '}'"));
                }
                if (json.charAt(position) == '}') {
                    throw new JsonValidationException(formatError(position, "发现尾随逗号"));
                }
            } else {
                throw new JsonValidationException(formatErrorWithChar(position, "期望 '}' 或 ','", json.charAt(position)));
            }
        }
    }

    private void parseArray() throws JsonValidationException {
        expectChar('[');
        position++;
        skipWhitespace();
        
        if (position >= length) {
            throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 ']'"));
        }
        
        if (json.charAt(position) == ']') {
            position++;
            return;
        }
        
        while (true) {
            skipWhitespace();
            parseValue();
            skipWhitespace();
            
            if (position >= length) {
                throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 ']'"));
            }
            
            if (json.charAt(position) == ']') {
                position++;
                break;
            } else if (json.charAt(position) == ',') {
                position++;
                skipWhitespace();
                if (position >= length) {
                    throw new JsonValidationException(formatError(position, "意外到达字符串末尾，缺少 ']'"));
                }
                if (json.charAt(position) == ']') {
                    throw new JsonValidationException(formatError(position, "发现尾随逗号"));
                }
            } else {
                throw new JsonValidationException(formatErrorWithChar(position, "期望 ']' 或 ','", json.charAt(position)));
            }
        }
    }

    private void parseValue() throws JsonValidationException {
        skipWhitespace();
        if (position >= length) {
            throw new JsonValidationException(formatError(position, "意外到达字符串末尾，期望值"));
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
        } else if (json.startsWith(JSON_TRUE, position)) {
            position += JSON_TRUE.length();
        } else if (json.startsWith(JSON_FALSE, position)) {
            position += JSON_FALSE.length();
        } else if (json.startsWith(JSON_NULL, position)) {
            position += JSON_NULL.length();
        } else {
            throw new JsonValidationException(formatErrorWithChar(position, "发现无效值起始字符", c));
        }
    }

    private void parseString() throws JsonValidationException {
        int stringStartPos = position;
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
        
        throw new JsonValidationException(formatError(stringStartPos, "字符串未闭合，缺少结束的 '\"'"));
    }

    private void parseEscapeSequence() throws JsonValidationException {
        int escapeStartPos = position - 1;
        if (position >= length) {
            throw new JsonValidationException(formatError(escapeStartPos, "转义序列未完成"));
        }
        
        char c = json.charAt(position);
        position++;
        
        if (c == STRING_DELIMITER || c == ESCAPE_CHAR || c == '/' ||
            c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't') {
            // 有效的转义字符
        } else if (c == UNICODE_ESCAPE_PREFIX) {
            parseUnicodeEscape();
        } else {
            throw new JsonValidationException(formatError(escapeStartPos, "发现无效转义序列: \\" + c));
        }
    }

    private void parseUnicodeEscape() throws JsonValidationException {
        int unicodeStartPos = position - 2;
        if (position + UNICODE_ESCAPE_LENGTH - 1 >= length) {
            throw new JsonValidationException(formatError(unicodeStartPos, "Unicode转义序列不完整，需要" + UNICODE_ESCAPE_LENGTH + "个十六进制数字"));
        }
        
        for (int i = 0; i < UNICODE_ESCAPE_LENGTH; i++) {
            char c = json.charAt(position + i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                throw new JsonValidationException(formatErrorWithChar(position + i, "Unicode转义序列中发现无效字符", c));
            }
        }
        
        position += UNICODE_ESCAPE_LENGTH;
    }

    private void parseNumber() throws JsonValidationException {
        int numberStartPos = position;
        
        if (json.charAt(position) == '-') {
            position++;
            if (position >= length || !(json.charAt(position) >= '0' && json.charAt(position) <= '9')) {
                throw new JsonValidationException(formatError(numberStartPos, "减号后缺少数字"));
            }
        }
        
        if (json.charAt(position) == '0') {
            position++;
            if (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                throw new JsonValidationException(formatError(numberStartPos, "数字有前导零（不允许：0123，允许：123）"));
            }
        } else {
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }
        
        if (position < length && json.charAt(position) == '.') {
            position++;
            if (position >= length || !(json.charAt(position) >= '0' && json.charAt(position) <= '9')) {
                throw new JsonValidationException(formatError(numberStartPos, "小数点后缺少数字"));
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
                throw new JsonValidationException(formatError(numberStartPos, "指数部分缺少数字"));
            }
            while (position < length && json.charAt(position) >= '0' && json.charAt(position) <= '9') {
                position++;
            }
        }
    }

    private void expectChar(char expected) throws JsonValidationException {
        if (position >= length) {
            throw new JsonValidationException(formatError(position, "意外到达字符串末尾，期望 '" + expected + "'"));
        }
        if (json.charAt(position) != expected) {
            throw new JsonValidationException(formatErrorWithChar(position, "期望 '" + expected + "'", json.charAt(position)));
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
