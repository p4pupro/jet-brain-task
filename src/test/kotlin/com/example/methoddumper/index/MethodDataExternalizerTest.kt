package com.example.methoddumper.index

import com.example.methoddumper.model.MethodEntry
import com.intellij.util.io.IOUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MethodDataExternalizerTest {

    @Test
    fun testSaveAndRead() {
        val externalizer = MethodDataExternalizer
        val original = MethodEntry("testMethod", "public void test() { }")

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)
        
        externalizer.save(dataOutput, original)
        
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val dataInput = DataInputStream(inputStream)
        
        val restored = externalizer.read(dataInput)
        
        assertEquals(original.name, restored.name)
        assertEquals(original.body, restored.body)
    }

    @Test
    fun testSaveAndReadEmptyBody() {
        val externalizer = MethodDataExternalizer
        val original = MethodEntry("emptyMethod", "")

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)
        
        externalizer.save(dataOutput, original)
        
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val dataInput = DataInputStream(inputStream)
        
        val restored = externalizer.read(dataInput)
        
        assertEquals(original.name, restored.name)
        assertEquals(original.body, restored.body)
    }

    @Test
    fun testSaveAndReadLargeBody() {
        val externalizer = MethodDataExternalizer
        val largeBody = "public void largeMethod() {\n" + "    ".repeat(100) + "}\n"
        val original = MethodEntry("largeMethod", largeBody)

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)
        
        externalizer.save(dataOutput, original)
        
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val dataInput = DataInputStream(inputStream)
        
        val restored = externalizer.read(dataInput)
        
        assertEquals(original.name, restored.name)
        assertEquals(original.body, restored.body)
    }
}
