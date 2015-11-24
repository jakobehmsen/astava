grammar Java;

classFile: classDefinition;
script: element*;
element: classDefinition | implementsInterface | fieldDefinition | methodDefinition | statement | expression | annotation;
classDefinition: modifiers KW_CLASS name=typeQualifier implementsInterface OPEN_BRA classMember* CLOSE_BRA;
implementsInterface: KW_IMPLEMENTS typeQualifier (COMMA typeQualifier)*;
classMember: fieldDefinition | methodDefinition;
fieldDefinition: modifiers type=typeQualifier name=ID (OP_ASSIGN value=expression)? SEMI_COLON;
methodDefinition: 
    modifiers returnType=typeQualifier name=ID parameters 
    (SEMI_COLON | OPEN_BRA statement* CLOSE_BRA);
parameters: OPEN_PAR (parameter (COMMA parameter)*)? CLOSE_PAR;
parameter: type=typeQualifier name=ID;
typeQualifier: ID (DOT ID)* | capture;
modifiers: accessModifier? KW_ABSTRACT? KW_STATIC?;
accessModifier: KW_PUBLIC | KW_PRIVATE | KW_PROTECTED;
statement: nonDelimitedStatement | (anyStatement=capture | delimitedStatement SEMI_COLON);

nonDelimitedStatement: ifElseStatement | tryCatchStatement | methodBodyStatement;
ifElseStatement: 
    KW_IF OPEN_PAR condition=expression CLOSE_PAR 
    ifTrueBlock=singleOrMultiStatement
    (KW_ELSE ifFalseBlock=singleOrMultiStatement)?;
tryCatchStatement:
    tryBlock catchBlock* finallyBlock?;
tryBlock: KW_TRY OPEN_BRA statement* CLOSE_BRA;
catchBlock: KW_CATCH OPEN_PAR type=typeQualifier name=ID CLOSE_PAR OPEN_BRA statement* CLOSE_BRA;
finallyBlock: KW_FINALLY OPEN_BRA statement* CLOSE_BRA;
methodBodyStatement: ELLIPSIS;

singleOrMultiStatement: OPEN_BRA statement* CLOSE_BRA | statement;

delimitedStatement: 
    returnStatement | variableDeclaration | throwStatement | expression;
returnStatement: KW_RETURN expression?;
variableDeclaration: type=typeQualifier name=ID (OP_ASSIGN value=expression)?;

// Expression precedence following http://www.cs.bilkent.edu.tr/~guvenir/courses/CS101/op_precedence.html
expression: assignment | expression4 | anyExpression=capture;
assignment: name=ID OP_ASSIGN value=expression;

expression4: expressionLogicalAnd | expression8;
expressionLogicalAnd: first=expression8 (AMPERSAND expression)*;

expression8: relationalExpression | expression9;
relationalExpression: first=expression9 (OP_EQUALS expression)*;

expression9: instanceOfExpression | expression13;
instanceOfExpression: expression13 KW_INSTANCE_OF typeQualifier;

expression13: typeCastExpression | leafExpression;
typeCastExpression: OPEN_PAR typeQualifier CLOSE_PAR expression;

// TODO: Add support for boolean literals true and false
leafExpression: 
    (
        invocation | classLiteral | ambigousName | intLiteral | stringLiteral | nullLiteral | thisLiteral |
        trueLiteral | falseLiteral | newInstance | methodBodyExpression
    )
    chainElement*;
invocation: identifier arguments;
chainElement: DOT (fieldAssignment | fieldAccess | invocation);
fieldAssignment: identifier OP_ASSIGN value=expression;
fieldAccess: identifier;
identifier: ID | capture;
classLiteral: typeQualifier DOT KW_CLASS;
ambigousName: ID ({_input.LT(2).getType() != OPEN_PAR && _input.LT(2).getType() != OP_ASSIGN}? DOT ID)*;
intLiteral: INT;
stringLiteral: STRING;
nullLiteral: KW_NULL;
thisLiteral: KW_THIS;
trueLiteral: KW_TRUE;
falseLiteral: KW_FALSE;
newInstance: KW_NEW name=typeQualifier arguments;
arguments: OPEN_PAR (expression (COMMA expression)*)? CLOSE_PAR;
methodBodyExpression: ELLIPSIS;
throwStatement: KW_THROW expression;

annotation: AT typeQualifier
    (OPEN_PAR
        ((valueArgument=expression | annotationArgument)? (COMMA annotationArgument)*)
    CLOSE_PAR)?;
annotationArgument: name=ID OP_ASSIGN value=expression;

classPredicate:
    classPredicateElement*;
classPredicateElement:
    classPredicateAccessAnnotation | /*classPredicateAccessModifier | */classPredicateName | classPredicateExtends |
    classPredicateImplements | classPredicateMember;
classPredicateAccessAnnotation:
    annotation;
/*classPredicateAccessModifier:
    accessModifier;*/
classPredicateName:
    KW_CLASS name=typeQualifier;
classPredicateExtends:
    (KW_EXTENDS superClassName=typeQualifier);
classPredicateImplements:
    KW_IMPLEMENTS classPredicateInterface (COMMA classPredicateInterface)*;
classPredicateInterface:
    typeQualifier;
classPredicateMember:
    classPredicateField | classPredicateMethod;
classPredicateField:
    modifiers type=typeQualifier? name=ID? SEMI_COLON;
classPredicateMethod:
    modifiers returnType=typeQualifier? name=ID? classPredicateMethodParameters;
classPredicateMethodParameters:
    OPEN_PAR ((typeQualifier (COMMA typeQualifier)*) | anyParams=ELLIPSIS)? CLOSE_PAR;

methodPredicate:
    methodPredicateElement*;
methodPredicateElement:
    methodPredicateAnnotation
    | methodPredicateAccessModifiers
    | methodPredicateAccessTypeAndName
    | methodPredicateParameters
    ;
methodPredicateAnnotation:
    annotation;
methodPredicateAccessModifiers:
    modifier+;
methodPredicateAccessTypeAndName:
    (returnType=typeQualifier | ELLIPSIS) name=ID?;
methodPredicateParameters:
    OPEN_PAR (typeQualifier (COMMA typeQualifier)*)? CLOSE_PAR;
modifier: KW_PRIVATE | KW_PUBLIC | KW_PROTECTED | KW_STATIC | KW_ABSTRACT;

methodModification:
    methodModificationElement*;
methodModificationElement:
    methodModificationAnnotation | methodModificationBody;
methodModificationAnnotation:
    annotation;
methodModificationBody:
    statement+;

capture: QUESTION_MARK ID?;

statementsOrExpression: statement* | expression;

QUESTION_MARK: '?';
AMPERSAND: '&&';
AT: '@';
OP_ASSIGN: '=';
OP_EQUALS: '==';
SEMI_COLON: ';';
DOT: '.';
ELLIPSIS: '...';
COMMA: ',';
OPEN_PAR: '(';
CLOSE_PAR: ')';
OPEN_BRA: '{';
CLOSE_BRA: '}';
KW_NEW: 'new';
KW_RETURN: 'return';
KW_PUBLIC: 'public';
KW_PRIVATE: 'private';
KW_PROTECTED: 'protected';
KW_ABSTRACT: 'abstract';
KW_STATIC: 'static';
KW_CLASS: 'class';
KW_NULL: 'null';
KW_EXTENDS: 'extends';
KW_IMPLEMENTS: 'implements';
KW_INSTANCE_OF: 'instanceof';
KW_IF: 'if';
KW_ELSE: 'else';
KW_THIS: 'this';
KW_TRUE: 'true';
KW_FALSE: 'false';
KW_THROW: 'throw';
KW_TRY: 'try';
KW_CATCH: 'catch';
KW_FINALLY: 'finally';
fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ID: (LETTER | '_') (LETTER | '_' | DIGIT)*;

INT: DIGIT+ (DOT DIGIT+)?;
STRING: '"' (EscapeSequence | ~[\\"])* '"';
fragment HexDigit: [0-9a-fA-F];
fragment EscapeSequence: '\\' [btnfr"'\\] | UnicodeEscape | OctalEscape;
fragment OctalEscape: '\\' [0-3] [0-7] [0-7] | '\\' [0-7] [0-7] | '\\' [0-7];
fragment UnicodeEscape: '\\' 'u' HexDigit HexDigit HexDigit HexDigit;

WS: [ \n\t\r]+ -> skip;
SINGLE_LINE_COMMENT: '//' ~('\r' | '\n')* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;