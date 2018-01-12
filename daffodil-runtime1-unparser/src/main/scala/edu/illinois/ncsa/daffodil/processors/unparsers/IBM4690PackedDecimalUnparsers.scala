/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.illinois.ncsa.daffodil.processors.unparsers

import edu.illinois.ncsa.daffodil.processors.parsers.HasKnownLengthInBits
import edu.illinois.ncsa.daffodil.processors.ElementRuntimeData
import edu.illinois.ncsa.daffodil.processors.ParseOrUnparseState
import edu.illinois.ncsa.daffodil.util.DecimalUtils
import java.lang.{ Long => JLong }
import java.math.{ BigInteger => JBigInteger }
import edu.illinois.ncsa.daffodil.processors.parsers.HasRuntimeExplicitLength
import edu.illinois.ncsa.daffodil.processors.Evaluatable
import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.LengthUnits

abstract class IBM4690PackedIntegerBaseUnparser(
  e: ElementRuntimeData)
  extends PackedBinaryIntegerBaseUnparser(e) {

  override def fromBigInteger(bigInt: JBigInteger, nBits: Int): Array[Byte] = DecimalUtils.ibm4690FromBigInteger(bigInt, nBits)
}

class IBM4690PackedIntegerKnownLengthUnparser(
  e: ElementRuntimeData,
  override val lengthInBits: Int)
  extends IBM4690PackedIntegerBaseUnparser(e)
  with HasKnownLengthInBits {
}

class IBM4690PackedIntegerRuntimeLengthUnparser(
  val e: ElementRuntimeData,
  val lengthEv: Evaluatable[JLong],
  val lUnits: LengthUnits)
  extends IBM4690PackedIntegerBaseUnparser(e)
  with HasRuntimeExplicitLength {

  override lazy val runtimeDependencies = List(lengthEv)
}

final class IBM4690PackedIntegerMinLengthInBytesUnparser(
  minLengthInBytes: Int,
  e: ElementRuntimeData)
  extends IBM4690PackedIntegerBaseUnparser(e) {

  override def getBitLength(state: ParseOrUnparseState): Int = {
    val len = state.currentNode.get.asSimple.dataValue.asInstanceOf[Array[Byte]].length * 8
    val min = minLengthInBytes *  8
    scala.math.max(len, min)
  }
}

abstract class IBM4690PackedDecimalBaseUnparser(
  e: ElementRuntimeData,
  binaryDecimalVirtualPoint: Int)
  extends PackedBinaryDecimalBaseUnparser(e, binaryDecimalVirtualPoint) {

  override def fromBigInteger(bigInt: JBigInteger, nBits: Int): Array[Byte] = DecimalUtils.ibm4690FromBigInteger(bigInt, nBits)
}

class IBM4690PackedDecimalKnownLengthUnparser(
  e: ElementRuntimeData,
  binaryDecimalVirtualPoint: Int,
  override val lengthInBits: Int)
  extends IBM4690PackedDecimalBaseUnparser(e, binaryDecimalVirtualPoint)
  with HasKnownLengthInBits {
}

class IBM4690PackedDecimalRuntimeLengthUnparser(
  val e: ElementRuntimeData,
  binaryDecimalVirtualPoint: Int,
  val lengthEv: Evaluatable[JLong],
  val lUnits: LengthUnits)
  extends IBM4690PackedDecimalBaseUnparser(e, binaryDecimalVirtualPoint)
  with HasRuntimeExplicitLength {

  override lazy val runtimeDependencies = List(lengthEv)
}

final class IBM4690PackedDecimalMinLengthInBytesUnparser(
  minLengthInBytes: Int,
  e: ElementRuntimeData,
  binaryDecimalVirtualPoint: Int)
  extends IBM4690PackedDecimalBaseUnparser(e, binaryDecimalVirtualPoint) {

  override def getBitLength(state: ParseOrUnparseState): Int = {
    val len = state.currentNode.get.asSimple.dataValue.asInstanceOf[Array[Byte]].length * 8
    val min = minLengthInBytes *  8
    scala.math.max(len, min)
  }
}
