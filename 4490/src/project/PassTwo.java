package project;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: Nathanael
 * Date: 8/31/13
 * Time: 10:30 AM
 */
public class PassTwo {
    private String scope = "g.";
    private int variableId = 100;
    private Stack<SAR> SAS = new Stack<SAR>();
    private Stack<Opr_SAR> OS = new Stack<Opr_SAR>();

    private LinkedHashMap<String, Symbol> symbolTable;
    private LexicalAnalyzer lexicalAnalyzer;
    private String errorList = "";

    public PassTwo(LinkedHashMap<String, Symbol> symbolTable, LexicalAnalyzer lexicalAnalyzer) {
        this.symbolTable = symbolTable;
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    public void evaluate() {
        // pass two
        if (!compilation_unit()) {
            System.out.print(errorList);
            System.exit(0);
        }
        System.out.println("Semantic Analysis Successful!");
    }

    private boolean new_declaration() {
        // check format: "(" [argument_list] ")" |  "[" expression "]"
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            // check format: "(" [argument_list] ")"
            lexicalAnalyzer.nextToken();
            BALPush(new BAL_SAR());

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                EALPush();
                if (!newObjPush()) {
                    return false;
                }

                lexicalAnalyzer.nextToken();
                return true;
            }

            String errorCheck = errorList;

            if (!argument_list()) {
                if (!errorCheck.equals(errorList)) {
                    return false;
                }
            }

            EALPush();
            if (!newObjPush()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            return true;
        } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
            // check format: "[" expression "]"
            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }
            lexicalAnalyzer.nextToken();

            if (!expression()) {
                return false;
            }

            arrayClose();
            if (!newArrayPush()) {
                return false;
            }
            lexicalAnalyzer.nextToken();
            return true;
        }

        return false;
    }

    public boolean assignment_expression() {
        if (lexicalAnalyzer.getToken().getType().equals(KeyConst.NEW.getKey())) {
            // check format: "new" type new_declaration
            lexicalAnalyzer.nextToken();
            typePush(new Type_SAR(lexicalAnalyzer.getToken(), scope));
            if (!typeExists()) {
                return false;
            }

            lexicalAnalyzer.nextToken();

            return new_declaration();

        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ATOI.getKey())) {
            // check format: "atoi" "(" expression ")"
            lexicalAnalyzer.nextToken();

            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (!expression()) {
                return false;
            }

            if (!closingParen()) {
                return false;
            }

            if (!atoiCheck()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            return true;
        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ITOA.getKey())) {
            // check format: "itoa" "(" expression ")"
            lexicalAnalyzer.nextToken();
            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }

            lexicalAnalyzer.nextToken();

            if (!expression()) {
                return false;
            }

            if (!closingParen()) {
                return false;
            }

            if (!itoaCheck()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            return true;
        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.THIS.getKey())) {
            lexicalAnalyzer.nextToken();
            return true;
        } else {
            return expression();
        }
    }

    public boolean expressionz() {
        String errorCheck = errorList;
        if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
            return false;
        }

        if (!errorCheck.equals(errorList)) {
            return false;
        }


        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
            lexicalAnalyzer.nextToken();
            if (!assignment_expression()) {
                return false;
            }
        } else {
            lexicalAnalyzer.nextToken();
            if (!expression()) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean expression() {
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            // check format: "(" expression ")" [expressionz]
            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }
            lexicalAnalyzer.nextToken();

            if (!expression()) {
                return false;
            }

            if (!closingParen()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                return expressionz();
            }

            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.NULL.getKey()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
            // check format: "value" [expressionz]
            String type;
            if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey())) {
                type = KeyConst.BOOL.name();
            } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                type = KeyConst.INT.name();
            } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                type = KeyConst.CHAR.name();
            } else {
                type = "null";
            }

            literalPush(new Literal_SAR(lexicalAnalyzer.getToken(), type));
            lexicalAnalyzer.nextToken();

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                return expressionz();
            }

            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
            // check format: identifier [ fn_arr_member ] [ member_refz ] [ expressionz ]
            identifierPush(new Identifier_SAR(lexicalAnalyzer.getToken(), scope));

            lexicalAnalyzer.nextToken();
            String errCheck = errorList;

            fn_arr_member();
            if (!errCheck.equals(errorList)) {
                return false;
            }

            if (!identifierExist()) {
                return false;
            }

            member_refz();
            if (!errCheck.equals(errorList)) {
                return false;
            }

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                return expressionz();
            }

            return true;
        }
        return false;
    }

    private boolean argument_list() {
        // check format: expression { "," expression}
        if (!expression()) {
            return false;
        }

        while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getName().equals(",")) {
            if (!COMMA()) {
                return false;
            }

            lexicalAnalyzer.nextToken();

            if (!expression()) {
                return false;
            }
        }
        return COMMA();
    }

    private boolean fn_arr_member() {
        // check format: "(" [ argument_list ] ")" | "[" expression "]"
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            //check format: "(" [ argument_list ] ")"
            BALPush(new BAL_SAR());
            lexicalAnalyzer.nextToken();

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                EALPush();
                functionPush();
                lexicalAnalyzer.nextToken();
                return true;
            }

            if (!argument_list()) {
                return false;
            }

            EALPush();
            functionPush();
            lexicalAnalyzer.nextToken();
            return true;

        } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
            //check format: "[" expression "]"
            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }
            lexicalAnalyzer.nextToken();

            if (!expression()) {
                return false;
            }
            //check for array end
            lexicalAnalyzer.nextToken();
            arrayClose();
            return ArrayRefPush();
        }
        return false;
    }

    private boolean member_refz() {
        // check format: "." identifier [ fn_arr_member ] [ member_refz ]
        if (!lexicalAnalyzer.getToken().getName().equals(".")) {
            return false;
        }

        lexicalAnalyzer.nextToken();
        identifierPush(new Identifier_SAR(lexicalAnalyzer.getToken(), scope));
        lexicalAnalyzer.nextToken();

        String errCheck = errorList;

        fn_arr_member();
        if (!errCheck.equals(errorList)) {
            return false;
        }

        if (!memberRefExists()) {
            return false;
        }

        member_refz();
        return errCheck.equals(errorList);
    }

    public boolean statement() {
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
            // check format: "{" {statement} "}"
            String errorCheck = errorList;
            lexicalAnalyzer.nextToken();

            while (statement()) {
                lexicalAnalyzer.nextToken();
            }

            return errorCheck.equals(errorList);

        } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.IF.getKey())) {
            // check format: "if" "(" expression ")" statement [ "else" statement ]
            lexicalAnalyzer.nextToken();
            operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()));

            lexicalAnalyzer.nextToken();
            if (!expression()) {
                return false;
            }

            if (!closingParen()) {
                return false;
            }

            if (!ifCheck()) {
                return false;
            }

            lexicalAnalyzer.nextToken();

            if (!statement()) {
                return false;
            }

            if (!(lexicalAnalyzer.peek() instanceof NullTuple) && lexicalAnalyzer.peek().getName().equals(KeyConst.ELSE.getKey())) {
                lexicalAnalyzer.nextToken();
                lexicalAnalyzer.nextToken();

                return statement();
            }

            return true;

        } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.WHILE.getKey())) {
            // check format: "while" "(" expression ")" statement
            lexicalAnalyzer.nextToken();
            operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()));


            lexicalAnalyzer.nextToken();
            if (!expression()) {
                return false;
            }

            if (!closingParen()) {
                return false;
            }

            if (!whileCheck()) {
                return false;
            }

            lexicalAnalyzer.nextToken();

            return statement();

        } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.RETURN.getKey())) {
            // check format: "return" [ expression ] ";"
            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                return EOE() && returnCheck();
            }

            return expression() && EOE() && returnCheck();

        } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.COUT.getKey())) {
            // check format: "cout" "<<" expression ";"
            lexicalAnalyzer.nextToken();
            lexicalAnalyzer.nextToken();

            return expression() && EOE() && coutCheck();

        } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.CIN.getKey())) {
            // check format: "cin" ">>" expression ";"
            lexicalAnalyzer.nextToken();
            lexicalAnalyzer.nextToken();

            return expression() && EOE() && cinCheck();

        } else {
            // check format: expression ";"
            return expression() && EOE();
        }
    }

    public boolean parameter() {
        // check format: type identifier ["[" "]"]
        typePush(new Type_SAR(lexicalAnalyzer.getToken(), scope));
        if (!typeExists()) {
            return false;
        }
        popSAS();

        lexicalAnalyzer.nextToken();
        lexicalAnalyzer.nextToken();
        if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
            return true;
        }
        lexicalAnalyzer.nextToken();
        return true;
    }

    public boolean parameter_list() {
        // check format: parameter { "," parameter}
        if (!parameter()) {
            return false;
        }

        while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getName().equals(",")) {
            lexicalAnalyzer.nextToken();

            if (!parameter()) {
                return false;
            }
        }
        return true;
    }

    public boolean variable_declaration() {
        // check format: type identifier ["[" "]"] ["=" assignment_expression ] ";"
        boolean variableHasBeenAdded = false;

        typePush(new Type_SAR(lexicalAnalyzer.getToken(), scope));
        if (!typeExists()) {
            return false;
        }
        popSAS();

        String type = lexicalAnalyzer.getToken().getName();

        lexicalAnalyzer.nextToken();
        Tuple variable = lexicalAnalyzer.getToken();
        lexicalAnalyzer.nextToken();
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
            lexicalAnalyzer.nextToken();
            variablePush(new Variable_SAR(variable, scope, "V" + variableId++, "@:" + type));
            variableHasBeenAdded = true;
            lexicalAnalyzer.nextToken();
        }

        if (!variableHasBeenAdded) {
            variablePush(new Variable_SAR(variable, scope, "V" + variableId++, type));
        }

        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
            if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (!assignment_expression()) {
                return false;
            }
        } else {
            popSAS();
        }

        if (!EOE()) {
            return false;
        }

        lexicalAnalyzer.nextToken();
        return true;
    }

    public boolean method_body() {
        // check format: "{" {variable_declaration} {statement} "}"
        lexicalAnalyzer.nextToken();

        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
            lexicalAnalyzer.nextToken();
            return true;
        }

        String errorCheck = errorList;

        if (type(lexicalAnalyzer.getToken().getType())) {
            while (variable_declaration()) {
                if (!type(lexicalAnalyzer.getToken().getType())) break;
            }

            if (!errorCheck.equals(errorList)) {
                return false;
            }
        }

        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
            lexicalAnalyzer.nextToken();
            return true;
        }

        while (statement()) {
            lexicalAnalyzer.nextToken();
        }

        if (!errorCheck.equals(errorList)) {
            return false;
        }

        lexicalAnalyzer.nextToken();
        return true;
    }

    public boolean constructor_declaration() {
        // check format: class_name "(" [parameter_list] ")" method_body
        if (!constructorDeclaration()) {
            return false;
        }

        String constructorName = lexicalAnalyzer.getToken().getName();

        lexicalAnalyzer.nextToken();
        lexicalAnalyzer.nextToken();

        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
            lexicalAnalyzer.nextToken();
            incrementScope(constructorName, false);

            if (!method_body()) {
                return false;
            }

            decrementScope();
            return true;
        }

        incrementScope(constructorName, false);

        if (!parameter_list()) {
            return false;
        }

        lexicalAnalyzer.nextToken();

        if (!method_body()) {
            return false;
        }

        decrementScope();
        return true;
    }

    public boolean field_declaration(String type, Tuple value) {
        if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            // check format: "(" [parameter_list] ")" method_body

            lexicalAnalyzer.nextToken();
            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                incrementScope(value.getName(), false);
                lexicalAnalyzer.nextToken();
                if (!method_body()) {
                    return false;
                }

                decrementScope();
                return true;
            }

            incrementScope(value.getName(), false);

            if (!parameter_list()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            if (!method_body()) {
                return false;
            }

            decrementScope();
            return true;

        } else {
            // check format: ["[" "]"] ["=" assignment_expression ] ";"
            boolean variableHasBeenAdded = false;
            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                variablePush(new Variable_SAR(value, scope, "V" + variableId++, type));
                lexicalAnalyzer.nextToken();
                return true;
            }

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {

                lexicalAnalyzer.nextToken();
                lexicalAnalyzer.nextToken();

                variablePush(new Variable_SAR(value, scope, "V" + variableId++, "@:" + type));
                if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                    lexicalAnalyzer.nextToken();
                    return true;
                }

                variableHasBeenAdded = true;
            }

            if (!variableHasBeenAdded) {
                variablePush(new Variable_SAR(value, scope, "V" + variableId++, type));
            }

            if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                if (!operatorPush(new Opr_SAR(lexicalAnalyzer.getToken()))) {
                    return false;
                }

                lexicalAnalyzer.nextToken();
                if (!assignment_expression()) {
                    return false;
                }
            }

            if (!EOE()) {
                return false;
            }

            lexicalAnalyzer.nextToken();
            return true;
        }
    }

    public boolean class_member_declaration() {
        if (lexicalAnalyzer.getToken().getType().equals(KeyConst.MODIFIER.getKey())) {
            // check format: modifier type identifier field_declaration
            lexicalAnalyzer.nextToken();
            typePush(new Type_SAR(lexicalAnalyzer.getToken(), scope));
            if (!typeExists()) {
                return false;
            }
            popSAS();

            String type = lexicalAnalyzer.getToken().getName();

            lexicalAnalyzer.nextToken();

            Tuple name = lexicalAnalyzer.getToken();

            lexicalAnalyzer.nextToken();

            return field_declaration(type, name);

        } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
            // check format: constructor_declaration
            return constructor_declaration();
        }

        return false;
    }

    public boolean class_declaration() {
        // check format: "class" class_name "{" {class_member_declaration} "}"
        // class declaration should be in the symbol table at this point
        lexicalAnalyzer.nextToken();
        incrementScope(lexicalAnalyzer.getToken().getName(), true);

        // check for block begin
        lexicalAnalyzer.nextToken();

        lexicalAnalyzer.nextToken();

        String errorCheck = errorList;
        //noinspection StatementWithEmptyBody
        while (class_member_declaration()) {
        }

        if (!errorCheck.equals(errorList)) {
            return false;
        }

        // class is over now decrement scope
        decrementScope();
        lexicalAnalyzer.nextToken();
        return true;
    }

    public boolean compilation_unit() {
        // check format: {class_declaration} "void" "main" "(" ")" method_body
        if (!lexicalAnalyzer.getToken().getName().equals(KeyConst.VOID.getKey())) {
            if (!class_declaration()) {
                return false;
            }

            while (lexicalAnalyzer.getToken().getName().equals(KeyConst.CLASS.getKey())) {
                if (!class_declaration()) {
                    return false;
                }
            }
        }

        // look for main method
        lexicalAnalyzer.nextToken();

        // look for open parenthesis
        lexicalAnalyzer.nextToken();

        // look for closing parenthesis
        lexicalAnalyzer.nextToken();

        lexicalAnalyzer.nextToken();
        incrementScope("main", true);

        // at this point we have declared classes and "void main()"
        if (!method_body()) {
            return false;
        }

        decrementScope();
        return true;
    }

    private boolean type(String itemType) {
        return (itemType.equals(KeyConst.INT.getKey()) || itemType.equals(KeyConst.CHAR.getKey()) || itemType.equals(KeyConst.BOOL.getKey()) || itemType.equals(KeyConst.VOID.getKey()) || itemType.equals(KeyConst.CLASS_NAME.getKey()));
    }

    private boolean isLogicalConnectiveExpression(String itemType) {
        return itemType.equals(LexicalAnalyzer.tokenTypesEnum.LOGICAL_OPR.name());
    }

    private boolean isBooleanExpression(String itemType) {
        return itemType.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name());
    }

    private boolean isMathematicalExpression(String itemType) {
        return itemType.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name());
    }

    private void decrementScope() {
        int scopeDepth = 0;
        for (char c : scope.toCharArray()) {
            if (c == '.') scopeDepth++;
        }

        if (scopeDepth > 1) {
            scope = scope.substring(0, scope.lastIndexOf("."));
        } else {
            scope = scope.substring(0, scope.lastIndexOf(".") + 1);
        }
    }

    private void incrementScope(String name, boolean isClass) {
        if (isClass) {
            scope += name;
        } else {
            scope += "." + name;
        }
    }

    public SAR popSAS() {
        if (!SAS.isEmpty()) {
            return SAS.pop();
        }
        return null;
    }

    public boolean atoiCheck() {
        SAR sar = SAS.pop();

        if (sar.getType().equalsIgnoreCase(KeyConst.INT.name())) {
            SAS.push(new Variable_SAR(sar.getLexi(), sar.getScope(), "V" + variableId++, KeyConst.INT.name()));
            return true;
        }

        if (sar.getType().startsWith("@:")) {
            String type = sar.getType().substring(sar.getType().indexOf(":") + 1, sar.getType().length());
            if (type.equalsIgnoreCase(KeyConst.INT.name())) {
                SAS.push(new Variable_SAR(sar.getLexi(), sar.getScope(), "V" + variableId++, KeyConst.INT.name()));
                return true;
            }
        }

        errorList += "atoi expression must be able to evaluate to an integer type. Line: " + sar.getLexi().getLineNum() + "\n";
        return false;
    }

    public boolean itoaCheck() {
        SAR sar = SAS.pop();

        if (sar.getType().equalsIgnoreCase(KeyConst.INT.name())) {
            SAS.push(new Variable_SAR(sar.getLexi(), sar.getScope(), "V" + variableId++, "@:" + KeyConst.CHAR.name()));
            return true;
        }

        errorList += "itoa expression must be of integer type. Line: " + sar.getLexi().getLineNum() + "\n";
        return false;
    }

    public boolean coutCheck() {
        SAR sar = popSAS();

        if (sar.getType().equalsIgnoreCase(KeyConst.INT.name()) || sar.getType().equalsIgnoreCase(KeyConst.CHAR.name())) {
            return true;
        }

        errorList += "variable must of type 'int' or 'char' to be used in a cout statement. Line: " + sar.getLexi().getLineNum() + "\n";
        return false;
    }

    public boolean cinCheck() {
        SAR sar = popSAS();

        if (sar instanceof Literal_SAR) {
            errorList += "cannot assignment values to a literal value. Line: " + sar.getLexi().getLineNum() + "\n";
            return false;
        }

        if (sar.getType().equalsIgnoreCase(KeyConst.INT.name()) || sar.getType().equalsIgnoreCase(KeyConst.CHAR.name())) {
            return true;
        }

        errorList += "variable must of type 'int' or 'char' to be used in a cin statement. Line: " + sar.getLexi().getLineNum() + "\n";
        return false;
    }

    public boolean returnCheck() {
        String returnType;
        int lineNum;
        SAR sar;

        if (SAS.isEmpty()) {
            returnType = KeyConst.VOID.name();
            lineNum = lexicalAnalyzer.peekPreviousToken().getLineNum();
        } else {
            sar = SAS.pop();
            returnType = sar.getType();
            lineNum = sar.getLexi().getLineNum();
        }

        String methodName = scope.substring(scope.lastIndexOf(".") + 1, scope.length());

        String searchScope = scope.substring(0, scope.lastIndexOf("."));

        for (String key : symbolTable.keySet()) {
            Symbol temp = symbolTable.get(key);

            if (!temp.getScope().equals(searchScope)) {
                continue;
            }

            if (temp.getValue().equals(methodName) && (temp.getData() instanceof MethodData)) {
                if (!returnType.equalsIgnoreCase(temp.getData().getType())) {
                    errorList += "Invalid return statement. method requires return type of '" + temp.getData().getType() + "'. Found type '" + returnType + "'. Line: " + lineNum + "\n";
                    return false;
                }
                return true;
            }
        }

        errorList += "invalid return statement. Line: " + lineNum + "\n";
        return false;
    }

    public boolean ifCheck() {
        SAR sar = popSAS();
        if (!sar.getType().equalsIgnoreCase(KeyConst.BOOL.name())) {
            errorList += "the expression in the 'if' statement must evaluate to a type bool. Line: " + sar.getLexi().getLineNum() + "\n";
            return false;
        }
        return true;
    }

    public boolean whileCheck() {
        SAR sar = popSAS();
        if (!sar.getType().equalsIgnoreCase(KeyConst.BOOL.name())) {
            errorList += "the expression in the 'while' statement must evaluate to a type bool. Line: " + sar.getLexi().getLineNum() + "\n";
            return false;
        }
        return true;
    }

    public boolean EOE() {
        while (!OS.isEmpty()) {
            if (!handleOperation()) {
                return false;
            }
        }

        if (!SAS.isEmpty() && SAS.peek() instanceof Function_SAR) {
            SAS.pop();
        }

        return true;
    }

    public boolean closingParen() {
        while (!OS.peek().getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            if (!handleOperation()) {
                return false;
            }
        }

        // pop opening parenthesis
        OS.pop();

        return true;
    }

    public boolean arrayClose() {
        while (!OS.peek().getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
            if (!handleOperation()) {
                return false;
            }
        }

        // pop array opening
        OS.pop();

        return true;
    }

    public boolean ArrayRefPush() {
        Identifier_SAR value = (Identifier_SAR) SAS.pop();

        if (!value.getType().equalsIgnoreCase(KeyConst.INT.name())) {
            errorList += "array indexer must be of type int. type '" + value.getType() + "' was found. Line: " + value.getLexi().getLineNum() + "\n";
            return false;
        }

        Identifier_SAR array = (Identifier_SAR) SAS.pop();

        SAS.push(new Array_SAR(array.getScope(), array.getLexi(), array.getType(), array, value));
        return identifierExist();
    }

    public boolean COMMA() {
        while (!(SAS.peek() instanceof BAL_SAR) && !OS.isEmpty()) {
            if (!handleOperation()) {
                return false;
            }
        }

        return true;
    }

    public void identifierPush(Identifier_SAR identifier) {
        SAS.push(identifier);
    }

    public boolean identifierExist() {
        SAR id_sar = SAS.pop();

        String type = isInSymbolTable(id_sar);
        if (type != null) {
            id_sar.setType(type);
            SAS.push(id_sar);
            return true;
        }

        errorList += "identifier does not exist in the symbol table. name: '" + id_sar.getLexi().getName() + "' type: '" + id_sar.getType() + "'. Line: " + id_sar.getLexi().getLineNum() + "\n";
        return false;
    }

    public boolean memberRefExists() {
        SAR rhs = SAS.pop();
        SAR lhs = SAS.pop();

        if (lhs instanceof Literal_SAR) {
            errorList += "left hand side of a call operation cannot be a literal value. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (rhs instanceof Literal_SAR) {
            errorList += "right hand side of a call operation cannot be a literal value. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (SARType(lhs.getType())) {
            errorList += "variable: '" + lhs.getLexi().getName() + "' is of type '" + lhs.getType() + "' which is un-assignable. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        String lhsItemScope = "g." + lhs.getType();

        for (String key : symbolTable.keySet()) {
            Symbol temp = symbolTable.get(key);

            if (!temp.getScope().equals(lhsItemScope)) {
                continue;
            }

            if (temp.getValue().equals(rhs.getLexi().getName())) {
                if (temp.getData().getAccessMod().toUpperCase().equals(KeyConst.PRIVATE.name())) {
                    errorList += "'" + rhs.getLexi().getName() + "' must be a public variable in order to be accessed outside of its class. Line: " + rhs.getLexi().getLineNum() + "\n";
                    return false;
                }

                if (temp.getData() instanceof MethodData) {
                    ((Function_SAR) rhs).getFunction().setType(temp.getData().getType());

                    if (((Function_SAR) rhs).getArguments().getArguments().size() > ((MethodData) temp.getData()).getParameters().size()) {
                        errorList += "there are too many parameters for called method. Line: " + rhs.getLexi().getLineNum() + "\n";
                        return false;
                    } else if (((Function_SAR) rhs).getArguments().getArguments().size() < ((MethodData) temp.getData()).getParameters().size()) {
                        errorList += "there are too few parameters for called method. Line: " + rhs.getLexi().getLineNum() + "\n";
                        return false;
                    }

                    List<SAR> args = ((Function_SAR) rhs).getArguments().getArguments();
                    int index = ((Function_SAR) rhs).getArguments().getArguments().size() - 1;

                    for (String type : ((MethodData) temp.getData()).getParameters()) {
                        if (!args.get(index).getType().equals(type)) {
                            errorList += "invalid argument type. expected: " + type + " but was: " + args.get(index).getType() + " Line: " + rhs.getLexi().getLineNum() + "\n";
                            return false;
                        }
                        index--;
                    }
                }

                SAS.push(new Ref_SAR(lhs.getScope(), new Tuple("T" + variableId++, temp.getData().getType(), rhs.getLexi().getLineNum()), temp.getData().getType()));
                return true;
            }
        }

        errorList += "'" + rhs.getLexi().getName() + "' does not exists in '" + lhs.getLexi().getName() + "'. Line: " + rhs.getLexi().getLineNum() + "\n";
        return false;
    }

    public void literalPush(Literal_SAR literal) {
        SAS.push(literal);
    }

    public void typePush(Type_SAR type) {
        SAS.push(type);
    }

    public boolean typeExists() {
        Type_SAR itemType = (Type_SAR) SAS.peek();

        if (SARType(itemType.getLexi().getName())) {
            return true;
        } else if (itemType.getLexi().getType().equals(KeyConst.CLASS_NAME.getKey())) {
            return isClassInSymbolTable(itemType);
        }

        errorList += "type: '" + itemType.getType() + "' does not exists. Line: " + itemType.getLexi().getLineNum() + "\n";
        return false;
    }

    private boolean isClassInSymbolTable(Type_SAR itemType) {
        for (String key : symbolTable.keySet()) {
            Symbol temp = symbolTable.get(key);

            if (!temp.getScope().equals("g.")) {
                continue;
            }

            if (temp.getValue().equals(itemType.getName())) {
                return true;
            }
        }

        errorList += "type: '" + itemType.getName() + "' does not exists. Line: " + itemType.getLexi().getLineNum() + "\n";
        return false;
    }

    private String isInSymbolTable(SAR sar) {
        String searchScope = sar.getScope();
        while (!searchScope.equals("g.")) {
            for (String key : symbolTable.keySet()) {
                Symbol temp = symbolTable.get(key);

                if (!temp.getScope().equals(searchScope)) {
                    continue;
                }

                if (sar.getScope().contains(temp.getScope()) && temp.getValue().equals(sar.getLexi().getName())) {
                    return temp.getData().getType();
                }
            }
            searchScope = decrementScope(searchScope);
        }

        errorList += "symbol: '" + sar.getLexi().getName() + "' does not exists. Line: " + sar.getLexi().getLineNum() + "\n";
        return null;
    }

    public void variablePush(Variable_SAR variable) {
        SAS.push(variable);
    }

    public void BALPush(BAL_SAR identifier) {
        SAS.push(identifier);
    }

    public void EALPush() {
        List<SAR> arguments = new ArrayList<SAR>();
        while (!(SAS.peek() instanceof BAL_SAR)) {
            arguments.add(SAS.pop());
        }

        // remove BAL_SAR
        SAS.pop();

        SAS.push(new EAL_SAR(arguments));
    }

    public void functionPush() {
        EAL_SAR arguments = (EAL_SAR) SAS.pop();
        Identifier_SAR function = (Identifier_SAR) SAS.pop();
        SAS.push(new Function_SAR(function.getScope(), function.getLexi(), function.getType(), function, arguments));
    }

    public boolean operatorPush(Opr_SAR opr) {
        opr.setPrecedence(getOperatorPrecedence(opr.getLexi().getName()));

        if (OS.isEmpty()) {
            OS.push(opr);
            return true;
        }

        int lastOprPrecedence;
        if (OS.peek().getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name()) || OS.peek().getLexi().getName().equals(".") || OS.peek().getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            lastOprPrecedence = -1;
        } else {
            lastOprPrecedence = OS.peek().getPrecedence();
        }

        if (lastOprPrecedence <= opr.getPrecedence()) {
            OS.push(opr);
        } else {
            if (!handleOperation()) {
                return false;
            }
            OS.push(opr);
        }
        return true;
    }

    public boolean newArrayPush() {
        SAR element = SAS.pop();

        if (!element.getType().toUpperCase().equals("INT")) {
            errorList += "values in array declaration must be integers. Line: " + element.getLexi().getLineNum() + "\n";
            return false;
        }

        Type_SAR type = (Type_SAR) SAS.pop();

        if (type.getName().equals(KeyConst.VOID.name())) {
            errorList += "cannot create an array of void objects. Line: " + type.getLexi().getLineNum() + "\n";
            return false;
        }

        List<SAR> sarList = new ArrayList<SAR>();
        sarList.add(element);
        EAL_SAR eal_sar = new EAL_SAR(sarList);

        SAS.push(new New_SAR(element.getScope(), new Tuple(), "@:" + type.getName(), type, eal_sar));
        return true;
    }

    public boolean newObjPush() {
        EAL_SAR parameters = (EAL_SAR) SAS.pop();
        Type_SAR type = (Type_SAR) SAS.pop();

        String constructorScope = "g." + type.getName();

        for (String key : symbolTable.keySet()) {
            Symbol temp = symbolTable.get(key);

            if (!temp.getScope().equals(constructorScope)) {
                continue;
            }

            if (temp.getValue().equals(type.getName())) {
                if (temp.getData() instanceof MethodData) {
                    type.setType(temp.getData().getType());

                    if (parameters.getArguments().size() > ((MethodData) temp.getData()).getParameters().size()) {
                        errorList += "there are too many parameters for called constructor. Line: " + type.getLexi().getLineNum() + "\n";
                        return false;
                    } else if (parameters.getArguments().size() < ((MethodData) temp.getData()).getParameters().size()) {
                        errorList += "there are too few parameters for called constructor. Line: " + type.getLexi().getLineNum() + "\n";
                        return false;
                    }

                    List<SAR> args = parameters.getArguments();
                    int index = parameters.getArguments().size() - 1;

                    for (String params : ((MethodData) temp.getData()).getParameters()) {
                        if (!args.get(index).getType().equals(params)) {
                            errorList += "invalid argument type. expected: " + params + " but was: " + args.get(index).getType() + " Line: " + type.getLexi().getLineNum() + "\n";
                            return false;
                        }
                        index--;
                    }
                }
                SAS.push(new New_SAR(type.getScope(), type.getLexi(), type.getType(), type, parameters));
                return true;
            }
        }

        errorList += "Invalid Constructor. Line: " + type.getLexi().getLineNum() + "\n";
        return false;
    }

    private boolean handleOperation() {
        Opr_SAR topOpr = OS.pop();

        if (topOpr.getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
            return mathematicalOperation();
        } else if (topOpr.getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
            return assignment();
        } else if (topOpr.getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
            return booleanOperation();
        } else if (topOpr.getLexi().getType().equals(LexicalAnalyzer.tokenTypesEnum.LOGICAL_OPR.name())) {
            return logicalOperation();
        }

        return false;
    }

    private boolean logicalOperation() {
        SAR rhs = SAS.pop();
        SAR lhs = SAS.pop();

        if (!lhs.getType().equalsIgnoreCase(KeyConst.BOOL.name())) {
            errorList += "left hand side of logical operation must a bool. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (!rhs.getType().equalsIgnoreCase(KeyConst.BOOL.name())) {
            errorList += "right hand side of logical operation must a bool. Line: " + rhs.getLexi().getLineNum() + "\n";
            return false;
        }

        String key = "T" + variableId;
        symbolTable.put(key, new Symbol(lhs.getScope(), key, key, Compiler.VARIABLE, new VariableData(KeyConst.BOOL.name(), KeyConst.PRIVATE.name()), 1));
        SAS.push(new Variable_SAR(new Tuple(key, KeyConst.BOOL.name(), rhs.getLexi().getLineNum()), rhs.getScope(), key, KeyConst.BOOL.name()));
        variableId++;
        return true;
    }

    private boolean booleanOperation() {
        SAR rhs = SAS.pop();
        SAR lhs = SAS.pop();

        if (!lhs.getType().equalsIgnoreCase("int")) {
            errorList += "left hand side of boolean operation must an int. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (!rhs.getType().equalsIgnoreCase("int")) {
            errorList += "right hand side of boolean operation must an int. Line: " + rhs.getLexi().getLineNum() + "\n";
            return false;
        }

        String key = "T" + variableId;
        symbolTable.put(key, new Symbol(lhs.getScope(), key, key, Compiler.VARIABLE, new VariableData(KeyConst.BOOL.name(), KeyConst.PRIVATE.name()), 1));
        SAS.push(new Variable_SAR(new Tuple(key, KeyConst.BOOL.name(), rhs.getLexi().getLineNum()), rhs.getScope(), key, KeyConst.BOOL.name()));
        variableId++;
        return true;
    }

    private boolean constructorDeclaration() {
        if (!lexicalAnalyzer.getToken().getName().equals(scope.substring(scope.lastIndexOf(".") + 1, scope.length()))) {
            errorList += "Invalid constructor name. the name must be the same as the class that it is in. Line: " + lexicalAnalyzer.getToken().getLineNum() + "\n";
            return false;
        }
        return true;
    }

    private boolean assignment() {
        SAR rhs = SAS.pop();
        SAR lhs = SAS.pop();

        if (lhs instanceof Literal_SAR) {
            errorList += "left hand side is not a valid assignable type. Line. " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (!SARType(lhs.getType()) && rhs.getType().equalsIgnoreCase(KeyConst.NULL.getKey())) {
            return true;
        }

        if (!lhs.getType().equalsIgnoreCase(rhs.getType())) {
            errorList += "left and right hand sides of assignment operation are incompatible types. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        return true;
    }

    private boolean mathematicalOperation() {
        SAR rhs = SAS.pop();
        SAR lhs = SAS.pop();

        if (!lhs.getType().equalsIgnoreCase("int")) {
            errorList += "left hand side of mathematical operator must an int. Line: " + lhs.getLexi().getLineNum() + "\n";
            return false;
        }

        if (!rhs.getType().equalsIgnoreCase("int")) {
            errorList += "right hand side of mathematical operator must an int. Line: " + rhs.getLexi().getLineNum() + "\n";
            return false;
        }

        String key = "T" + variableId;
        symbolTable.put(key, new Symbol(lhs.getScope(), key, key, Compiler.VARIABLE, new VariableData(KeyConst.INT.name(), KeyConst.PRIVATE.name()), 1));
        Tuple tempTuple = new Tuple(key, KeyConst.INT.name(), lhs.getLexi().getLineNum());
        SAR temp = new Identifier_SAR(lhs.getScope(), tempTuple, KeyConst.INT.name());
        variableId++;
        SAS.push(temp);

        return true;
    }

    private int getOperatorPrecedence(String oprSymbol) {
        if (oprSymbol.equals(".") || oprSymbol.equals("(") || oprSymbol.equals("[")) {
            return 15;
        } else if (oprSymbol.equals(")") || oprSymbol.equals("]")) {
            return 0;
        } else if (oprSymbol.equals("*") || oprSymbol.equals("/") || oprSymbol.equals("%")) {
            return 13;
        } else if (oprSymbol.equals("+") || oprSymbol.equals("-")) {
            return 11;
        } else if (oprSymbol.equals("<") || oprSymbol.equals(">") || oprSymbol.equals("<=") || oprSymbol.equals(">=")) {
            return 9;
        } else if (oprSymbol.equals("==") || oprSymbol.equals("!=")) {
            return 7;
        } else if (oprSymbol.equals("&&")) {
            return 5;
        } else if (oprSymbol.equals("||")) {
            return 3;
        } else if (oprSymbol.equals("=")) {
            return 1;
        }
        return 0;
    }

    private boolean SARType(String itemType) {
        return (itemType.equals(KeyConst.INT.getKey()) || itemType.equals(KeyConst.CHAR.getKey()) || itemType.equals(KeyConst.BOOL.getKey()) || itemType.equals(KeyConst.VOID.getKey()));
    }

    private String decrementScope(String scope) {
        int scopeDepth = 0;
        for (char c : scope.toCharArray()) {
            if (c == '.') scopeDepth++;
        }

        if (scopeDepth > 1) {
            return scope.substring(0, scope.lastIndexOf("."));
        } else {
            return scope.substring(0, scope.lastIndexOf(".") + 1);
        }
    }
}
