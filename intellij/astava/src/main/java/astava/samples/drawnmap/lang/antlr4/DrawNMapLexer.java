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
		COMMA=1, OPEN_PAR=2, CLOSE_PAR=3, ADD_OP=4, MUL_OP=5, ASSIGN_OP=6, DOT=7, 
		COLON=8, ID=9, NUMBER=10, STRING=11, WS=12;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"','", "'('", "')'", "ADD_OP", "MUL_OP", "'='", "'.'", "':'", "ID", "NUMBER", 
		"STRING", "WS"
	};
	public static final String[] ruleNames = {
		"COMMA", "OPEN_PAR", "CLOSE_PAR", "ADD_OP", "MUL_OP", "ASSIGN_OP", "DOT", 
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
		case 17: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\16\u0080\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3"+
		"\b\3\t\3\t\3\n\3\n\3\13\5\13;\n\13\3\f\3\f\5\f?\n\f\3\f\3\f\3\f\7\fD\n"+
		"\f\f\f\16\fG\13\f\3\r\6\rJ\n\r\r\r\16\rK\3\r\3\r\6\rP\n\r\r\r\16\rQ\5"+
		"\rT\n\r\3\16\3\16\3\16\7\16Y\n\16\f\16\16\16\\\13\16\3\16\3\16\3\17\3"+
		"\17\3\20\3\20\3\20\3\20\5\20f\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\5\21q\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\6\23{\n"+
		"\23\r\23\16\23|\3\23\3\23\2\24\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17"+
		"\t\1\21\n\1\23\2\1\25\2\1\27\13\1\31\f\1\33\r\1\35\2\1\37\2\1!\2\1#\2"+
		"\1%\16\2\3\2\f\4\2--//\4\2,,\61\61\3\2\62;\4\2C\\c|\4\2$$^^\5\2\62;CH"+
		"ch\n\2$$))^^ddhhppttvv\3\2\62\65\3\2\629\5\2\13\f\17\17\"\"\u0087\2\3"+
		"\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2"+
		"\2\17\3\2\2\2\2\21\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2%\3"+
		"\2\2\2\3\'\3\2\2\2\5)\3\2\2\2\7+\3\2\2\2\t-\3\2\2\2\13/\3\2\2\2\r\61\3"+
		"\2\2\2\17\63\3\2\2\2\21\65\3\2\2\2\23\67\3\2\2\2\25:\3\2\2\2\27>\3\2\2"+
		"\2\31I\3\2\2\2\33U\3\2\2\2\35_\3\2\2\2\37e\3\2\2\2!p\3\2\2\2#r\3\2\2\2"+
		"%z\3\2\2\2\'(\7.\2\2(\4\3\2\2\2)*\7*\2\2*\6\3\2\2\2+,\7+\2\2,\b\3\2\2"+
		"\2-.\t\2\2\2.\n\3\2\2\2/\60\t\3\2\2\60\f\3\2\2\2\61\62\7?\2\2\62\16\3"+
		"\2\2\2\63\64\7\60\2\2\64\20\3\2\2\2\65\66\7<\2\2\66\22\3\2\2\2\678\t\4"+
		"\2\28\24\3\2\2\29;\t\5\2\2:9\3\2\2\2;\26\3\2\2\2<?\5\25\13\2=?\7a\2\2"+
		"><\3\2\2\2>=\3\2\2\2?E\3\2\2\2@D\5\25\13\2AD\7a\2\2BD\5\23\n\2C@\3\2\2"+
		"\2CA\3\2\2\2CB\3\2\2\2DG\3\2\2\2EC\3\2\2\2EF\3\2\2\2F\30\3\2\2\2GE\3\2"+
		"\2\2HJ\5\23\n\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3\2\2\2LS\3\2\2\2MO\5"+
		"\17\b\2NP\5\23\n\2ON\3\2\2\2PQ\3\2\2\2QO\3\2\2\2QR\3\2\2\2RT\3\2\2\2S"+
		"M\3\2\2\2ST\3\2\2\2T\32\3\2\2\2UZ\7$\2\2VY\5\37\20\2WY\n\6\2\2XV\3\2\2"+
		"\2XW\3\2\2\2Y\\\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[]\3\2\2\2\\Z\3\2\2\2]^\7$"+
		"\2\2^\34\3\2\2\2_`\t\7\2\2`\36\3\2\2\2ab\7^\2\2bf\t\b\2\2cf\5#\22\2df"+
		"\5!\21\2ea\3\2\2\2ec\3\2\2\2ed\3\2\2\2f \3\2\2\2gh\7^\2\2hi\t\t\2\2ij"+
		"\t\n\2\2jq\t\n\2\2kl\7^\2\2lm\t\n\2\2mq\t\n\2\2no\7^\2\2oq\t\n\2\2pg\3"+
		"\2\2\2pk\3\2\2\2pn\3\2\2\2q\"\3\2\2\2rs\7^\2\2st\7w\2\2tu\5\35\17\2uv"+
		"\5\35\17\2vw\5\35\17\2wx\5\35\17\2x$\3\2\2\2y{\t\13\2\2zy\3\2\2\2{|\3"+
		"\2\2\2|z\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\b\23\2\2\177&\3\2\2\2\17\2:"+
		">CEKQSXZep|";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}