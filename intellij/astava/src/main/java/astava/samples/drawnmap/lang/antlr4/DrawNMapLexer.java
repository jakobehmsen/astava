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
		COMMA=1, OPEN_PAR=2, CLOSE_PAR=3, OPEN_BRA=4, CLOSE_BRA=5, ADD_OP=6, MUL_OP=7, 
		ASSIGN_OP=8, DEFINE_OP=9, PIPE=10, DOT=11, COLON=12, ID=13, NUMBER=14, 
		STRING=15, WS=16;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"','", "'('", "')'", "'{'", "'}'", "ADD_OP", "MUL_OP", "'='", "'=>'", 
		"'|'", "'.'", "':'", "ID", "NUMBER", "STRING", "WS"
	};
	public static final String[] ruleNames = {
		"COMMA", "OPEN_PAR", "CLOSE_PAR", "OPEN_BRA", "CLOSE_BRA", "ADD_OP", "MUL_OP", 
		"ASSIGN_OP", "DEFINE_OP", "PIPE", "DOT", "COLON", "DIGIT", "LETTER", "ID", 
		"NUMBER", "STRING", "HexDigit", "EscapeSequence", "OctalEscape", "UnicodeEscape", 
		"WS"
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
		case 21: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\22\u0091\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\3\2\3\3\3"+
		"\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3"+
		"\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\5\17L\n\17\3\20\3\20\5\20P\n\20\3\20"+
		"\3\20\3\20\7\20U\n\20\f\20\16\20X\13\20\3\21\6\21[\n\21\r\21\16\21\\\3"+
		"\21\3\21\6\21a\n\21\r\21\16\21b\5\21e\n\21\3\22\3\22\3\22\7\22j\n\22\f"+
		"\22\16\22m\13\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\5\24w\n\24\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u0082\n\25\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\27\6\27\u008c\n\27\r\27\16\27\u008d\3\27\3"+
		"\27\2\30\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25"+
		"\f\1\27\r\1\31\16\1\33\2\1\35\2\1\37\17\1!\20\1#\21\1%\2\1\'\2\1)\2\1"+
		"+\2\1-\22\2\3\2\f\4\2--//\4\2,,\61\61\3\2\62;\4\2C\\c|\4\2$$^^\5\2\62"+
		";CHch\n\2$$))^^ddhhppttvv\3\2\62\65\3\2\629\5\2\13\f\17\17\"\"\u0098\2"+
		"\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2"+
		"\31\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2-\3\2\2\2\3/\3\2\2\2\5"+
		"\61\3\2\2\2\7\63\3\2\2\2\t\65\3\2\2\2\13\67\3\2\2\2\r9\3\2\2\2\17;\3\2"+
		"\2\2\21=\3\2\2\2\23?\3\2\2\2\25B\3\2\2\2\27D\3\2\2\2\31F\3\2\2\2\33H\3"+
		"\2\2\2\35K\3\2\2\2\37O\3\2\2\2!Z\3\2\2\2#f\3\2\2\2%p\3\2\2\2\'v\3\2\2"+
		"\2)\u0081\3\2\2\2+\u0083\3\2\2\2-\u008b\3\2\2\2/\60\7.\2\2\60\4\3\2\2"+
		"\2\61\62\7*\2\2\62\6\3\2\2\2\63\64\7+\2\2\64\b\3\2\2\2\65\66\7}\2\2\66"+
		"\n\3\2\2\2\678\7\177\2\28\f\3\2\2\29:\t\2\2\2:\16\3\2\2\2;<\t\3\2\2<\20"+
		"\3\2\2\2=>\7?\2\2>\22\3\2\2\2?@\7?\2\2@A\7@\2\2A\24\3\2\2\2BC\7~\2\2C"+
		"\26\3\2\2\2DE\7\60\2\2E\30\3\2\2\2FG\7<\2\2G\32\3\2\2\2HI\t\4\2\2I\34"+
		"\3\2\2\2JL\t\5\2\2KJ\3\2\2\2L\36\3\2\2\2MP\5\35\17\2NP\7a\2\2OM\3\2\2"+
		"\2ON\3\2\2\2PV\3\2\2\2QU\5\35\17\2RU\7a\2\2SU\5\33\16\2TQ\3\2\2\2TR\3"+
		"\2\2\2TS\3\2\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2W \3\2\2\2XV\3\2\2\2Y[\5"+
		"\33\16\2ZY\3\2\2\2[\\\3\2\2\2\\Z\3\2\2\2\\]\3\2\2\2]d\3\2\2\2^`\5\27\f"+
		"\2_a\5\33\16\2`_\3\2\2\2ab\3\2\2\2b`\3\2\2\2bc\3\2\2\2ce\3\2\2\2d^\3\2"+
		"\2\2de\3\2\2\2e\"\3\2\2\2fk\7$\2\2gj\5\'\24\2hj\n\6\2\2ig\3\2\2\2ih\3"+
		"\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2\2\2ln\3\2\2\2mk\3\2\2\2no\7$\2\2o$\3"+
		"\2\2\2pq\t\7\2\2q&\3\2\2\2rs\7^\2\2sw\t\b\2\2tw\5+\26\2uw\5)\25\2vr\3"+
		"\2\2\2vt\3\2\2\2vu\3\2\2\2w(\3\2\2\2xy\7^\2\2yz\t\t\2\2z{\t\n\2\2{\u0082"+
		"\t\n\2\2|}\7^\2\2}~\t\n\2\2~\u0082\t\n\2\2\177\u0080\7^\2\2\u0080\u0082"+
		"\t\n\2\2\u0081x\3\2\2\2\u0081|\3\2\2\2\u0081\177\3\2\2\2\u0082*\3\2\2"+
		"\2\u0083\u0084\7^\2\2\u0084\u0085\7w\2\2\u0085\u0086\5%\23\2\u0086\u0087"+
		"\5%\23\2\u0087\u0088\5%\23\2\u0088\u0089\5%\23\2\u0089,\3\2\2\2\u008a"+
		"\u008c\t\13\2\2\u008b\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008b\3"+
		"\2\2\2\u008d\u008e\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\b\27\2\2\u0090"+
		".\3\2\2\2\17\2KOTV\\bdikv\u0081\u008d";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}