// Generated from /home/jakob/github/astava/intellij/astava/src/main/java/astava/samples/drawnmap/lang/antlr4/DrawNMap.g4 by ANTLR 4.1
package astava.samples.drawnmap.lang.antlr4;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DrawNMapParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DrawNMapVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull DrawNMapParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(@NotNull DrawNMapParser.StringContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#leafExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeafExpression(@NotNull DrawNMapParser.LeafExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(@NotNull DrawNMapParser.ProgramContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(@NotNull DrawNMapParser.NumberContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#addExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddExpression(@NotNull DrawNMapParser.AddExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#embeddedExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmbeddedExpression(@NotNull DrawNMapParser.EmbeddedExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(@NotNull DrawNMapParser.FunctionCallContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(@NotNull DrawNMapParser.StatementContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#property}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty(@NotNull DrawNMapParser.PropertyContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(@NotNull DrawNMapParser.IdContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(@NotNull DrawNMapParser.AssignContext ctx);

	/**
	 * Visit a parse tree produced by {@link DrawNMapParser#mulExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulExpression(@NotNull DrawNMapParser.MulExpressionContext ctx);
}