// Generated from Regex.g4 by ANTLR 4.7
package mock.answers.readers.regexreader;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RegexParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		NOT=10, ANY=11, NUMBER=12, WS=13;
	public static final int
		RULE_main = 0, RULE_ref = 1, RULE_text = 2, RULE_characterClass = 3, RULE_characterClassLetters = 4, 
		RULE_characterClassRange = 5, RULE_specialCharacter = 6, RULE_digit = 7, 
		RULE_amount = 8, RULE_atLeastOne = 9, RULE_anyAmount = 10, RULE_range = 11;
	public static final String[] ruleNames = {
		"main", "ref", "text", "characterClass", "characterClassLetters", "characterClassRange", 
		"specialCharacter", "digit", "amount", "atLeastOne", "anyAmount", "range"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'['", "']'", "'-'", "'\\d'", "'+'", "'*'", "'{'", "','", "'}'", 
		"'^'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, "NOT", "ANY", 
		"NUMBER", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Regex.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RegexParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class MainContext extends ParserRuleContext {
		public RefContext ref() {
			return getRuleContext(RefContext.class,0);
		}
		public MainContext main() {
			return getRuleContext(MainContext.class,0);
		}
		public TerminalNode EOF() { return getToken(RegexParser.EOF, 0); }
		public MainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_main; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterMain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitMain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitMain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MainContext main() throws RecognitionException {
		MainContext _localctx = new MainContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_main);
		try {
			setState(28);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__3:
			case ANY:
				enterOuterAlt(_localctx, 1);
				{
				setState(24);
				ref();
				setState(25);
				main();
				}
				break;
			case EOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(27);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class RefContext extends ParserRuleContext {
		public CharacterClassContext characterClass() {
			return getRuleContext(CharacterClassContext.class,0);
		}
		public AmountContext amount() {
			return getRuleContext(AmountContext.class,0);
		}
		public SpecialCharacterContext specialCharacter() {
			return getRuleContext(SpecialCharacterContext.class,0);
		}
		public TextContext text() {
			return getRuleContext(TextContext.class,0);
		}
		public RefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RefContext ref() throws RecognitionException {
		RefContext _localctx = new RefContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ref);
		try {
			setState(36);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(30);
				characterClass();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(31);
				characterClass();
				setState(32);
				amount();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(34);
				specialCharacter();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(35);
				text();
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

	public static class TextContext extends ParserRuleContext {
		public TerminalNode ANY() { return getToken(RegexParser.ANY, 0); }
		public TextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_text; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterText(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitText(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitText(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_text);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			match(ANY);
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

	public static class CharacterClassContext extends ParserRuleContext {
		public CharacterClassRangeContext characterClassRange() {
			return getRuleContext(CharacterClassRangeContext.class,0);
		}
		public CharacterClassLettersContext characterClassLetters() {
			return getRuleContext(CharacterClassLettersContext.class,0);
		}
		public CharacterClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterCharacterClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitCharacterClass(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitCharacterClass(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterClassContext characterClass() throws RecognitionException {
		CharacterClassContext _localctx = new CharacterClassContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_characterClass);
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(40);
				match(T__0);
				setState(41);
				characterClassRange();
				setState(42);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(44);
				match(T__0);
				setState(45);
				characterClassLetters();
				setState(46);
				match(T__1);
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

	public static class CharacterClassLettersContext extends ParserRuleContext {
		public CharacterClassLettersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterClassLetters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterCharacterClassLetters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitCharacterClassLetters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitCharacterClassLetters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterClassLettersContext characterClassLetters() throws RecognitionException {
		CharacterClassLettersContext _localctx = new CharacterClassLettersContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_characterClassLetters);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(51); 
			_errHandler.sync(this);
			_alt = 1+1;
			do {
				switch (_alt) {
				case 1+1:
					{
					{
					setState(50);
					matchWildcard();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(53); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class CharacterClassRangeContext extends ParserRuleContext {
		public CharacterClassRangeContext characterClassRange() {
			return getRuleContext(CharacterClassRangeContext.class,0);
		}
		public CharacterClassRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterClassRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterCharacterClassRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitCharacterClassRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitCharacterClassRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterClassRangeContext characterClassRange() throws RecognitionException {
		CharacterClassRangeContext _localctx = new CharacterClassRangeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_characterClassRange);
		try {
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(55);
				matchWildcard();
				setState(56);
				match(T__2);
				setState(57);
				matchWildcard();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(58);
				matchWildcard();
				setState(59);
				match(T__2);
				setState(60);
				matchWildcard();
				setState(61);
				characterClassRange();
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

	public static class SpecialCharacterContext extends ParserRuleContext {
		public DigitContext digit() {
			return getRuleContext(DigitContext.class,0);
		}
		public SpecialCharacterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialCharacter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterSpecialCharacter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitSpecialCharacter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitSpecialCharacter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialCharacterContext specialCharacter() throws RecognitionException {
		SpecialCharacterContext _localctx = new SpecialCharacterContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_specialCharacter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			digit();
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

	public static class DigitContext extends ParserRuleContext {
		public AmountContext amount() {
			return getRuleContext(AmountContext.class,0);
		}
		public DigitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_digit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterDigit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitDigit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitDigit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DigitContext digit() throws RecognitionException {
		DigitContext _localctx = new DigitContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_digit);
		try {
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(66);
				match(T__3);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(67);
				match(T__3);
				setState(68);
				amount();
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

	public static class AmountContext extends ParserRuleContext {
		public AtLeastOneContext atLeastOne() {
			return getRuleContext(AtLeastOneContext.class,0);
		}
		public AnyAmountContext anyAmount() {
			return getRuleContext(AnyAmountContext.class,0);
		}
		public RangeContext range() {
			return getRuleContext(RangeContext.class,0);
		}
		public AmountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_amount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterAmount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitAmount(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitAmount(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AmountContext amount() throws RecognitionException {
		AmountContext _localctx = new AmountContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_amount);
		try {
			setState(74);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				atLeastOne();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 2);
				{
				setState(72);
				anyAmount();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 3);
				{
				setState(73);
				range();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class AtLeastOneContext extends ParserRuleContext {
		public AtLeastOneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atLeastOne; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterAtLeastOne(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitAtLeastOne(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitAtLeastOne(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtLeastOneContext atLeastOne() throws RecognitionException {
		AtLeastOneContext _localctx = new AtLeastOneContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_atLeastOne);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(T__4);
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

	public static class AnyAmountContext extends ParserRuleContext {
		public AnyAmountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyAmount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterAnyAmount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitAnyAmount(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitAnyAmount(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyAmountContext anyAmount() throws RecognitionException {
		AnyAmountContext _localctx = new AnyAmountContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_anyAmount);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			match(T__5);
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

	public static class RangeContext extends ParserRuleContext {
		public List<TerminalNode> NUMBER() { return getTokens(RegexParser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(RegexParser.NUMBER, i);
		}
		public RangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).enterRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RegexListener ) ((RegexListener)listener).exitRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RegexVisitor ) return ((RegexVisitor<? extends T>)visitor).visitRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeContext range() throws RecognitionException {
		RangeContext _localctx = new RangeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			match(T__6);
			setState(81);
			match(NUMBER);
			setState(82);
			match(T__7);
			setState(83);
			match(NUMBER);
			setState(84);
			match(T__8);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17Y\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\3\2\3\2\3\2\3\2\5\2\37\n\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3"+
		"\'\n\3\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\63\n\5\3\6\6\6\66\n"+
		"\6\r\6\16\6\67\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7A\n\7\3\b\3\b\3\t\3\t\3"+
		"\t\5\tH\n\t\3\n\3\n\3\n\5\nM\n\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\67\2\16\2\4\6\b\n\f\16\20\22\24\26\30\2\2\2V\2\36\3\2\2\2\4"+
		"&\3\2\2\2\6(\3\2\2\2\b\62\3\2\2\2\n\65\3\2\2\2\f@\3\2\2\2\16B\3\2\2\2"+
		"\20G\3\2\2\2\22L\3\2\2\2\24N\3\2\2\2\26P\3\2\2\2\30R\3\2\2\2\32\33\5\4"+
		"\3\2\33\34\5\2\2\2\34\37\3\2\2\2\35\37\7\2\2\3\36\32\3\2\2\2\36\35\3\2"+
		"\2\2\37\3\3\2\2\2 \'\5\b\5\2!\"\5\b\5\2\"#\5\22\n\2#\'\3\2\2\2$\'\5\16"+
		"\b\2%\'\5\6\4\2& \3\2\2\2&!\3\2\2\2&$\3\2\2\2&%\3\2\2\2\'\5\3\2\2\2()"+
		"\7\r\2\2)\7\3\2\2\2*+\7\3\2\2+,\5\f\7\2,-\7\4\2\2-\63\3\2\2\2./\7\3\2"+
		"\2/\60\5\n\6\2\60\61\7\4\2\2\61\63\3\2\2\2\62*\3\2\2\2\62.\3\2\2\2\63"+
		"\t\3\2\2\2\64\66\13\2\2\2\65\64\3\2\2\2\66\67\3\2\2\2\678\3\2\2\2\67\65"+
		"\3\2\2\28\13\3\2\2\29:\13\2\2\2:;\7\5\2\2;A\13\2\2\2<=\13\2\2\2=>\7\5"+
		"\2\2>?\13\2\2\2?A\5\f\7\2@9\3\2\2\2@<\3\2\2\2A\r\3\2\2\2BC\5\20\t\2C\17"+
		"\3\2\2\2DH\7\6\2\2EF\7\6\2\2FH\5\22\n\2GD\3\2\2\2GE\3\2\2\2H\21\3\2\2"+
		"\2IM\5\24\13\2JM\5\26\f\2KM\5\30\r\2LI\3\2\2\2LJ\3\2\2\2LK\3\2\2\2M\23"+
		"\3\2\2\2NO\7\7\2\2O\25\3\2\2\2PQ\7\b\2\2Q\27\3\2\2\2RS\7\t\2\2ST\7\16"+
		"\2\2TU\7\n\2\2UV\7\16\2\2VW\7\13\2\2W\31\3\2\2\2\t\36&\62\67@GL";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}