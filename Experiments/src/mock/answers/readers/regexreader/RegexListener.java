// Generated from Regex.g4 by ANTLR 4.7
package mock.answers.readers.regexreader;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RegexParser}.
 */
public interface RegexListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RegexParser#main}.
	 * @param ctx the parse tree
	 */
	void enterMain(RegexParser.MainContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#main}.
	 * @param ctx the parse tree
	 */
	void exitMain(RegexParser.MainContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#ref}.
	 * @param ctx the parse tree
	 */
	void enterRef(RegexParser.RefContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#ref}.
	 * @param ctx the parse tree
	 */
	void exitRef(RegexParser.RefContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#text}.
	 * @param ctx the parse tree
	 */
	void enterText(RegexParser.TextContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#text}.
	 * @param ctx the parse tree
	 */
	void exitText(RegexParser.TextContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#characterClass}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClass(RegexParser.CharacterClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#characterClass}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClass(RegexParser.CharacterClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#characterClassLetters}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClassLetters(RegexParser.CharacterClassLettersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#characterClassLetters}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClassLetters(RegexParser.CharacterClassLettersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#characterClassRange}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClassRange(RegexParser.CharacterClassRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#characterClassRange}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClassRange(RegexParser.CharacterClassRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#specialCharacter}.
	 * @param ctx the parse tree
	 */
	void enterSpecialCharacter(RegexParser.SpecialCharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#specialCharacter}.
	 * @param ctx the parse tree
	 */
	void exitSpecialCharacter(RegexParser.SpecialCharacterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#digit}.
	 * @param ctx the parse tree
	 */
	void enterDigit(RegexParser.DigitContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#digit}.
	 * @param ctx the parse tree
	 */
	void exitDigit(RegexParser.DigitContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#amount}.
	 * @param ctx the parse tree
	 */
	void enterAmount(RegexParser.AmountContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#amount}.
	 * @param ctx the parse tree
	 */
	void exitAmount(RegexParser.AmountContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#atLeastOne}.
	 * @param ctx the parse tree
	 */
	void enterAtLeastOne(RegexParser.AtLeastOneContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#atLeastOne}.
	 * @param ctx the parse tree
	 */
	void exitAtLeastOne(RegexParser.AtLeastOneContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#anyAmount}.
	 * @param ctx the parse tree
	 */
	void enterAnyAmount(RegexParser.AnyAmountContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#anyAmount}.
	 * @param ctx the parse tree
	 */
	void exitAnyAmount(RegexParser.AnyAmountContext ctx);
	/**
	 * Enter a parse tree produced by {@link RegexParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(RegexParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RegexParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(RegexParser.RangeContext ctx);
}