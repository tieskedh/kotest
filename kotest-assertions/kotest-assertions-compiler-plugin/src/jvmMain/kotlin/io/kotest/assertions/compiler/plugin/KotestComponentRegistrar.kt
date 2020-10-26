package io.kotest.assertions.compiler.plugin

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.common.serialization.knownBuiltins
import org.jetbrains.kotlin.backend.jvm.codegen.psiElement
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.classValueType
import org.jetbrains.kotlin.util.OperatorNameConventions

class KotestComponentRegistrar : ComponentRegistrar {

   override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
      IrGenerationExtension.registerExtension(
         project,
         TestIrGenerationExtension(configuration)
      )
   }
}

class TestIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

   private fun log(any: Any?) {
      configuration.report(CompilerMessageSeverity.STRONG_WARNING, any?.toString() ?: "<null>")
   }

   override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

      fun createPackage(packageName: String): IrPackageFragment =
         IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
            moduleFragment.descriptor,
            FqName(packageName)
         )

      fun createClass(
         irPackage: IrPackageFragment,
         shortName: String,
         classKind: ClassKind,
         classModality: Modality
      ): IrClassSymbol = buildClass {
         name = Name.identifier(shortName)
         kind = classKind
         modality = classModality
      }.apply {
         parent = irPackage
         createImplicitParameterDeclarationWithWrappedDescriptor()
      }.symbol

      for (file in moduleFragment.files) {
         configuration.report(CompilerMessageSeverity.STRONG_WARNING, "Processing file ${file.fqName}")
         file.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {

            override fun visitCall(expression: IrCall): IrExpression {
               return when (expression.symbol.owner.name.asString()) {
                  // should have an extension receiver which is the left hand side
                  // should have 1 arg, which is the right hand side
                  "shouldBe" ->
                     if (expression.valueArgumentsCount == 1 && expression.extensionReceiver != null) {
                        rewriteShouldBe(expression, expression.extensionReceiver!!, expression.getValueArgument(0)!!)
                     } else expression
                  else -> expression
               }
            }

            /**
             * a.b.c shouldBe d.e.f should be broken down into:
             *
             * val __1 = a
             * val __2 = a.b
             * val __3 = b.c
             *
             * val __4 = d
             * val __5 = d.e
             * val __6 = e.f
             *
             * try {
             *   a.b.c shouldBe d.e.f
             * } catch (e: AssertionError) {
             *   println("Assertion failed ${e.message}")
             *   vals.foreach {
             *
             *   }
             *   throw e
             * }
             *
             * To give output like:
             *
             * Assertion failed: expected:<a> was:<b>
             *
             * a        <-- foo
             *  .b      <-- bar
             *  .c      <-- a
             *
             *  shouldBe
             *
             * d        <-- baz
             *  .e      <-- boo
             *  .f      <-- b
             *
             * With expressions:
             *
             * (a.b.c + d.e.f) + g.h.i shouldBe true
             *
             *
             * Assertion failed: expected:<b> was:<a33>
             *
             * (
             *  (
             *   a        <-- foo
             *    .b      <-- bar
             *    .c      <-- a
             *
             *   +        <-- a3
             *
             *   d        <-- 1
             *    .e      <-- 2
             *    .f      <-- 3
             *  )
             *
             *  +         <-- a33
             *
             *  g         <-- 1
             *   .h       <-- 2
             *   .i       <-- 3
             *
             * )
             *
             * shouldBe
             *
             * d         <-- baz
             *  .e       <-- boo
             *  .f       <-- b
             *
             */
            private fun rewriteShouldBe(expression: IrCall, lhs: IrExpression, rhs: IrExpression): IrExpression {
               log("Visting shouldBe ${expression.symbol.owner.name}")
               log("lhs type " + lhs::class)
               log("rhs type " + rhs::class)
               processTree(lhs)
               log("shouldBe")
               processTree(rhs)
//               expression.transform(object : IrElementTransformerVoid() {
//                  override fun visitExpression(expression: IrExpression): IrExpression {
//                     log("transform ${expression.type}")
//                     return super.visitExpression(expression)
//                  }
//               }, null)

               return IrBlockBuilder(
                  pluginContext,
                  currentScope?.scope!!,
                  expression.startOffset,
                  expression.endOffset
               ).irBlock {
                  val c = context.irBuiltIns.throwableClass.constructors.single { it.owner.valueParameters.isEmpty() }
//                  val type = context.builtIns.getBuiltInClassByFqName(FqName("kotlin.Exception")).defaultType as IrType
//                  val c = type.getClass()!!.constructors.single { it.valueParameters.isEmpty() }.symbol
                  val t = irCallConstructor(c, emptyList())
                  +irThrow(t)
               }
            }

            private fun processTree(expr: IrExpression) {
               when (expr) {
                  is IrGetValue -> log("dfgdfgdfgdf ")
                  is IrCall -> log("adasd ")
                  else -> log("unsupported expr type")
               }
            }
         })
      }
   }
}


