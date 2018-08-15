// Generated from Regex.g4 by ANTLR 4.7
package mock.answers.readers.regexreader;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RegexParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RegexVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RegexParser#main}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMain(RegexParser.MainContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#ref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRef(RegexParser.RefContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(RegexParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#characterClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClass(RegexParser.CharacterClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#characterClassLetters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClassLetters(RegexParser.CharacterClassLettersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#characterClassRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClassRange(RegexParser.CharacterClassRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#specialCharacter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialCharacter(RegexParser.SpecialCharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#digit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDigit(RegexParser.DigitContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#amount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAmount(RegexParser.AmountContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#atLeastOne}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtLeastOne(RegexParser.AtLeastOneContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#anyAmount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyAmount(RegexParser.AnyAmountContext ctx);
	/**
	 * Visit a parse tree produced by {@link RegexParser#range}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange(RegexParser.RangeContext ctx);
}