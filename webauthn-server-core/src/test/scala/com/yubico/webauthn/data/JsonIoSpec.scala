// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.yubico.webauthn.data

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.yubico.internal.util.JacksonCodecs
import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.AssertionResult
import com.yubico.webauthn.Generators._
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.attestation.Attestation
import com.yubico.webauthn.attestation.Generators._
import com.yubico.webauthn.attestation.Transport
import com.yubico.webauthn.data.Generators._
import com.yubico.webauthn.extension.appid.AppId
import com.yubico.webauthn.extension.appid.Generators._
import org.junit.runner.RunWith
import org.scalacheck.Arbitrary
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

@RunWith(classOf[JUnitRunner])
class JsonIoSpec
    extends FunSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks {

  def json: ObjectMapper =
    new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .setSerializationInclusion(Include.NON_ABSENT)
      .registerModule(new Jdk8Module())

  describe("The class") {

    def test[A](tpe: TypeReference[A])(implicit a: Arbitrary[A]): Unit = {
      val cn = tpe.getType.getTypeName
      describe(s"${cn}") {
        it("can be serialized to JSON.") {
          forAll { value: A =>
            val encoded: String = json.writeValueAsString(value)

            encoded should not be empty
          }
        }

        it("can be deserialized from JSON.") {
          forAll { value: A =>
            val encoded: String = json.writeValueAsString(value)
            val decoded: A = json.readValue(encoded, tpe)

            decoded should equal(value)
          }
        }

        it("is identical after multiple serialization round-trips..") {
          forAll { value: A =>
            println(value)

            val encoded: String = json.writeValueAsString(value)
            println(encoded)

            val decoded: A = json.readValue(encoded, tpe)
            println(decoded)

            val recoded: String = json.writeValueAsString(decoded)
            println(recoded)

            decoded should equal(value)
            recoded should equal(encoded)
          }
        }
      }
    }

    test(new TypeReference[AuthenticatorAssertionResponse]() {})
    test(new TypeReference[AuthenticatorAttachment]() {})
  }

}
