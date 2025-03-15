package com.luckyzyx.jadx.plugins

import com.highcapable.yukireflection.type.java.*
import jadx.api.*
import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
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
		val rawClassName = node.rawName

		return """
			val %sClazz = "%s".toClass().apply {

			}
		""".trimIndent().let {
			String.format(it, clsName, rawClassName)
		}
	}

	private fun generateMethodSnippet(node: JavaMethod): String {
		val rawClassName = node.declaringClass.rawName
		val methodNode = node.methodNode
		val methodName = methodNode.name
		val args = methodNode.argTypes.map(::fixTypeContent)
		val returnType = fixTypeContent(methodNode.returnType)

		val addClass = options.addMethodClass
		return if (methodNode.isConstructor) {
			"""
				${if (addClass) "\"%s\".toClass().apply {" else ""}
					constructor {
						${if (args.isEmpty()) "emptyParam()" else "param(%s)"}
					}.hook {

					}
				${if (addClass) "}" else ""}
			""".trimIndent().let {
				val formats = ArrayList<String>().apply {
					if (addClass) add(rawClassName)
					if (args.isNotEmpty()) add(args.joinToString(", "))
				}
				String.format(it, *formats.toTypedArray())
			}
		} else {
			"""
				${if (addClass) "\"%s\".toClass().apply {" else ""}
					method {
						name = "%s"
						${if (args.isEmpty()) "emptyParam()" else "param(%s)"}
						returnType = %s
					}.hook {

					}
				${if (addClass) "}" else ""}
			""".trimIndent().let {
				val formats = ArrayList<String>().apply {
					if (addClass) add(rawClassName)
					add(methodName)
					if (args.isNotEmpty()) add(args.joinToString(", "))
					add(returnType)
				}
				String.format(it, *formats.toTypedArray())
			}
		}
	}

	private fun generateFieldSnippet(node: JavaField): String {
		val rawClassName = node.declaringClass.rawName
		val fieldNode = node.fieldNode
		val fieldName = fieldNode.name
		val static = if (node.accessFlags.isStatic) "" else "this"
		val type = fixTypeContent(node.fieldNode.type)

		val addClass = options.addFieldClass
		return """
			${if (addClass) "\"%s\".toClass().apply {" else ""}
				field {
					name = "%s"
					type = %s
				}.get(%s)
			${if (addClass) "}" else ""}
		""".trimIndent().let {
			val formats = ArrayList<String>().apply {
				if (addClass) add(rawClassName)
				add(fieldName)
				add(type)
				add(static)
			}
			String.format(it, *formats.toTypedArray())
		}
	}

	private fun fixTypeContent(type: ArgType): String {
		return when {
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

			type.isObject -> when (type.`object`) {
				IntClass.name -> "IntClass"
				IntArrayClass.name -> "IntArrayClass"
				IntArrayType.name -> "IntArrayType"
				DoubleClass.name -> "DoubleClass"
				DoubleArrayClass.name -> "DoubleArrayClass"
				DoubleArrayType.name -> "DoubleArrayType"
				LongClass.name -> "LongClass"
				LongArrayClass.name -> "LongArrayClass"
				LongArrayType.name -> "LongArrayType"
				FloatClass.name -> "FloatClass"
				FloatArrayClass.name -> "FloatArrayClass"
				FloatArrayType.name -> "FloatArrayType"
				ShortClass.name -> "ShortClass"
				ShortArrayClass.name -> "ShortArrayClass"
				ShortArrayType.name -> "ShortArrayType"
				ByteClass.name -> "ByteClass"
				ByteArrayClass.name -> "ByteArrayClass"
				ByteArrayType.name -> "ByteArrayType"
				CharClass.name -> "CharClass"
				CharArrayClass.name -> "CharArrayClass"
				CharArrayType.name -> "CharArrayType"
				BooleanClass.name -> "BooleanClass"
				BooleanArrayClass.name -> "BooleanArrayClass"
				BooleanArrayType.name -> "BooleanArrayType"
				StringClass.name -> "StringClass"
				StringArrayClass.name -> "StringArrayClass"
				CharSequenceClass.name -> "CharSequenceClass"
				CharSequenceArrayClass.name -> "CharSequenceArrayClass"

				UnitClass.name -> "UnitClass"
				AnyClass.name -> "AnyClass"
				AnyArrayClass.name -> "AnyArrayClass"
				NumberClass.name -> "NumberClass"
				NumberArrayClass.name -> "NumberArrayClass"

				ArrayClass.name -> "ArrayClass"
				ListClass.name -> "ListClass"
				ArrayListClass.name -> "ArrayListClass"
				HashMapClass.name -> "HashMapClass"
				HashSetClass.name -> "HashSetClass"
				WeakHashMapClass.name -> "WeakHashMapClass"
				WeakReferenceClass.name -> "WeakReferenceClass"
				SerializableClass.name -> "SerializableClass"
				EnumClass.name -> "EnumClass"
				MapClass.name -> "MapClass"
				Map_EntryClass.name -> "Map_EntryClass"
				SetClass.name -> "SetClass"
				ReferenceClass.name -> "ReferenceClass"
				VectorClass.name -> "VectorClass"

				FileClass.name -> "FileClass"
				InputStreamClass.name -> "InputStreamClass"
				OutputStreamClass.name -> "OutputStreamClass"
				BufferedReaderClass.name -> "BufferedReaderClass"

				DateClass.name -> "DateClass"
				TimeZoneClass.name -> "TimeZoneClass"
				SimpleDateFormatClass_Java.name -> "SimpleDateFormatClass_Java"

				TimerClass.name -> "TimerClass"
				TimerTaskClass.name -> "TimerTaskClass"
				ThreadClass.name -> "ThreadClass"
				ObserverClass.name -> "ObserverClass"

				StringBuilderClass.name -> "StringBuilderClass"
				StringBufferClass.name -> "StringBufferClass"

				ZipFileClass.name -> "ZipFileClass"
				ZipEntryClass.name -> "ZipEntryClass"
				ZipInputStreamClass.name -> "ZipInputStreamClass"
				ZipOutputStreamClass.name -> "ZipOutputStreamClass"

				else -> type.`object`
			}

			else -> "$type"
		}
	}
}
