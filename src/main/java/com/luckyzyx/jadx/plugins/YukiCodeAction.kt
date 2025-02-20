package com.luckyzyx.jadx.plugins

import jadx.api.*
import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.instructions.args.PrimitiveType
import jadx.core.utils.exceptions.JadxRuntimeException
import java.util.function.Consumer

class YukiCodeAction(private val guiContext: JadxGuiContext, private val decompiler: JadxDecompiler) :
	Consumer<ICodeNodeRef?> {

	override fun accept(iCodeNodeRef: ICodeNodeRef?) {
		val node = decompiler.getJavaNodeByRef(iCodeNodeRef)
		val code = generateXposedSnippet(node)
		guiContext.copyToClipboard(code)
	}

	private fun generateXposedSnippet(node: JavaNode?): String {
		if (node is JavaClass) {
			return generateClassSnippet(node)
		}
		if (node is JavaMethod) {
			return generateMethodSnippet(node)
		}
		if (node is JavaField) {
			return generateFieldSnippet(node)
		}
		throw JadxRuntimeException("Unsupported node : " + (node?.javaClass))
	}

	private fun generateClassSnippet(node: JavaClass): String {
		val clsName = node.name
		val rawClassName = node.rawName

		val code = """
			val %sClazz = "%s".toClass().apply {

			}
		""".trimIndent()
		return String.format(code, clsName, rawClassName)
	}

	private fun generateMethodSnippet(node: JavaMethod): String {
		val rawClassName = node.declaringClass.rawName
		val methodNode = node.methodNode
		val methodName = methodNode.name
		val args = methodNode.argTypes.map(::fixTypeContent)
		val returnType = fixTypeContent(methodNode.returnType)

		return if (methodNode.isConstructor) {
			if (args.isEmpty()) {
				"""
					"%s".toClass().apply {
						constructor {
							emptyParam()
						}.hook {

						}
					}
				""".trimIndent().let {
					String.format(it, rawClassName)
				}
			} else {
				"""
					"%s".toClass().apply {
						constructor {
							param(%s)
						}.hook {

						}
					}
				""".trimIndent().let {
					String.format(it, rawClassName, args.joinToString(", "))
				}
			}
		} else {
			if (args.isEmpty()) {
				"""
					"%s".toClass().apply {
						method {
							name = "%s"
							emptyParam()
							returnType = %s
						}.hook {

						}
					}
				""".trimIndent().let {
					String.format(it, rawClassName, methodName, returnType)
				}
			} else {
				"""
					"%s".toClass().apply {
						method {
							name = "%s"
							param(%s)
							returnType = %s
						}.hook {

						}
					}
				""".trimIndent().let {
					String.format(it, rawClassName, methodName, args.joinToString(", "), returnType)
				}
			}
		}
	}

	private fun generateFieldSnippet(node: JavaField): String {
		val rawClassName = node.declaringClass.rawName
		val fieldNode = node.fieldNode
		val fieldName = fieldNode.name
		val static = if (node.accessFlags.isStatic) "" else "this"
		val type = fixTypeContent(node.fieldNode.type)

		val code = """
			"%s".toClass().apply {
				field {
					name = "%s"
					type = %s
				}.get(%s)
			}
		""".trimIndent()
		return String.format(code, rawClassName, fieldName, type, static)
	}

	private fun fixTypeContent(type: ArgType): String {
		return when {
			type.isGeneric -> "\"${type.`object`}\""
			type.isGenericType && type.isObject && type.isTypeKnown -> "AnyClass"
			type.isPrimitive -> when (type.primitiveType) {
				PrimitiveType.BOOLEAN -> "BooleanType"
				PrimitiveType.CHAR -> "CharType"
				PrimitiveType.BYTE -> "ByteType"
				PrimitiveType.SHORT -> "ShortType"
				PrimitiveType.INT -> "IntType"
				PrimitiveType.FLOAT -> "FloatType"
				PrimitiveType.LONG -> "LongType"
				PrimitiveType.DOUBLE -> "DoubleType"
				PrimitiveType.OBJECT -> "AnyClass"
				PrimitiveType.ARRAY -> "ArrayClass"
				PrimitiveType.VOID -> "UnitType"
				else -> throw JadxRuntimeException("Unknown or null primitive type: $type")
			}

			else -> "\"$type\""
		}
	}
}
