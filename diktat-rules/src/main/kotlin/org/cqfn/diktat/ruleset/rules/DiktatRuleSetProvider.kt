package org.cqfn.diktat.ruleset.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.calculations.AccurateCalculationsRule
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.comments.CommentsRuleWrapper
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.ruleset.rules.identifiers.LocalVariablesRule
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.kdoc.KdocCommentsWrapper
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.JavaDummyElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.JavaDummyHolder
import org.slf4j.LoggerFactory

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

class DiktatRuleSetProvider(private val diktatConfigFile: String = "diktat-analysis.yml") : RuleSetProvider {
    override val ruleSetId = DIKTAT_RULE_SET_ID

    override fun instance(config: Config): RuleSet {
        log.debug("Will run $DIKTAT_RULE_SET_ID with $diktatConfigFile (it can be placed to the run directory or the default file from resources will be used)")
        val configRules = RulesConfigReader(javaClass.classLoader).readResource(diktatConfigFile) ?: listOf()
        val rules = listOf(
                ::CommentsRuleWrapper,
                ::KdocCommentsWrapper//,
//                ::KdocMethods,
//                ::KdocFormatting,
//                ::FileNaming,
//                ::PackageNaming,
//                ::StringTemplateFormatRule,
//                ::FileSize,
//                ::IdentifierNaming,
//                ::LocalVariablesRule,
//                ::ClassLikeStructuresOrderRule,
//                ::BracesInConditionalsAndLoopsRule,
//                ::BlockStructureBraces,
//                ::EmptyBlock,
//                ::EnumsSeparated,
//                ::VariableGenericTypeDeclarationRule,
//                ::SingleLineStatementsRule,
//                ::CommentsFormatting,
//                ::ConsecutiveSpacesRule,
//                ::LongNumericalValuesSeparatedRule,
//                ::MultipleModifiersSequence,
//                ::AnnotationNewLineRule,
//                ::HeaderCommentRule,
//                ::SortRule,
//                ::StringConcatenationRule,
//                ::AccurateCalculationsRule,
//                ::LineLength,
//                ::TypeAliasRule,
//                ::BlankLinesRule,
//                ::WhiteSpaceRule,
//                ::WhenMustHaveElseRule,
//                ::FileStructureRule,  // this rule should be right before indentation because it should operate on already valid code
//                ::NewlinesRule,  // newlines need to be inserted right before fixing indentation
//                ::IndentationRule  // indentation rule should be the last because it fixes formatting after all the changes done by previous rules
        )
                .map {
                    it.invoke(configRules, config)
                }
        return RuleSet(
                DIKTAT_RULE_SET_ID,
                rules
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiktatRuleSetProvider::class.java)
    }
}

// === copy-pasted from detekt to temporarily wrap our rules

internal fun ASTNode.visitTokens(currentNode: (ASTNode) -> Unit) {
    if (this.isNoFakeElement()) {
        currentNode(this)
    }
    getChildren(null).forEach { it.visitTokens(currentNode) }
}

internal fun ASTNode.isNoFakeElement(): Boolean {
    val parent = this.psi?.parent
    return parent !is JavaDummyHolder && parent !is JavaDummyElement
}
