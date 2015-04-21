// Generated from /home/jakob/github/astava/intellij/astava/src/main/java/astava/samples/drawnmap/lang/antlr4/DrawNMap.g4 by ANTLR 4.1
package astava.samples.drawnmap.lang.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DrawNMapParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COMMA=1, OPEN_PAR=2, CLOSE_PAR=3, OPEN_BRA=4, CLOSE_BRA=5, OPEN_SQ=6, 
		CLOSE_SQ=7, ADD_OP=8, MUL_OP=9, ASSIGN_OP=10, DEFINE_OP=11, PIPE=12, DOT=13, 
		COLON=14, ID=15, NUMBER=16, STRING=17, WS=18;
	public static final String[] tokenNames = {
		"<INVALID>", "','", "'('", "')'", "'{'", "'}'", "'['", "']'", "ADD_OP", 
		"MUL_OP", "'='", "'=>'", "'|'", "'.'", "':'", "ID", "NUMBER", "STRING", 
		"WS"
	};
	public static final int
		RULE_program = 0, RULE_statement = 1, RULE_assign = 2, RULE_function = 3, 
		RULE_parameters = 4, RULE_expression = 5, RULE_addExpression = 6, RULE_mulExpression = 7, 
		RULE_leafExpression = 8, RULE_functionCall = 9, RULE_property = 10, RULE_propertyAssign = 11, 
		RULE_id = 12, RULE_number = 13, RULE_string = 14, RULE_array = 15, RULE_block = 16, 
		RULE_parameterAndUsage = 17, RULE_embeddedExpression = 18;
	public static final String[] ruleNames = {
		"program", "statement", "assign", "function", "parameters", "expression", 
		"addExpression", "mulExpression", "leafExpression", "functionCall", "property", 
		"propertyAssign", "id", "number", "string", "array", "block", "parameterAndUsage", 
		"embeddedExpression"
	};

	@Override
	public String getGrammarFileName() { return "DrawNMap.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public DrawNMapParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgramContext extends ParserRuleContext {
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(38); statement();
				}
				}
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public AssignContext assign() {
			return getRuleContext(AssignContext.class,0);
		}
		public PropertyAssignContext propertyAssign() {
			return getRuleContext(PropertyAssignContext.class,0);
		}
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(47);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(44); propertyAssign();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(45); assign();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(46); function();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DrawNMapParser.ID, 0); }
		public TerminalNode ASSIGN_OP() { return getToken(DrawNMapParser.ASSIGN_OP, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assign; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitAssign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignContext assign() throws RecognitionException {
		AssignContext _localctx = new AssignContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49); match(ID);
			setState(50); match(ASSIGN_OP);
			setState(51); expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DrawNMapParser.ID, 0); }
		public ParametersContext parameters() {
			return getRuleContext(ParametersContext.class,0);
		}
		public TerminalNode DEFINE_OP() { return getToken(DrawNMapParser.DEFINE_OP, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53); match(ID);
			setState(54); match(DEFINE_OP);
			setState(56);
			_la = _input.LA(1);
			if (_la==PIPE) {
				{
				setState(55); parameters();
				}
			}

			setState(58); expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParametersContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(DrawNMapParser.ID); }
		public List<TerminalNode> PIPE() { return getTokens(DrawNMapParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(DrawNMapParser.PIPE, i);
		}
		public TerminalNode ID(int i) {
			return getToken(DrawNMapParser.ID, i);
		}
		public ParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameters; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParametersContext parameters() throws RecognitionException {
		ParametersContext _localctx = new ParametersContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_parameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60); match(PIPE);
			setState(64);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(61); match(ID);
				}
				}
				setState(66);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(67); match(PIPE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public AddExpressionContext addExpression() {
			return getRuleContext(AddExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); addExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AddExpressionContext extends ParserRuleContext {
		public List<MulExpressionContext> mulExpression() {
			return getRuleContexts(MulExpressionContext.class);
		}
		public List<TerminalNode> ADD_OP() { return getTokens(DrawNMapParser.ADD_OP); }
		public TerminalNode ADD_OP(int i) {
			return getToken(DrawNMapParser.ADD_OP, i);
		}
		public MulExpressionContext mulExpression(int i) {
			return getRuleContext(MulExpressionContext.class,i);
		}
		public AddExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_addExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitAddExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AddExpressionContext addExpression() throws RecognitionException {
		AddExpressionContext _localctx = new AddExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_addExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71); mulExpression();
			setState(76);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ADD_OP) {
				{
				{
				setState(72); match(ADD_OP);
				setState(73); mulExpression();
				}
				}
				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MulExpressionContext extends ParserRuleContext {
		public List<LeafExpressionContext> leafExpression() {
			return getRuleContexts(LeafExpressionContext.class);
		}
		public List<TerminalNode> MUL_OP() { return getTokens(DrawNMapParser.MUL_OP); }
		public LeafExpressionContext leafExpression(int i) {
			return getRuleContext(LeafExpressionContext.class,i);
		}
		public TerminalNode MUL_OP(int i) {
			return getToken(DrawNMapParser.MUL_OP, i);
		}
		public MulExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mulExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitMulExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MulExpressionContext mulExpression() throws RecognitionException {
		MulExpressionContext _localctx = new MulExpressionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_mulExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79); leafExpression();
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL_OP) {
				{
				{
				setState(80); match(MUL_OP);
				setState(81); leafExpression();
				}
				}
				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LeafExpressionContext extends ParserRuleContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public ParameterAndUsageContext parameterAndUsage() {
			return getRuleContext(ParameterAndUsageContext.class,0);
		}
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public EmbeddedExpressionContext embeddedExpression() {
			return getRuleContext(EmbeddedExpressionContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public PropertyContext property() {
			return getRuleContext(PropertyContext.class,0);
		}
		public LeafExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leafExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitLeafExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LeafExpressionContext leafExpression() throws RecognitionException {
		LeafExpressionContext _localctx = new LeafExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_leafExpression);
		try {
			setState(96);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(87); functionCall();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(88); property();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(89); id();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(90); number();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(91); string();
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(92); array();
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(93); block();
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(94); parameterAndUsage();
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(95); embeddedExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionCallContext extends ParserRuleContext {
		public List<TerminalNode> COMMA() { return getTokens(DrawNMapParser.COMMA); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OPEN_PAR() { return getToken(DrawNMapParser.OPEN_PAR, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public TerminalNode CLOSE_PAR() { return getToken(DrawNMapParser.CLOSE_PAR, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(DrawNMapParser.COMMA, i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98); id();
			setState(99); match(OPEN_PAR);
			setState(108);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << OPEN_PAR) | (1L << OPEN_BRA) | (1L << OPEN_SQ) | (1L << COLON) | (1L << ID) | (1L << NUMBER) | (1L << STRING))) != 0)) {
				{
				setState(100); expression();
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(101); match(COMMA);
					setState(102); expression();
					}
					}
					setState(107);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(110); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyContext extends ParserRuleContext {
		public IdContext target;
		public IdContext name;
		public TerminalNode DOT() { return getToken(DrawNMapParser.DOT, 0); }
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public PropertyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_property; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitProperty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyContext property() throws RecognitionException {
		PropertyContext _localctx = new PropertyContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_property);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112); ((PropertyContext)_localctx).target = id();
			setState(113); match(DOT);
			setState(114); ((PropertyContext)_localctx).name = id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PropertyAssignContext extends ParserRuleContext {
		public IdContext target;
		public IdContext name;
		public TerminalNode DOT() { return getToken(DrawNMapParser.DOT, 0); }
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public TerminalNode ASSIGN_OP() { return getToken(DrawNMapParser.ASSIGN_OP, 0); }
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public PropertyAssignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_propertyAssign; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitPropertyAssign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PropertyAssignContext propertyAssign() throws RecognitionException {
		PropertyAssignContext _localctx = new PropertyAssignContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_propertyAssign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116); ((PropertyAssignContext)_localctx).target = id();
			setState(117); match(DOT);
			setState(118); ((PropertyAssignContext)_localctx).name = id();
			setState(119); match(ASSIGN_OP);
			setState(120); expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DrawNMapParser.ID, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122); match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(DrawNMapParser.NUMBER, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124); match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(DrawNMapParser.STRING, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126); match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayContext extends ParserRuleContext {
		public TerminalNode OPEN_SQ() { return getToken(DrawNMapParser.OPEN_SQ, 0); }
		public TerminalNode CLOSE_SQ() { return getToken(DrawNMapParser.CLOSE_SQ, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DrawNMapParser.COMMA); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(DrawNMapParser.COMMA, i);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitArray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_array);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128); match(OPEN_SQ);
			setState(137);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << OPEN_PAR) | (1L << OPEN_BRA) | (1L << OPEN_SQ) | (1L << COLON) | (1L << ID) | (1L << NUMBER) | (1L << STRING))) != 0)) {
				{
				setState(129); expression();
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(130); match(COMMA);
					setState(131); expression();
					}
					}
					setState(136);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(139); match(CLOSE_SQ);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockContext extends ParserRuleContext {
		public ParametersContext parameters() {
			return getRuleContext(ParametersContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode CLOSE_BRA() { return getToken(DrawNMapParser.CLOSE_BRA, 0); }
		public TerminalNode OPEN_BRA() { return getToken(DrawNMapParser.OPEN_BRA, 0); }
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141); match(OPEN_BRA);
			setState(143);
			_la = _input.LA(1);
			if (_la==PIPE) {
				{
				setState(142); parameters();
				}
			}

			setState(145); expression();
			setState(146); match(CLOSE_BRA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParameterAndUsageContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DrawNMapParser.ID, 0); }
		public TerminalNode COLON() { return getToken(DrawNMapParser.COLON, 0); }
		public ParameterAndUsageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterAndUsage; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitParameterAndUsage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterAndUsageContext parameterAndUsage() throws RecognitionException {
		ParameterAndUsageContext _localctx = new ParameterAndUsageContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_parameterAndUsage);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148); match(COLON);
			setState(149); match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EmbeddedExpressionContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(DrawNMapParser.OPEN_PAR, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(DrawNMapParser.CLOSE_PAR, 0); }
		public EmbeddedExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_embeddedExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DrawNMapVisitor ) return ((DrawNMapVisitor<? extends T>)visitor).visitEmbeddedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EmbeddedExpressionContext embeddedExpression() throws RecognitionException {
		EmbeddedExpressionContext _localctx = new EmbeddedExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_embeddedExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151); match(OPEN_PAR);
			setState(152); expression();
			setState(153); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\24\u009e\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\3\2\7\2*\n\2\f\2\16\2-\13\2\3\3\3\3\3\3\5\3\62\n"+
		"\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\5\5;\n\5\3\5\3\5\3\6\3\6\7\6A\n\6\f\6\16"+
		"\6D\13\6\3\6\3\6\3\7\3\7\3\b\3\b\3\b\7\bM\n\b\f\b\16\bP\13\b\3\t\3\t\3"+
		"\t\7\tU\n\t\f\t\16\tX\13\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\nc\n"+
		"\n\3\13\3\13\3\13\3\13\3\13\7\13j\n\13\f\13\16\13m\13\13\5\13o\n\13\3"+
		"\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3"+
		"\20\3\20\3\21\3\21\3\21\3\21\7\21\u0087\n\21\f\21\16\21\u008a\13\21\5"+
		"\21\u008c\n\21\3\21\3\21\3\22\3\22\5\22\u0092\n\22\3\22\3\22\3\22\3\23"+
		"\3\23\3\23\3\24\3\24\3\24\3\24\3\24\2\25\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"$&\2\2\u009e\2+\3\2\2\2\4\61\3\2\2\2\6\63\3\2\2\2\b\67\3\2"+
		"\2\2\n>\3\2\2\2\fG\3\2\2\2\16I\3\2\2\2\20Q\3\2\2\2\22b\3\2\2\2\24d\3\2"+
		"\2\2\26r\3\2\2\2\30v\3\2\2\2\32|\3\2\2\2\34~\3\2\2\2\36\u0080\3\2\2\2"+
		" \u0082\3\2\2\2\"\u008f\3\2\2\2$\u0096\3\2\2\2&\u0099\3\2\2\2(*\5\4\3"+
		"\2)(\3\2\2\2*-\3\2\2\2+)\3\2\2\2+,\3\2\2\2,\3\3\2\2\2-+\3\2\2\2.\62\5"+
		"\30\r\2/\62\5\6\4\2\60\62\5\b\5\2\61.\3\2\2\2\61/\3\2\2\2\61\60\3\2\2"+
		"\2\62\5\3\2\2\2\63\64\7\21\2\2\64\65\7\f\2\2\65\66\5\f\7\2\66\7\3\2\2"+
		"\2\678\7\21\2\28:\7\r\2\29;\5\n\6\2:9\3\2\2\2:;\3\2\2\2;<\3\2\2\2<=\5"+
		"\f\7\2=\t\3\2\2\2>B\7\16\2\2?A\7\21\2\2@?\3\2\2\2AD\3\2\2\2B@\3\2\2\2"+
		"BC\3\2\2\2CE\3\2\2\2DB\3\2\2\2EF\7\16\2\2F\13\3\2\2\2GH\5\16\b\2H\r\3"+
		"\2\2\2IN\5\20\t\2JK\7\n\2\2KM\5\20\t\2LJ\3\2\2\2MP\3\2\2\2NL\3\2\2\2N"+
		"O\3\2\2\2O\17\3\2\2\2PN\3\2\2\2QV\5\22\n\2RS\7\13\2\2SU\5\22\n\2TR\3\2"+
		"\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2W\21\3\2\2\2XV\3\2\2\2Yc\5\24\13\2Z"+
		"c\5\26\f\2[c\5\32\16\2\\c\5\34\17\2]c\5\36\20\2^c\5 \21\2_c\5\"\22\2`"+
		"c\5$\23\2ac\5&\24\2bY\3\2\2\2bZ\3\2\2\2b[\3\2\2\2b\\\3\2\2\2b]\3\2\2\2"+
		"b^\3\2\2\2b_\3\2\2\2b`\3\2\2\2ba\3\2\2\2c\23\3\2\2\2de\5\32\16\2en\7\4"+
		"\2\2fk\5\f\7\2gh\7\3\2\2hj\5\f\7\2ig\3\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2"+
		"\2\2lo\3\2\2\2mk\3\2\2\2nf\3\2\2\2no\3\2\2\2op\3\2\2\2pq\7\5\2\2q\25\3"+
		"\2\2\2rs\5\32\16\2st\7\17\2\2tu\5\32\16\2u\27\3\2\2\2vw\5\32\16\2wx\7"+
		"\17\2\2xy\5\32\16\2yz\7\f\2\2z{\5\f\7\2{\31\3\2\2\2|}\7\21\2\2}\33\3\2"+
		"\2\2~\177\7\22\2\2\177\35\3\2\2\2\u0080\u0081\7\23\2\2\u0081\37\3\2\2"+
		"\2\u0082\u008b\7\b\2\2\u0083\u0088\5\f\7\2\u0084\u0085\7\3\2\2\u0085\u0087"+
		"\5\f\7\2\u0086\u0084\3\2\2\2\u0087\u008a\3\2\2\2\u0088\u0086\3\2\2\2\u0088"+
		"\u0089\3\2\2\2\u0089\u008c\3\2\2\2\u008a\u0088\3\2\2\2\u008b\u0083\3\2"+
		"\2\2\u008b\u008c\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008e\7\t\2\2\u008e"+
		"!\3\2\2\2\u008f\u0091\7\6\2\2\u0090\u0092\5\n\6\2\u0091\u0090\3\2\2\2"+
		"\u0091\u0092\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0094\5\f\7\2\u0094\u0095"+
		"\7\7\2\2\u0095#\3\2\2\2\u0096\u0097\7\20\2\2\u0097\u0098\7\21\2\2\u0098"+
		"%\3\2\2\2\u0099\u009a\7\4\2\2\u009a\u009b\5\f\7\2\u009b\u009c\7\5\2\2"+
		"\u009c\'\3\2\2\2\16+\61:BNVbkn\u0088\u008b\u0091";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}