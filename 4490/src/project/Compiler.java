package project;

/**
 * Created with IntelliJ IDEA.
 * User: Nathanael
 * Date: 4/27/13
 * Time: 8:40 AM
 */
public class Compiler {
    private LexicalAnalyzer lexicalAnalyzer;
    private String errorList = "";

    public Compiler(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    public void evaluate() {
        // pass one
        while (lexicalAnalyzer.hasNext()) {

            if (expression()) {
                if (lexicalAnalyzer.getToken() instanceof NullTuple)
                    break;

                continue;
            }
            lexicalAnalyzer.nextToken();
        }

        System.out.print(errorList);
    }

    private boolean new_declaration() {
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {

            // check format: "(" [argument_list] ")"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new_declaration operation. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE)) {
                lexicalAnalyzer.nextToken();
                return true;
            }

            if (!argument_list()) {
                errorList += "illegal new_declaration argument_list. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new_declaration operation. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE)) {
                errorList += "illegal new_declaration operation, missing closing paran. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }
            return true;
        }
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {

            // check format: "[" expression "]"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new_declaration operation. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!expression()) {
                errorList += "illegal new_declaration expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new_declaration operation. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END)) {
                errorList += "illegal new_declaration operation, missing closing paran. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }
            return true;
        }

        return false;
    }

    private boolean assignment_expression() {
        if (lexicalAnalyzer.getToken().getType().equals(KeyConst.NEW.getKey())) {

            // check format: "new" type new_declaration
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!type(lexicalAnalyzer.getToken().getType())) {
                errorList += "illegal new operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal new operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!new_declaration()) {
                errorList += "illegal new operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            return true;
        }

        if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ATOI.getKey())) {

            // check format: "atoi" "(" expression ")"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal atoi operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                errorList += "illegal atoi operation, this method is missing an open paren. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal atoi operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!expression()) {
                errorList += "illegal atoi operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal atoi operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                errorList += "illegal atoi operation, this method is missing an close paren. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            return true;
        }

        if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ITOA.getKey())) {

            // check format: "itoa" "(" expression ")"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal itoa operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                errorList += "illegal itoa operation, this method is missing an open paren. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal itoa operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!expression()) {
                errorList += "illegal itoa operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal itoa operation. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                errorList += "illegal itoa operation, this method is missing an close paren. Line " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            return true;
        }

        return expression() || lexicalAnalyzer.getToken().getType().equals(KeyConst.THIS.getKey());
    }

    public boolean expressionz() {
        Tuple token = lexicalAnalyzer.getToken();
        if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || token.getType().equals(LexicalAnalyzer.tokenTypesEnum.LOGICAL_OPR.name()) || token.getType().equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name()) || token.getType().equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "expressionz missing right hand expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                if (!assignment_expression()) {
                    errorList += "illegal assignment operation. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
                    return false;
                }
            } else {
                if (!expression()) {
                    errorList += "illegal operation. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean expression() {
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {

            // check format: "(" expression ")" [expressionz]
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!expression()) {
                errorList += "illegal expression. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
                return false;
            }

            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "illegal expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                errorList += "illegal expression. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                return true;
            }

            expressionz();
            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.NULL.getKey()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {

            // check format: "value" [expressionz]
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                return true;
            }

            expressionz();
            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {

            // check format: identifier [ fn_arr_member ] [ member_refz ] [ expressionz ]
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                return true;
            }
            String errCheck = errorList;

            if (fn_arr_member()) {
                return true;
            }

            if (!errCheck.equals(errorList)) {
                return false;
            }

            if (member_refz()) {
                return true;
            }

            if (!errCheck.equals(errorList)) {
                return false;
            }

            if (expressionz()) {
                return true;
            }

            if (!errCheck.equals(errorList)) {
                return false;
            }

            return true;
        }
        return false;
    }

    private boolean argument_list() {

        // check format: expression { "," expression}
        if (!expression()) {
            errorList += "Invalid argument list. Line. " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
            return false;
        }

        if (lexicalAnalyzer.getToken() instanceof NullTuple) {
            return true;
        }

        while (lexicalAnalyzer.getToken().getLexi().equals(",")) {
            if (!expression()) {
                errorList += "Invalid argument list. Line. " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                break;
            }
        }
        return true;
    }

    private boolean fn_arr_member() {

        // check format: "(" [ argument_list ] ")" | "[" expression "]"
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {

            //check format: "(" [ argument_list ] ")"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "invalid function. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                lexicalAnalyzer.nextToken();
                return true;
            }

            if (!argument_list()) {
                errorList += "invalid argument list in function. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
                return false;
            }


            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "invalid function. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                errorList += "invalid function. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {

            //check format: "[" expression "]"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "array element missing expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!expression()) {
                errorList += "invalid array expression. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                errorList += "array element missing array close. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }

            if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                errorList += "array element missing array close. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean member_refz() {

        // check format: "." identifier [ fn_arr_member ] [ member_refz ]
        if (!lexicalAnalyzer.getToken().getLexi().equals(".")) {
            return false;
        }

        lexicalAnalyzer.nextToken();
        if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
            errorList += "invalid member ref. Line: " + lexicalAnalyzer.previousToken().getLineNum() + "\n";
            return false;
        }

        lexicalAnalyzer.nextToken();
        if (lexicalAnalyzer.getToken() instanceof NullTuple) {
            return true;
        }

        String errCheck = errorList;

        if (fn_arr_member()) {
            return true;
        }

        if (!errCheck.equals(errorList)) {
            return false;
        }

        if (member_refz()) {
            return true;
        }

        if (!errCheck.equals(errorList)) {
            return false;
        }

        return true;
    }

    private boolean type(String itemType) {
        return (itemType.equals(KeyConst.INT.getKey()) || itemType.equals(KeyConst.CHAR.getKey()) || itemType.equals(KeyConst.BOOL.getKey()) || itemType.equals(KeyConst.VOID.getKey()) || itemType.equals(KeyConst.CLASS_NAME.getKey()));
    }
}