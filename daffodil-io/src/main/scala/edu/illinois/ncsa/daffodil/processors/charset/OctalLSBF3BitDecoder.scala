/* Copyright (c) 2012-2016 Tresys Technology, LLC. All rights reserved.
 *
 * Developed by: Tresys Technology, LLC
 *               http://www.tresys.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal with
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimers.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimers in the
 *     documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the names of Tresys Technology, nor the names of its contributors
 *     may be used to endorse or promote products derived from this Software
 *     without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE
 * SOFTWARE.
 */

package edu.illinois.ncsa.daffodil.processors.charset

import edu.illinois.ncsa.daffodil.schema.annotation.props.gen.BitOrder

/**
 * Some encodings are not byte-oriented.
 *
 * X-DFDL-OCTAL-LSBF occupies only 3 bits with each
 * code unit.
 *
 */

object OctalLSBF3BitCharset
  extends NBitsWidth_BitsCharset("X-DFDL-OCTAL-LSBF",
    "01234567",
    3, // width
    BitOrder.LeastSignificantBitFirst,
    0) // replacement charCode for encoding of unmapped chars.

object OctalMSBF3BitCharset
  extends NBitsWidth_BitsCharset("X-DFDL-OCTAL-MSBF",
    "01234567",
    3, // width
    BitOrder.MostSignificantBitFirst,
    0) // replacement charCode for encoding of unmapped chars.
