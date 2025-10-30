package com.example.methoddumper.index

import com.example.methoddumper.model.MethodEntry
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import java.io.DataInput
import java.io.DataOutput

object MethodDataExternalizer : DataExternalizer<MethodEntry> {
    override fun save(out: DataOutput, value: MethodEntry) {
        IOUtil.writeUTF(out, value.name)
        IOUtil.writeUTF(out, value.body)
    }

    override fun read(`in`: DataInput): MethodEntry {
        val name = IOUtil.readUTF(`in`)
        val body = IOUtil.readUTF(`in`)
        return MethodEntry(name = name, body = body)
    }
}
