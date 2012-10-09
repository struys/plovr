/*
 * Copyright 2012 The Closure Compiler Authors.
 *
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
package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_CONSTRUCTOR_MISING;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_DESCRIPTOR_NOT_VALID;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_MODIFIERS_NOT_VALID;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_STATICS_NOT_VALID;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_SUPER_CLASS_NOT_VALID;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_TARGET_INVALID;
import static com.google.javascript.jscomp.ClosureRewriteClass.GOOG_CLASS_UNEXPECTED_PARAMS;


/**
 * Unit tests for ClosureRewriteGoogClass
 * @author johnlenz@google.com (John Lenz)
 */
public class ClosureRewriteClassTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new ClosureRewriteClass(compiler);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.enableEcmaScript5(false);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  public void testBasic1() {
    test(
        "var x = goog.defineClass(null, {\n" +
        "  constructor: function(){}\n" +
        "});",

        "{var x = function() {};}");
  }

  public void testBasic2() {
    test(
        "var x = {};\n" +
        "x.y = goog.defineClass(null, {\n" +
        "  constructor: function(){}\n" +
        "});",

        "var x = {};" +
        "{x.y = function() {};}");
  }

  public void testInnerClass1() {
    test(
        "var x = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    inner: goog.defineClass(x,{\n" +
        "      constructor: function(){\n" +
        "        this.bar = 1;\n" +
        "      }\n" +
        "    })\n" +
        "  }\n" +
        "});",

        "{" +
        "var x=function(){this.foo=1};" +
        "goog.inherits(x,some.Super);" +
        "{" +
        "x.inner=function(){this.bar=1};" +
        "goog.inherits(x.inner,x);" +
        "}" +
        "}");
  }

  public void testComplete1() {
    test(
        "var x = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    prop1: 1,\n" +
        "    /** @const */\n" +
        "    PROP2: 2\n" +
        "  },\n" +
        "  anotherProp: 1,\n" +
        "  aMethod: function() {}\n" +
        "}, [goog.addSingletonGetter, seal]);",

        "{" +
        "var x=function(){this.foo=1};" +
        "goog.inherits(x,some.Super);" +
        "x.prop1=1;" +
        "x.PROP2=2;" +
        "x.prototype.anotherProp=1;" +
        "x.prototype.aMethod=function(){};" +
        "goog.addSingletonGetter(x);" +
        "seal(x);" +
        "}");
  }

  public void testComplete2() {
    test(
        "x.y = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    prop1: 1,\n" +
        "    /** @const */\n" +
        "    PROP2: 2\n" +
        "  },\n" +
        "  anotherProp: 1,\n" +
        "  aMethod: function() {}\n" +
        "}, [goog.addSingletonGetter, seal]);",

        "{\n" +
        "/** @constructor */\n" +
        "x.y=function(){this.foo=1};\n" +
        "goog.inherits(x.y,some.Super);" +
        "x.y.prop1=1;\n" +
        "/** @const */\n" +
        "x.y.PROP2=2;\n" +
        "x.y.prototype.anotherProp=1;" +
        "x.y.prototype.aMethod=function(){};" +
        "goog.addSingletonGetter(x.y);" +
        "seal(x.y);" +
        "}");
  }


  public void testInvalid1() {
    testSame(
        "var x = goog.defineClass();",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass('foo');",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(foo());",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({'foo':1});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({1:1});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);

    this.enableEcmaScript5(true);

    testSame(
        "var x = goog.defineClass({get foo() {return 1}});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({set foo(a) {}});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
  }

  public void testInvalid2() {
    testSame(
        "var x = goog.defineClass(null);",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, null);",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, foo());",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
  }

  public void testInvalid3() {
    testSame(
        "var x = goog.defineClass(null, {});",
        GOOG_CLASS_CONSTRUCTOR_MISING, true);
  }

  public void testInvalid4() {
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: null" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: foo" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: {'foo': 1}" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: {1: 1}" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);  }

  public void testInvalid5() {
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}" +
        "}, null);",
        GOOG_CLASS_MODIFIERS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}" +
        "}, foo);",
        GOOG_CLASS_MODIFIERS_NOT_VALID, true);
  }

  public void testInvalid6() {
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}" +
        "}, [], null);",
        GOOG_CLASS_UNEXPECTED_PARAMS, true);
  }

  public void testInvalid7() {
    testSame(
        "goog.defineClass();",
        GOOG_CLASS_TARGET_INVALID, true);

    testSame(
        "var x = goog.defineClass() || null;",
        GOOG_CLASS_TARGET_INVALID, true);

    testSame(
        "({foo: goog.defineClass()});",
        GOOG_CLASS_TARGET_INVALID, true);
  }
}
