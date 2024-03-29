/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.daffodil.processors.unparsers

import org.apache.daffodil.processors._
import org.apache.daffodil.infoset._
import org.apache.daffodil.processors.RuntimeData
import org.apache.daffodil.processors.dfa.DFADelimiter
import org.apache.daffodil.schema.annotation.props.gen.ChoiceLengthKind
import org.apache.daffodil.util.Maybe._
import org.apache.daffodil.util.Maybe
import org.apache.daffodil.util.MaybeInt
import org.apache.daffodil.util.Maybe._

class ChoiceCombinatorUnparser(
  mgrd: ModelGroupRuntimeData,
  eventUnparserMap: Map[ChoiceBranchEvent, Unparser],
  choiceLengthInBits: MaybeInt)
  extends CombinatorUnparser(mgrd)
  with ToBriefXMLImpl {
  override def nom = "Choice"

  override lazy val runtimeDependencies = Vector()

  override lazy val childProcessors = eventUnparserMap.map { case (k, v) => v }.toSeq.toVector

  def unparse(state: UState): Unit = {
    state.pushTRD(mgrd)
    val event: InfosetAccessor = state.inspectOrError
    val key: ChoiceBranchEvent = event match {
      //
      // The ChoiceBranchStartEvent(...) is not a case class constructor. It is a
      // hash-table lookup for a cached value. This avoids constructing these
      // objects over and over again.
      //
      case e if e.isStart && e.isElement => ChoiceBranchStartEvent(e.erd.namedQName)
      case e if e.isEnd && e.isElement => ChoiceBranchEndEvent(e.erd.namedQName)
      case e if e.isStart && e.isArray => ChoiceBranchStartEvent(e.erd.namedQName)
      case e if e.isEnd && e.isArray => ChoiceBranchEndEvent(e.erd.namedQName)
    }

    val childUnparser = eventUnparserMap.get(key).getOrElse {
      UnparseError(One(mgrd.schemaFileLocation), One(state.currentLocation),
        "Found next element %s, but expected one of %s.",
        key.qname.toExtendedSyntax,
        eventUnparserMap.keys.map { _.qname.toExtendedSyntax }.mkString(", "))
    }
    state.popTRD(mgrd)
    state.pushTRD(childUnparser.context.asInstanceOf[TermRuntimeData])
    if (choiceLengthInBits.isDefined) {
      val suspendableOp = new ChoiceUnusedUnparserSuspendableOperation(mgrd, choiceLengthInBits.get)
      val choiceUnusedUnparser = new ChoiceUnusedUnparser(mgrd, choiceLengthInBits.get, suspendableOp)

      suspendableOp.captureDOSStartForChoiceUnused(state)
      childUnparser.unparse1(state)
      suspendableOp.captureDOSEndForChoiceUnused(state)
      choiceUnusedUnparser.unparse(state)
    } else {
      childUnparser.unparse1(state)
    }
    state.popTRD(childUnparser.context.asInstanceOf[TermRuntimeData])
  }
}

// Choices inside a hidden group ref (i.e. Hidden Choices) are slightly
// different because we will never see events for any of the branches. Instead,
// we will just always pick the branch in which every thing is defaultble or
// OVC, so we can calculated exactly which branch to take in a hidden choice
// statically at compile time. That logic is in ChoiceCombinator, and the
// branch to take is passed into this HiddenChoiceCombinatorUnparser.
class HiddenChoiceCombinatorUnparser(mgrd: ModelGroupRuntimeData, branchUnparser: Unparser)
  extends CombinatorUnparser(mgrd)
  with ToBriefXMLImpl {
  override def nom = "HiddenChoice"
  override lazy val runtimeDependencies = Vector()

  override lazy val childProcessors = Vector(branchUnparser)

  def unparse(state: UState): Unit = {
    branchUnparser.unparse1(state)
  }
}

class DelimiterStackUnparser(
  initiatorOpt: Maybe[InitiatorUnparseEv],
  separatorOpt: Maybe[SeparatorUnparseEv],
  terminatorOpt: Maybe[TerminatorUnparseEv],
  ctxt: TermRuntimeData,
  bodyUnparser: Unparser)
  extends CombinatorUnparser(ctxt) {
  override def nom = "DelimiterStack"

  override def toBriefXML(depthLimit: Int = -1): String = {
    if (depthLimit == 0) "..." else
      "<DelimiterStack initiator='" + initiatorOpt +
        "' separator='" + separatorOpt +
        "' terminator='" + terminatorOpt + "'>" +
        bodyUnparser.toBriefXML(depthLimit - 1) +
        "</DelimiterStack>"
  }

  override lazy val childProcessors = Vector(bodyUnparser)

  override lazy val runtimeDependencies = (initiatorOpt.toList ++ separatorOpt.toList ++ terminatorOpt.toList).toVector

  def unparse(state: UState): Unit = {
    // Evaluate Delimiters
    val init = if (initiatorOpt.isDefined) initiatorOpt.get.evaluate(state) else Array[DFADelimiter]()
    val sep = if (separatorOpt.isDefined) separatorOpt.get.evaluate(state) else Array[DFADelimiter]()
    val term = if (terminatorOpt.isDefined) terminatorOpt.get.evaluate(state) else Array[DFADelimiter]()

    val node = DelimiterStackUnparseNode(init, sep, term)

    state.pushDelimiters(node)

    bodyUnparser.unparse1(state)

    state.popDelimiters
  }
}

class DynamicEscapeSchemeUnparser(escapeScheme: EscapeSchemeUnparseEv, ctxt: TermRuntimeData, bodyUnparser: Unparser)
  extends CombinatorUnparser(ctxt) {
  override def nom = "EscapeSchemeStack"

  override lazy val childProcessors = Vector(bodyUnparser)

  override lazy val runtimeDependencies = Vector(escapeScheme)

  def unparse(state: UState): Unit = {
    // evaluate the dynamic escape scheme in the correct scope. the resulting
    // value is cached in the Evaluatable (since it is manually cached) and
    // future parsers/evaluatables that use this escape scheme will use that
    // cached value.
    escapeScheme.newCache(state)
    escapeScheme.evaluate(state)

    // Unparse
    bodyUnparser.unparse1(state)

    // invalidate the escape scheme cache
    escapeScheme.invalidateCache(state)
  }
}
