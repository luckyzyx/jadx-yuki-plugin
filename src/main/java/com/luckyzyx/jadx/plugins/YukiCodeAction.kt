package com.luckyzyx.jadx.plugins

import jadx.api.*
import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
import jadx.core.dex.info.AccessInfo
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.instructions.args.PrimitiveType
import jadx.core.utils.exceptions.JadxRuntimeException
import java.util.function.Consumer

class YukiCodeAction(
	private val guiContext: JadxGuiContext,
	private val decompiler: JadxDecompiler,
	private val options: JadxPluginOptions
) : Consumer<ICodeNodeRef?> {

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
//		val clsNode = node.classNode
		val rawClassName = node.rawName

		return """
			val %sClazz = "%s".toClass().resolve().apply {

			}
		""".trimIndent().let {
			String.format(it, clsName, rawClassName)
		}
	}

	private fun generateMethodSnippet(node: JavaMethod): String {
		val rawClassName = node.declaringClass.rawName
		val methodNode = node.methodNode
		val methodName = methodNode.name
		val modifiers = fixModifierContent(methodNode.accessFlags)
		val args = methodNode.argTypes.map(::fixTypeContent)
		val returnType = fixTypeContent(methodNode.returnType)

		val formats = arrayListOf<String>()
		val sb = StringBuilder()
		if (options.addMethodClass) sb.append("\"$rawClassName\".toClass().resolve().apply {\n")

		if (options.addModifiers) formats.add(modifiers.joinToString(", "))
		if (args.isNotEmpty()) formats.add(args.joinToString(", "))

		if (methodNode.isConstructor) {
			sb.append("firstConstructor {\n")
			if (options.addModifiers) sb.append("modifiers(%s)\n")
			if (args.isEmpty()) sb.append("emptyParameters()\n") else sb.append("parameters(%s)\n")
		} else {
			sb.append("firstMethod {\n")
			if (options.addModifiers) sb.append("modifiers(%s)\n")
			sb.append("name = \"$methodName\"\n")
			if (args.isEmpty()) sb.append("emptyParameters()\n") else sb.append("parameters(%s)\n")
			sb.append("returnType = $returnType\n")
		}
		sb.append("}.hook {\n")
		sb.append("\n")
		sb.append("}\n")
		if (options.addMethodClass) sb.append("}")

		return String.format(sb.toString(), *formats.toTypedArray())
	}

	private fun generateFieldSnippet(node: JavaField): String {
		val rawClassName = node.declaringClass.rawName
		val fieldNode = node.fieldNode
		val fieldName = fieldNode.name
		val modifiers = fixModifierContent(fieldNode.accessFlags)
		val isStatic = fieldNode.isStatic
		val type = fixTypeContent(fieldNode.type)

		val formats = arrayListOf<String>()
		val sb = StringBuilder()
		if (options.addMethodClass) sb.append("\"$rawClassName\".toClass().resolve().apply {\n")

		if (options.addModifiers) formats.add(modifiers.joinToString(", "))
		formats.add(if (isStatic) "get()" else "of(instance).get()")

		sb.append("firstField {\n")
		if (options.addModifiers) sb.append("modifiers(%s)\n")
		sb.append("name = \"$fieldName\"\n")
		sb.append("type = $type\n")
		sb.append("}.%s\n")
		if (options.addMethodClass) sb.append("}")

		return String.format(sb.toString(), *formats.toTypedArray())
	}

	private fun fixModifierContent(info: AccessInfo): ArrayList<String> {
		val list = arrayListOf<String>()
		if (info.isPublic) list.add("Modifiers.PUBLIC")
		if (info.isPrivate) list.add("Modifiers.PRIVATE")
		if (info.isProtected) list.add("Modifiers.PROTECTED")
		if (info.isStatic) list.add("Modifiers.STATIC")
		if (info.isFinal) list.add("Modifiers.FINAL")
		if (info.isSynchronized) list.add("Modifiers.SYNCHRONIZED")
		if (info.isVolatile) list.add("Modifiers.VOLATILE")
		if (info.isTransient) list.add("Modifiers.TRANSIENT")
		if (info.isNative) list.add("Modifiers.NATIVE")
		if (info.isInterface) list.add("Modifiers.INTERFACE")
		if (info.isAbstract) list.add("Modifiers.ABSTRACT")
//		if (info.isStrict) list.add("Modifier.STRICT")
		return list
	}

	private fun fixTypeContent(type: ArgType, log: Boolean = true): String {
		return when {
			type.isPrimitive -> when (type) {
				ArgType.BOOLEAN -> "Boolean::class"
				ArgType.BYTE -> "Byte::class"
				ArgType.CHAR -> "Char::class"
				ArgType.INT -> "Int::class"
				ArgType.DOUBLE -> "Double::class"
				ArgType.LONG -> "Long::class"
				ArgType.FLOAT -> "Float::class"
				ArgType.SHORT -> "Short::class"

				ArgType.VOID -> "Void.TYPE"

				else -> throw JadxRuntimeException("Unknown or null primitive type: $type")
			}

			type.isArray -> when (type.arrayElement) {
				ArgType.BOOLEAN -> "BooleanArray::class"
				ArgType.BYTE -> "ByteArray::class"
				ArgType.CHAR -> "CharArray::class"
				ArgType.INT -> "IntArray::class"
				ArgType.DOUBLE -> "DoubleArray::class"
				ArgType.LONG -> "LongArray::class"
				ArgType.FLOAT -> "FloatArray::class"
				ArgType.SHORT -> "ShortArray::class"

				else -> "ArrayClass(${fixTypeContent(type.arrayElement, false)})"
			}

			//忽略列表成员泛型
			type.isGeneric && type.isObject -> fixTypeContent(ArgType.`object`(type.`object`), false)
			//T泛型擦除
			type.isGenericType && type.isObject -> "Any::class"

			else -> when (type) {
				PrimitiveType.BOOLEAN.boxType -> "JBoolean::class"
				PrimitiveType.BYTE.boxType -> "JByte::class"
				PrimitiveType.CHAR.boxType -> "JCharacter::class"
				PrimitiveType.INT.boxType -> "JInteger::class"
				PrimitiveType.DOUBLE.boxType -> "JDouble::class"
				PrimitiveType.LONG.boxType -> "JLong::class"
				PrimitiveType.FLOAT.boxType -> "JFloat::class"
				PrimitiveType.SHORT.boxType -> "JShort::class"

				PrimitiveType.VOID.boxType -> "JVoid::class"

				ArgType.OBJECT -> "Any::class"
				ArgType.CLASS -> "Class::class"
				ArgType.STRING -> "String::class"
				ArgType.ENUM -> "Enum::class"
				ArgType.THROWABLE -> "Throwable::class"
				ArgType.EXCEPTION -> "Exception::class"

				else -> "\"$type\""
			}
		} + if (options.addTypeLog && log) "\n${getTyeLogs(type)}\n" else ""

	}

	fun getTyeLogs(type: ArgType): String {
		return """
			//-------------------------------
			//$type
			//isPrimitive: ${type.isPrimitive}
			//primitiveType: ${type.primitiveType}
			//isObject: ${type.isObject}
			//Object: ${if (type.isObject) type.`object` else null}
			//isArray: ${type.isArray}
			//arrayElement: ${type.arrayElement}
			//isGeneric: ${type.isGeneric}
			//isGenericType: ${type.isGenericType}
			//genericTypes: ${type.genericTypes}
			//isWildcard: ${type.isWildcard}
			//wildcardType: ${type.wildcardType}
			//wildcardBound: ${type.wildcardBound}
			//-------------------------------
		""".trimIndent()
	}
}
