// Generated from /home/jakob/github/astava/intellij/astava/src/main/java/astava/samples/drawnmap/lang/antlr4/DrawNMap.g4 by ANTLR 4.1
package astava.samples.drawnmap.lang.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DrawNMapLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COMMA=1, OPEN_PAR=2, CLOSE_PAR=3, OPEN_BRA=4, CLOSE_BRA=5, OPEN_SQ=6, 
		CLOSE_SQ=7, ADD_OP=8, MUL_OP=9, ASSIGN_OP=10, DEFINE_OP=11, PIPE=12, DOT=13, 
		COLON=14, ID=15, NUMBER=16, STRING=17, WS=18;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"','", "'('", "')'", "'{'", "'}'", "'['", "']'", "ADD_OP", "MUL_OP", "'='", 
		"'=>'", "'|'", "'.'", "':'", "ID", "NUMBER", "STRING", "WS"
	};
	public static final String[] ruleNames = {
		"COMMA", "OPEN_PAR", "CLOSE_PAR", "OPEN_BRA", "CLOSE_BRA", "OPEN_SQ", 
		"CLOSE_SQ", "ADD_OP", "MUL_OP", "ASSIGN_OP", "DEFINE_OP", "PIPE", "DOT", 
		"COLON", "DIGIT", "LETTER", "ID", "NUMBER", "STRING", "HexDigit", "EscapeSequence", 
		"OctalEscape", "UnicodeEscape", "WS"
	};


	public DrawNMapLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DrawNMap.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 23: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\24\u0099\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t"+
		"\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3"+
		"\21\5\21T\n\21\3\22\3\22\5\22X\n\22\3\22\3\22\3\22\7\22]\n\22\f\22\16"+
		"\22`\13\22\3\23\6\23c\n\23\r\23\16\23d\3\23\3\23\6\23i\n\23\r\23\16\23"+
		"j\5\23m\n\23\3\24\3\24\3\24\7\24r\n\24\f\24\16\24u\13\24\3\24\3\24\3\25"+
		"\3\25\3\26\3\26\3\26\3\26\5\26\177\n\26\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\5\27\u008a\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\6\31\u0094\n\31\r\31\16\31\u0095\3\31\3\31\2\32\3\3\1\5\4\1\7\5\1\t\6"+
		"\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27\r\1\31\16\1\33\17\1\35"+
		"\20\1\37\2\1!\2\1#\21\1%\22\1\'\23\1)\2\1+\2\1-\2\1/\2\1\61\24\2\3\2\f"+
		"\4\2--//\4\2,,\61\61\3\2\62;\4\2C\\c|\4\2$$^^\5\2\62;CHch\n\2$$))^^dd"+
		"hhppttvv\3\2\62\65\3\2\629\5\2\13\f\17\17\"\"\u00a0\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3"+
		"\2\2\2\2\35\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2\61\3\2\2\2\3\63"+
		"\3\2\2\2\5\65\3\2\2\2\7\67\3\2\2\2\t9\3\2\2\2\13;\3\2\2\2\r=\3\2\2\2\17"+
		"?\3\2\2\2\21A\3\2\2\2\23C\3\2\2\2\25E\3\2\2\2\27G\3\2\2\2\31J\3\2\2\2"+
		"\33L\3\2\2\2\35N\3\2\2\2\37P\3\2\2\2!S\3\2\2\2#W\3\2\2\2%b\3\2\2\2\'n"+
		"\3\2\2\2)x\3\2\2\2+~\3\2\2\2-\u0089\3\2\2\2/\u008b\3\2\2\2\61\u0093\3"+
		"\2\2\2\63\64\7.\2\2\64\4\3\2\2\2\65\66\7*\2\2\66\6\3\2\2\2\678\7+\2\2"+
		"8\b\3\2\2\29:\7}\2\2:\n\3\2\2\2;<\7\177\2\2<\f\3\2\2\2=>\7]\2\2>\16\3"+
		"\2\2\2?@\7_\2\2@\20\3\2\2\2AB\t\2\2\2B\22\3\2\2\2CD\t\3\2\2D\24\3\2\2"+
		"\2EF\7?\2\2F\26\3\2\2\2GH\7?\2\2HI\7@\2\2I\30\3\2\2\2JK\7~\2\2K\32\3\2"+
		"\2\2LM\7\60\2\2M\34\3\2\2\2NO\7<\2\2O\36\3\2\2\2PQ\t\4\2\2Q \3\2\2\2R"+
		"T\t\5\2\2SR\3\2\2\2T\"\3\2\2\2UX\5!\21\2VX\7a\2\2WU\3\2\2\2WV\3\2\2\2"+
		"X^\3\2\2\2Y]\5!\21\2Z]\7a\2\2[]\5\37\20\2\\Y\3\2\2\2\\Z\3\2\2\2\\[\3\2"+
		"\2\2]`\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_$\3\2\2\2`^\3\2\2\2ac\5\37\20\2ba"+
		"\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2el\3\2\2\2fh\5\33\16\2gi\5\37\20"+
		"\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2km\3\2\2\2lf\3\2\2\2lm\3\2\2"+
		"\2m&\3\2\2\2ns\7$\2\2or\5+\26\2pr\n\6\2\2qo\3\2\2\2qp\3\2\2\2ru\3\2\2"+
		"\2sq\3\2\2\2st\3\2\2\2tv\3\2\2\2us\3\2\2\2vw\7$\2\2w(\3\2\2\2xy\t\7\2"+
		"\2y*\3\2\2\2z{\7^\2\2{\177\t\b\2\2|\177\5/\30\2}\177\5-\27\2~z\3\2\2\2"+
		"~|\3\2\2\2~}\3\2\2\2\177,\3\2\2\2\u0080\u0081\7^\2\2\u0081\u0082\t\t\2"+
		"\2\u0082\u0083\t\n\2\2\u0083\u008a\t\n\2\2\u0084\u0085\7^\2\2\u0085\u0086"+
		"\t\n\2\2\u0086\u008a\t\n\2\2\u0087\u0088\7^\2\2\u0088\u008a\t\n\2\2\u0089"+
		"\u0080\3\2\2\2\u0089\u0084\3\2\2\2\u0089\u0087\3\2\2\2\u008a.\3\2\2\2"+
		"\u008b\u008c\7^\2\2\u008c\u008d\7w\2\2\u008d\u008e\5)\25\2\u008e\u008f"+
		"\5)\25\2\u008f\u0090\5)\25\2\u0090\u0091\5)\25\2\u0091\60\3\2\2\2\u0092"+
		"\u0094\t\13\2\2\u0093\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0093\3"+
		"\2\2\2\u0095\u0096\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\b\31\2\2\u0098"+
		"\62\3\2\2\2\17\2SW\\^djlqs~\u0089\u0095";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}