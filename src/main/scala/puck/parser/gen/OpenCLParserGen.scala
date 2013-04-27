package puck.parser.gen

import scala.virtualization.lms.internal.{FatExpressions, Effects, Expressions}
import trochee.codegen._
import trochee.basic.{SpireOpsExp, GenSpireOps}
import scala.virtualization.lms.common.IfThenElseExp

trait OpenCLParserGen[L] extends OpenCLKernelCodegen with OpenCLKernelGenVariables with GenSpireOps {
  val IR: Expressions with Effects with FatExpressions with trochee.kernels.KernelOpsExp with ParserCommonExp[L] with IfThenElseExp with SpireOpsExp with FloatOpsExp
  import IR._
  lazy val typeMaps = {
    Map[Class[_],String](manifestParseChart.erasure -> "PARSE_CELL" ,
    manifestTermChart.erasure -> "PARSE_CELL",
    manifestRuleCell.erasure -> "rule_cell*")
}
  override def remap[A](m: Manifest[A]) : String = {
    typeMaps.getOrElse(m.erasure, super.remap(m))
  }

  override def quote(x: Exp[Any]) = x match {
    case RuleDeref(cell, rule, grammar) => preferNoLocal(cell) + s"->rules[${quote(rule)}][${quote(grammar)}]"
    case _ => super.quote(x)
  }

  override def emitNode(sym: Sym[Any], rhs: Def[Any]) {
    rhs match {
      case Mad(a,b,c) =>
        emitValDef(sym, s"mad(${quote(a)}, ${quote(b)}, ${quote(c)})")
      case LogAdd(a, b) =>
        emitValDef(sym, s"${quote(a)} == -INFINITY ? ${quote(b)} : (${quote(a)} + log(1.0f + exp(${quote(b)} - ${quote(a)})))")
      case Log(a) =>
        cacheAndEmit(sym, s"log(${quote(a)})")
      case app@CellApply(NTCell(cell, off, begin, end, gram), symsym) =>
        cacheAndEmit(addPos(sym, app), s"${quote(cell)}[(${quote(symsym)} * CHART_SIZE + ${quote(off)} + TRIANGULAR_INDEX(${quote(begin)}, ${quote(end)}))*NUM_GRAMMARS + ${quote(gram)}]")
      case app@CellApply(TCell(cell, off, begin, gram), symsym) =>
        cacheAndEmit(addPos(sym, app), s"""${quote(cell)}[(${quote(symsym)} * TERM_CHART_SIZE + ${quote(off)} + ${quote(begin)})*NUM_GRAMMARS + ${quote(gram)}]""")
      case app@CellUpdate(NTCell(cell, off, begin, end, gram), symsym, value) =>
        emitValDef(sym, s""" ${quote(cell)}[(${quote(symsym)} * CHART_SIZE + ${quote(off)} + TRIANGULAR_INDEX(${quote(begin)}, ${quote(end)}))*NUM_GRAMMARS + ${quote(gram)}] = ${quote(value)}""")
      case BooleanNegate(b) => emitValDef(sym, "!" + quote(b))
      case BooleanAnd(lhs,rhs) => emitValDef(sym, quote(lhs) + " && " + quote(rhs))
      case BooleanOr(lhs,rhs) => emitValDef(sym, quote(lhs) + " || " + quote(rhs))
      case Printf(str, args) =>
        val call = s"""printf(${(quote(str) +: args.map(quote _)).mkString(", ")});"""
        cacheAndEmit(sym, call)
      case app@CellUpdate(TCell(cell, off, begin, gram), symsym, value) =>
        cacheAndEmit(sym, s"${quote(cell)}[(${quote(symsym)} * TERM_CHART_SIZE + ${quote(off)} + ${quote(begin)})*NUM_GRAMMARS + ${quote(gram)}] = ${quote(value)}")
      //case WriteOutput(NTCell(cell, off, begin, end, gram), acc) =>
     //   val base = s"${quote(cell)}[("
     //   val ending = s"* CHART_SIZE + ${quote(off)} + TRIANGULAR_INDEX(${quote(begin)}, ${quote(end)}))*NUM_GRAMMARS + ${quote(gram)}]"
    //    for( (id,vr) <- acc.vars) {
    //      cacheAndEmit(sym, s"$base$id $ending = ${acc.prefix}$id")
     //   }
      case _ => super.emitNode(sym, rhs)
    }

  }
}
