grammar DrawNMap;

program: statement*;
statement: assign;
assign: ID ASSIGN_OP expression;
expression: addExpression;
addExpression: mulExpression (ADD_OP mulExpression)*;
mulExpression: leafExpression (MUL_OP leafExpression)*;
leafExpression: 
    functionCall | property | id | number | string | embeddedExpression;
functionCall: id OPEN_PAR (expression (COMMA expression)*)? CLOSE_PAR;
property: target=id DOT name=id;
id: ID;
number: NUMBER;
string: STRING;
embeddedExpression: OPEN_PAR expression CLOSE_PAR;

COMMA: ',';
OPEN_PAR: '(';
CLOSE_PAR: ')';
ADD_OP: '+' | '-';
MUL_OP: '*' | '/';
ASSIGN_OP: '=';
DOT: '.';
COLON: ':';
fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ID: (LETTER | '_') (LETTER | '_' | DIGIT)*;
NUMBER: DIGIT+ (DOT DIGIT+)?;
STRING: '"' (EscapeSequence | ~[\\"])* '"';
fragment HexDigit: [0-9a-fA-F];
fragment EscapeSequence: '\\' [btnfr"'\\] | UnicodeEscape | OctalEscape;
fragment OctalEscape: '\\' [0-3] [0-7] [0-7] | '\\' [0-7] [0-7] | '\\' [0-7];
fragment UnicodeEscape: '\\' 'u' HexDigit HexDigit HexDigit HexDigit;

WS: [ \n\t\r]+ -> skip;
