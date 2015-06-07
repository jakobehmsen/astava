grammar Java;

classFile: classDefinition;
script: element*;
element: classDefinition | fieldDefinition | methodDefinition | statement | expression;
classDefinition: modifiers KW_CLASS name=typeQualifier OPEN_BRA classMember* CLOSE_BRA;
classMember: fieldDefinition | methodDefinition;
fieldDefinition: modifiers type=typeQualifier name=ID (OP_ASSIGN value=expression)? SEMI_COLON;
methodDefinition: 
    modifiers returnType=typeQualifier? name=ID parameters 
    (SEMI_COLON | OPEN_BRA statement* CLOSE_BRA);
parameters: OPEN_PAR (parameter (COMMA parameter)*)? CLOSE_PAR;
parameter: type=typeQualifier name=ID;
typeQualifier: ID (DOT ID)*;
modifiers: accessModifier? KW_ABSTRACT? KW_STATIC?;
accessModifier: KW_PUBLIC | KW_PRIVATE | KW_PROTECTED;
statement: delimitedStatement SEMI_COLON;
delimitedStatement: 
    returnStatement | variableDeclaration | expression;
returnStatement: KW_RETURN expression;
variableDeclaration: type=typeQualifier name=ID (OP_ASSIGN value=expression)?;
expression: assignment | leafExpression;
assignment: name=ID OP_ASSIGN value=expression;
leafExpression: 
    (invocation | ambigousName | intLiteral | stringLiteral | nullLiteral | newInstance)
    chainElement*;
invocation: ID arguments;
chainElement: DOT (fieldAssignment | fieldAccess | invocation);
fieldAssignment: ID OP_ASSIGN value=expression;
fieldAccess: ID;
ambigousName: ID ({_input.LT(2).getType() != OPEN_PAR && _input.LT(2).getType() != OP_ASSIGN}? DOT ID)*;
intLiteral: INT;
stringLiteral: STRING;
nullLiteral: KW_NULL;
newInstance: KW_NEW name=typeQualifier arguments;
arguments: OPEN_PAR (expression (COMMA expression)*)? CLOSE_PAR;

OP_ASSIGN: '=';
SEMI_COLON: ';';
DOT: '.';
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