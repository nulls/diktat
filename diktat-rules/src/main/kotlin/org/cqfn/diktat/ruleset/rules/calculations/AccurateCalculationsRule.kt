package org.cqfn.diktat.ruleset.rules.calculations

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FLOAT_IN_ACCURATE_CALCULATIONS
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.types.typeUtil.isDouble
import org.jetbrains.kotlin.types.typeUtil.isFloat

/**
 * Rule that checks that floating-point numbers are not used for accurate calculations
 * 1. Checks that floating-point numbers are not used in arithmetic binary expressions
 * Fixme: detect variables by type, not only floating-point literals
 */
class AccurateCalculationsRule(private val configRules: List<RulesConfig>, config: Config) : Rule(config) {
    override val issue = Issue("accurate-calculations", Severity.Defect, "Description TBD", Debt.TEN_MINS)

    companion object {
        private val arithmeticOperationTokens = listOf(KtTokens.PLUS, KtTokens.PLUSEQ, KtTokens.PLUSPLUS,
                KtTokens.MINUS, KtTokens.MINUSEQ, KtTokens.MINUSMINUS,
                KtTokens.MUL, KtTokens.MULTEQ, KtTokens.DIV, KtTokens.DIVEQ,
                KtTokens.PERC, KtTokens.PERCEQ,
                KtTokens.GT, KtTokens.LT, KtTokens.LTEQ, KtTokens.GTEQ,
                KtTokens.EQEQ
        )
        private val arithmeticOperationsFunctions = listOf("equals", "compareTo")
    }

    override fun visitExpression(expression: KtExpression) {
        super.visitExpression(expression)
        if (bindingContext == BindingContext.EMPTY) return
        when (expression) {
            is KtBinaryExpression -> handleBinaryExpression(expression)
            is KtDotQualifiedExpression -> handleFunction(expression)
            else -> return
        }
        val x = 1.0 + 2.7f / 1.4F
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleBinaryExpression(expression: KtBinaryExpression) = expression
            .takeIf { it.operationToken in arithmeticOperationTokens }
            ?.let { expr ->
                // !! is safe because `KtBinaryExpression#left` is annotated `Nullable IfNotParsed`
                val floatValue = expr.left!!.takeIf { it.isFloatingPoint() }
                        ?: expr.right!!.takeIf { it.isFloatingPoint() }
                checkFloatValue(floatValue, expr)
            }

    private fun handleFunction(expression: KtDotQualifiedExpression) = expression
            .takeIf { it.selectorExpression is KtCallExpression }
            ?.run { receiverExpression to selectorExpression as KtCallExpression }
            ?.takeIf {
                (it.second.calleeExpression as? KtNameReferenceExpression)
                        ?.getReferencedName() in arithmeticOperationsFunctions
            }
            ?.let { (receiverExpression, selectorExpression) ->
                val floatValue = receiverExpression.takeIf { it.isFloatingPoint() }
                        ?: selectorExpression
                                .valueArguments
                                .find { it.getArgumentExpression()?.isFloatingPoint() ?: false }

                checkFloatValue(floatValue, expression)
            }

    private fun checkFloatValue(floatValue: PsiElement?, expression: KtExpression) {
        if (floatValue != null) {
            // float value is used in comparison
            report(CodeSmell(issue,
                    Entity.from(expression),
                    "${FLOAT_IN_ACCURATE_CALCULATIONS.warnText()} float value of <${floatValue.text}> used in arithmetic expression in ${expression.text}"))
        }
    }

    private fun KtExpression.isFloatingPoint(): Boolean {
        val type = getType(bindingContext)
        if (type == null) {
            println("Cannot determine type of expression $text")
            return false
        }
        println("type of $text is $type")
        return type.run { isFloat() || isDouble() }
    }
}
