import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Nathanael
 * Date: 1/22/13
 * Time: 8:59 PM
 */
public class SyntaxAnalyzer {
    private LexicalAnalyzer lexicalAnalyzer;
    private List<Tuple<String, String, Integer>> openParens = new ArrayList<Tuple<String, String, Integer>>();
    private List<Tuple<String, String, Integer>> openBlocks = new ArrayList<Tuple<String, String, Integer>>();
    String errorList = "";

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    public void evaluate() throws IllegalArgumentException {
        Tuple<String, String, Integer> currentLex;
        Tuple<String, String, Integer> previousLex = null;

        while (lexicalAnalyzer.hasNext()) {
            Tuple<String, String, Integer> temp;
            List<Tuple<String, String, Integer>> tempList = new ArrayList<Tuple<String, String, Integer>>();

            temp = lexicalAnalyzer.getNext();
            while (canAddToList(temp)) {
                tempList.add(temp);
                temp = lexicalAnalyzer.getNext();
            }
            tempList.add(temp);

            if (tempList.isEmpty()) {
                continue;
            }

            for (int i = 0; i < tempList.size(); i++) {
                currentLex = tempList.get(i);

                if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
                    errorList += "Unknown object. Line: " + currentLex.lineNum + "\n";
                }

                Tuple<String, String, Integer> nextLexi =  (i + 1 == tempList.size() ? null : tempList.get(i+1));
                Tuple<String, String, Integer> peekTwoPrevious =  (i - 3 < 0 ? null : tempList.get(i-3));
                Tuple<String, String, Integer> peekPrevious =  (i - 2 < 0 ? null : tempList.get(i-2));


                // validate expressions
                if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                    validateAssignmentOpr(currentLex, previousLex, nextLexi, peekTwoPrevious);
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                    validateMathOpr(currentLex, previousLex, nextLexi);
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                    openParens.add(currentLex);
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                    if (openParens.size() == 0) {
                        errorList +=  "Invalid closing paren on line: " + currentLex.lineNum + "\n";
                    } else {
                        openParens.remove(openParens.size() - 1);
                    }
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
                    validateRelationalOpr(currentLex, previousLex, nextLexi);
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                    openBlocks.add(currentLex);
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                    if (openBlocks.size() == 0) {
                        errorList += "Invalid closing block on line: " + currentLex.lineNum + "\n";
                    } else {
                        openBlocks.remove(openBlocks.size() - 1);
                    }
                } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
                    validateBooleanOpr(currentLex, previousLex, nextLexi);
                }


                // validate statements
                if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.KEYWORD.name())) {
                    if (currentLex.lexi.equals("return")) {
                        if (!tempList.get(0).lexi.equals("return")) {
                            errorList += "return must be the first statement on the line. Line: " + currentLex.lineNum + "\n";
                        }
                        if (!tempList.get(tempList.size()-1).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                            errorList += "return statement must end with a end of line token (;). Line: " + currentLex.lineNum + "\n";
                        }
                        validateReturnStatement(currentLex, previousLex, nextLexi, peekPrevious);
                    }
                }

                previousLex = currentLex;
            }

        }

        if (openParens.size() > 0) {
            String errorMessage = "Incomplete statements (missing closing parens) on the following lines: ";

            for (Tuple<String, String, Integer> item : openParens) {
                errorMessage += item.lineNum + ", ";
            }
            errorList += errorMessage + "\n";
        }

        if (openBlocks.size() > 0) {
            String errorMessage = "Incomplete statements (missing closing blocks) on the following lines: ";

            for (Tuple<String, String, Integer> item : openBlocks) {
                errorMessage += item.lineNum + ", ";
            }
            errorList += errorMessage + "\n";
        }

        if (!errorList.isEmpty()) {
            throw new IllegalArgumentException(errorList);
        }
    }

    private boolean canAddToList(Tuple<String, String, Integer> temp) {
        return  !(temp.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()));
    }

    private void validateBooleanOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
        //TODO: add logic
    }

    private void validateReturnStatement(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex, Tuple<String, String, Integer> peekPrevious) {
        if (!isReturnValueValid(nextLex)) {
            errorList +="Return statement must either be followed by a value or an end of line token (;). Line: " + currentLex.lineNum + "\n";
        }

        if (!isLHSinValidFormat(peekPrevious)) {
            errorList += "Only the return statement can occupy the line (there should not be anything before it). Line: " + currentLex.lineNum +"\n";
        }
    }

    private boolean isReturnValueValid(Tuple<String, String, Integer> nextLex) {
        if (nextLex == null)
            return false;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || nextLex.lexi.equals("true") || nextLex.lexi.equals("false"))
            return true;

        return false;
    }

    private void validateRelationalOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
        if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
            errorList += "There must be a valid type on both sides of the relational operator. Line: " + currentLex.lineNum + "\n";
        }

        if (!isLHSinValidFormatRelationShip(previousLex)) {
            errorList += "Left hand side of the relational operator must be an Identifier, Number, or Character. Line: " + previousLex.lineNum + "\n";
        }

        if (isRHSinValidFormatAssignment(nextLex)) {
            return;
        }
        errorList += "Right hand side of relational operatior must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
    }

    private void validateMathOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
        if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
            errorList += "Mathematical operators require a right hand value. Line: " + currentLex.lineNum + "\n";
        }

        if ((previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || previousLex.lexi.equals(")")) && (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.lexi.equals("("))) {
            return;
        }
        errorList += "Both side of mathematical operation must be either an Identifier or a Number. Line: " + previousLex.lineNum + "\n";
    }

    private void validateAssignmentOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex, Tuple<String, String, Integer> previousTwoLexi) throws IllegalArgumentException {
        if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
            errorList += "There must be a valid type on both sides of the assignment operator. Line: " + currentLex.lineNum + "\n";
        }

        if (!isPreviousLexiValidAssignment(previousLex)) {
            errorList += "Left hand side of assignment operation must be an Identifier. Line: " + previousLex.lineNum + "\n";
        }

        if (!isLHSinValidFormatAssignment(previousTwoLexi, previousLex)) {
            errorList += "There can only be one variable or Identifier on the left side of the assignment operator. Line: " + currentLex.lineNum + "\n";
        }

        if (isRHSinValidFormatAssignment(nextLex)) {
            return;
        }
        errorList += "Right hand side of assignment operation must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
    }

    private boolean isRHSinValidFormatAssignment(Tuple<String, String, Integer> nextLex) {
        if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()))
            return true;
        else if (nextLex.lexi.equals("true") || nextLex.lexi.equals("false") || nextLex.lexi.equals("null"))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
            return true;
        }

        return false;
    }

    private boolean isLHSinValidFormatRelationShip(Tuple<String, String, Integer> nextLex) {
        if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()))
            return true;
        else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
            return true;
        }

        return false;
    }

    private boolean isPreviousLexiValidAssignment(Tuple<String, String, Integer> previousLex) {
        if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
            return true;
        else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
            return true;
        }

        return false;
    }

    private boolean isLHSinValidFormat(Tuple<String, String, Integer> peekPrevious) {
        if (peekPrevious == null)
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()))
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()))
            return true;

        return false;
    }

    private boolean isLHSinValidFormatAssignment(Tuple<String, String, Integer> peekPrevious, Tuple<String, String, Integer> previousLex) {
        if (peekPrevious == null)
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()))
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()))
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()))
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()))
            return true;
        else if (peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()))
            return true;
        else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))
            return true;

        return false;
    }
}